package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.BitacoraMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.BitacoraService;
import co.edu.uniquindio.huellitas.worker.NotificacionWorker;
import co.edu.uniquindio.huellitas.worker.PdfWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BitacoraServiceImpl implements BitacoraService {

    private final BitacoraMedicaRepository bitacoraMedicaRepository;
    private final RegistroMedicoRepository registroMedicoRepository;
    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraMapper bitacoraMapper;
    private final PdfWorker pdfWorker;
    private final NotificacionWorker notificacionWorker;

    @Override
    public BitacoraResponseDTO abrirBitacora(Long casoId, AbrirBitacoraDTO dto, String correoVeterinaria) {
        // 14.1: Una sola bitácora por caso (409 Conflict)
        if (bitacoraMedicaRepository.existsByCasoId(casoId)) {
            throw new ConflictException("Ya existe una bitácora médica registrada para este caso");
        }

        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        // 14.1: Solo con el caso EN_ATENCION
        if (caso.getEstado() != EstadoCaso.EN_ATENCION) {
            throw new ConflictException("Solo se puede abrir la bitácora médica para casos en estado EN_ATENCION (estado actual: " + caso.getEstado() + ")");
        }

        Usuario veterinaria = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario veterinaria no encontrado con correo: " + correoVeterinaria));

        // 14.1: Solo una VETERINARIA verificada (403 Forbidden)
        if (veterinaria.getRol() != Rol.VETERINARIA || !veterinaria.isVerificado()) {
            throw new AccessDeniedException("Solo una veterinaria verificada puede abrir la bitácora médica");
        }

        BitacoraMedica.BitacoraMedicaBuilder builder = BitacoraMedica.builder()
                .caso(caso)
                .fechaApertura(Instant.now())
                .altaMedica(false)
                .veterinario(veterinaria);

        if (veterinaria.getOrganizacion() instanceof Veterinaria vetOrg) {
            builder.veterinaria(vetOrg);
        }

        BitacoraMedica guardada = bitacoraMedicaRepository.save(builder.build());

        // 14.2: Guardar firmante, fecha y fechaFirma si hay observación inicial
        if (dto != null && dto.observacionInicial() != null && !dto.observacionInicial().isBlank()) {
            RegistroMedico registroInicial = RegistroMedico.builder()
                    .bitacoraMedica(guardada)
                    .diagnostico("Observación Inicial: " + dto.observacionInicial().trim())
                    .fecha(Instant.now())
                    .fechaFirma(Instant.now())
                    .firmante(veterinaria)
                    .build();
            registroMedicoRepository.save(registroInicial);
        }

        caso.setFechaUltimaActividad(Instant.now());
        casoRepository.save(caso);

        return bitacoraMapper.toResponseDTO(guardada, caso.getId(), veterinaria.getId(), "ABIERTA");
    }

    @Override
    @Transactional(readOnly = true)
    public BitacoraDetalleDTO obtenerBitacora(Long casoId) {
        return obtenerBitacora(casoId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BitacoraDetalleDTO obtenerBitacora(Long casoId, String correoUsuario) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso con id: " + casoId));

        // 14.4: Restringir lectura a la Veterinaria, la Fundación custodia y el reportante (Ley 1581 y Ley 576)
        validarAccesoBitacora(bitacora, correoUsuario);

        List<RegistroMedico> registros = registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(bitacora.getId());
        List<EvolucionDetalleDTO> evoluciones = bitacoraMapper.toEvolucionDetalleDTOList(registros);

        return bitacoraMapper.toDetalleDTO(bitacora, casoId, evoluciones);
    }

    @Override
    public RegistroMedicoResponseDTO agregarEvolucion(Long casoId, EvolucionDTO dto, String correoVeterinaria) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso: " + casoId));

        // 14.3: Bloquear nuevas evoluciones si ya tiene alta médica (409 Conflict)
        if (bitacora.isAltaMedica()) {
            throw new ConflictException("No se pueden agregar evoluciones a una bitácora con alta médica registrada");
        }

        if (bitacora.getCaso().getEstado() == EstadoCaso.ARCHIVADO) {
            throw new ConflictException("No se pueden agregar evoluciones a un caso archivado");
        }

        Usuario veterinaria = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinaria no encontrada con correo: " + correoVeterinaria));

        // 14.1: Solo VETERINARIA verificada
        if (veterinaria.getRol() != Rol.VETERINARIA || !veterinaria.isVerificado()) {
            throw new AccessDeniedException("Solo una veterinaria verificada puede registrar evoluciones médicas");
        }

        // 14.2: Guardar firmante, fecha, fechaFirma y alinear campos con el modelo entidad-relación
        String evolucionTexto = (dto.observaciones() != null && !dto.observaciones().isBlank())
                ? dto.observaciones()
                : (dto.evolucion() != null ? dto.evolucion() : "");

        String procedimiento = dto.procedimientos() != null ? dto.procedimientos() : dto.procedimiento();

        RegistroMedico registro = RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico(dto.diagnostico())
                .procedimiento(procedimiento)
                .tratamiento(dto.tratamiento())
                .medicamentos(dto.medicamentos())
                .evolucion(evolucionTexto)
                .fecha(Instant.now())
                .fechaFirma(Instant.now())
                .firmante(veterinaria)
                .build();

        RegistroMedico guardado = registroMedicoRepository.save(registro);

        bitacora.getCaso().setFechaUltimaActividad(Instant.now());
        casoRepository.save(bitacora.getCaso());

        // 14.6: Disparar notificaciones de evolución médica con W-01
        try {
            notificacionWorker.notificarEvolucionMedica(bitacora, guardado, veterinaria);
        } catch (Exception e) {
            log.warn("Error al disparar notificación de evolución médica: {}", e.getMessage());
        }

        return bitacoraMapper.toRegistroMedicoResponseDTO(guardado, bitacora.getId());
    }

    @Override
    public AltaMedicaResponseDTO registrarAlta(Long casoId, AltaMedicaDTO dto, String correoVeterinaria) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso: " + casoId));

        // 14.3: Bloquear alta duplicada (409 Conflict)
        if (bitacora.isAltaMedica()) {
            throw new ConflictException("El alta médica ya fue registrada previamente para este caso");
        }

        if (bitacora.getCaso().getEstado() == EstadoCaso.ARCHIVADO) {
            throw new ConflictException("No se puede registrar alta médica en un caso archivado");
        }

        Usuario veterinaria = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinaria no encontrada con correo: " + correoVeterinaria));

        // 14.1: Solo VETERINARIA verificada
        if (veterinaria.getRol() != Rol.VETERINARIA || !veterinaria.isVerificado()) {
            throw new AccessDeniedException("Solo una veterinaria verificada puede registrar el alta médica");
        }

        // 14.3: Guardar altaMedica y fechaAlta
        bitacora.setAltaMedica(true);
        bitacora.setFechaAlta(Instant.now());
        bitacoraMedicaRepository.save(bitacora);

        // 14.2: Guardar firmante, fecha y fechaFirma
        RegistroMedico registroAlta = RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico("ALTA MÉDICA: " + dto.condicionAlta())
                .tratamiento("Indicaciones: " + (dto.indicaciones() != null ? dto.indicaciones() : "Sin indicaciones adicionales"))
                .fecha(Instant.now())
                .fechaFirma(Instant.now())
                .firmante(veterinaria)
                .build();
        registroMedicoRepository.save(registroAlta);

        bitacora.getCaso().setFechaUltimaActividad(Instant.now());
        casoRepository.save(bitacora.getCaso());

        // 14.6: Disparar notificaciones de alta médica con W-01
        try {
            notificacionWorker.notificarAltaMedica(bitacora, veterinaria);
        } catch (Exception e) {
            log.warn("Error al disparar notificación de alta médica: {}", e.getMessage());
        }

        return bitacoraMapper.toAltaMedicaResponseDTO(bitacora, casoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarMisPacientes(String correoVeterinaria) {
        // 14.5: Listar pacientes de la veterinaria autenticada
        if (correoVeterinaria == null || correoVeterinaria.isBlank()) {
            throw new AccessDeniedException("Debe autenticarse para consultar sus pacientes.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoVeterinaria));

        if (usuario.getRol() != Rol.VETERINARIA || !usuario.isVerificado()) {
            throw new AccessDeniedException("Solo una veterinaria verificada puede consultar sus pacientes.");
        }

        Long organizacionId = usuario.getOrganizacion() != null ? usuario.getOrganizacion().getId() : null;
        List<BitacoraMedica> pacientes = bitacoraMedicaRepository.findMisPacientes(usuario.getId(), organizacionId);

        return bitacoraMapper.toPacienteResponseDTOList(pacientes);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfResumen(Long casoId) {
        // W-03: delegar la generación del PDF al PdfWorker
        return pdfWorker.generarResumenClinico(casoId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfResumen(Long casoId, String correoUsuario) {
        if (!casoRepository.existsById(casoId)) {
            throw new ResourceNotFoundException("Caso", casoId);
        }

        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso: " + casoId));

        // Subtarea 16.5: Validar permisos (403 si no tiene acceso autorizado)
        validarAccesoBitacora(bitacora, correoUsuario);

        // W-03: generar el resumen clínico en PDF
        return pdfWorker.generarResumenClinico(casoId);
    }

    // ── Helper de Autorización 14.4 ──────────────────────────────

    private void validarAccesoBitacora(BitacoraMedica bitacora, String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new AccessDeniedException("Debe autenticarse para consultar la bitácora médica.");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado"));

        // 1. Administrador (supervisión)
        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return;
        }

        Caso caso = bitacora.getCaso();

        // 2. Reportante del caso
        if (caso.getReportante() != null && caso.getReportante().getId().equals(usuario.getId())) {
            return;
        }

        // 3. Fundación con custodia del caso
        if (caso.getFundacionCustodio() != null && usuario.getOrganizacion() != null
                && caso.getFundacionCustodio().getId().equals(usuario.getOrganizacion().getId())) {
            return;
        }

        // 4. Veterinaria verificada o tratante
        if (usuario.getRol() == Rol.VETERINARIA && usuario.isVerificado()) {
            return;
        }

        throw new AccessDeniedException("No tiene permisos para consultar la bitácora clínica confidencial (Ley 1581 y Ley 576).");
    }
}