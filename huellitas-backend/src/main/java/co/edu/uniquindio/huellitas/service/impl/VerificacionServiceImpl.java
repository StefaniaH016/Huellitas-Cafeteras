package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.verificacion.DecisionVerificacionDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionPendienteDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.VerificacionResponseDTO;
import co.edu.uniquindio.huellitas.dto.verificacion.DocumentoSoporteDTO;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.DocumentoSoporte;
import co.edu.uniquindio.huellitas.entity.enums.EstadoVerificacion;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.VerificacionMapper;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.service.VerificacionService;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import co.edu.uniquindio.huellitas.util.DocumentoSoporteUtils;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificacionServiceImpl implements VerificacionService {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final MunicipioRepository municipioRepository;
    private final VerificacionMapper verificacionMapper;
    private final NotificacionWorker notificacionWorker;

    @Override
    @Transactional(readOnly = true)
    public List<VerificacionPendienteDTO> listarPendientes() {
        List<Rol> rolesInstitucionales = List.of(Rol.FUNDACION_REFUGIO, Rol.VETERINARIA);
        List<Usuario> pendientes = usuarioRepository.findByVerificadoFalseAndRolIn(rolesInstitucionales);

        return pendientes.stream()
                .filter(u -> u.getOrganizacion() == null
                        || u.getOrganizacion().getEstadoVerificacion() == EstadoVerificacion.PENDIENTE)
                .map(verificacionMapper::toPendienteDTO)
                .toList();
    }

    @Override
    public VerificacionResponseDTO resolverVerificacion(Long usuarioId, DecisionVerificacionDTO dto, String correoAdmin) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        Usuario admin = usuarioRepository.findByCorreo(correoAdmin.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado con correo: " + correoAdmin));

        Organizacion organizacion = usuario.getOrganizacion() == null
                ? null
                : (Organizacion) Hibernate.unproxy(usuario.getOrganizacion());
        if (usuario.getRol() == Rol.FUNDACION_REFUGIO || usuario.getRol() == Rol.VETERINARIA) {
            if (organizacion == null) {
                throw new ConflictException(
                        "La cuenta institucional no tiene una organización asociada. Debe completar nuevamente su registro.");
            }
            if ((usuario.getRol() == Rol.FUNDACION_REFUGIO
                    && !(organizacion instanceof co.edu.uniquindio.huellitas.entity.Fundacion))
                    || (usuario.getRol() == Rol.VETERINARIA
                    && !(organizacion instanceof co.edu.uniquindio.huellitas.entity.Veterinaria))) {
                throw new ConflictException("El tipo de organización no coincide con el rol de la cuenta.");
            }
        }

        boolean aprobada = "APROBAR".equalsIgnoreCase(dto.decision());
        usuario.setVerificado(aprobada);

        if (organizacion != null) {
            organizacion.setEstadoVerificacion(aprobada ? EstadoVerificacion.VERIFICADA : EstadoVerificacion.RECHAZADA);
            organizacion.setFechaVerificacion(Instant.now());
            organizacion.setVerificador(admin);
            if (organizacion instanceof co.edu.uniquindio.huellitas.entity.Fundacion fundacion) {
                fundacion.setAceptaCustodia(aprobada);
                fundacion.setCapacidadAlbergue(aprobada ? 10 : 0);
            }
            organizacionRepository.save(organizacion);
        }

        usuarioRepository.save(usuario);
        notificarDespuesDeGuardar(usuario, aprobada);

        return verificacionMapper.toResponseDTO(usuario, aprobada);
    }

    @Override
    public VerificacionResponseDTO solicitarRevision(
            String correo,
            MultipartFile documentoSoporte,
            String nombreEntidad,
            String nit,
            String municipioOperacion) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));
        if (usuario.getRol() != Rol.FUNDACION_REFUGIO && usuario.getRol() != Rol.VETERINARIA) {
            throw new ConflictException("Solo las fundaciones y veterinarias pueden solicitar una revisión.");
        }

        Organizacion organizacion = usuario.getOrganizacion() == null
                ? crearOrganizacion(usuario, nombreEntidad, nit, municipioOperacion)
                : (Organizacion) Hibernate.unproxy(usuario.getOrganizacion());
        if ((usuario.getRol() == Rol.FUNDACION_REFUGIO
                && !(organizacion instanceof Fundacion))
                || (usuario.getRol() == Rol.VETERINARIA
                && !(organizacion instanceof Veterinaria))) {
            throw new ConflictException("El tipo de organización no coincide con el rol de la cuenta.");
        }
        if (organizacion.getEstadoVerificacion() == EstadoVerificacion.VERIFICADA) {
            throw new ConflictException("La organización ya se encuentra verificada.");
        }

        DocumentoSoporteUtils.adjuntar(organizacion, documentoSoporte);
        organizacion.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        organizacion.setFechaVerificacion(null);
        organizacion.setVerificador(null);
        usuario.setVerificado(false);
        Organizacion guardada = organizacionRepository.save(organizacion);
        usuario.setOrganizacion(guardada);
        usuarioRepository.save(usuario);
        return new VerificacionResponseDTO(usuario.getId(), EstadoVerificacion.PENDIENTE.name(), false);
    }

    private Organizacion crearOrganizacion(
            Usuario usuario,
            String nombreEntidad,
            String nit,
            String municipioOperacion) {
        if (nombreEntidad == null || nombreEntidad.isBlank()
                || nit == null || nit.isBlank()
                || municipioOperacion == null || municipioOperacion.isBlank()) {
            throw new ConflictException(
                    "Completa el nombre, NIT y municipio de operación de tu organización para solicitar la revisión.");
        }

        String nitNormalizado = nit.trim();
        if (organizacionRepository.existsByNit(nitNormalizado)) {
            throw new ConflictException(
                    "Ese NIT ya está asociado a una organización. Contacta al administrador para recuperar la asociación de tu cuenta.");
        }
        var municipio = municipioRepository.findFirstByNombreIgnoreCase(municipioOperacion.trim())
                .orElseThrow(() -> new ConflictException(
                        "No se encontró el municipio de operación indicado."));

        if (usuario.getRol() == Rol.FUNDACION_REFUGIO) {
            return Fundacion.builder()
                    .nombre(nombreEntidad.trim())
                    .nit(nitNormalizado)
                    .municipio(municipio)
                    .capacidadAlbergue(0)
                    .aceptaCustodia(false)
                    .build();
        }
        return Veterinaria.builder()
                .nombre(nombreEntidad.trim())
                .nit(nitNormalizado)
                .municipio(municipio)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoSoporteDTO obtenerDocumentoSoporte(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));
        if (usuario.getOrganizacion() == null) {
            throw new ResourceNotFoundException("Documento de soporte", usuarioId);
        }
        Organizacion organizacion = (Organizacion) Hibernate.unproxy(usuario.getOrganizacion());
        DocumentoSoporte documento = organizacion.getDocumentoSoporte();
        if (documento == null) {
            throw new ResourceNotFoundException("Documento de soporte", usuarioId);
        }
        return new DocumentoSoporteDTO(
                documento.getNombreArchivo(),
                documento.getTipoContenido(),
                documento.getContenido()
        );
    }

    private void notificarDespuesDeGuardar(Usuario usuario, boolean aprobada) {
        Runnable notificar = () -> notificacionWorker.notificarVerificacionOrganizacion(usuario, aprobada);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificar.run();
                }
            });
        } else {
            notificar.run();
        }
    }
}
