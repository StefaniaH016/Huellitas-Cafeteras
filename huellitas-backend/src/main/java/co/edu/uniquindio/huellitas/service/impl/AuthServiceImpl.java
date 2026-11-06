package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.auth.AuthResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.auth.UsuarioDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Fundacion;
import co.edu.uniquindio.huellitas.entity.Organizacion;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.Veterinaria;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.UsuarioMapper;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.MunicipioRepository;
import co.edu.uniquindio.huellitas.repository.OrganizacionRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.security.JwtUtils;
import co.edu.uniquindio.huellitas.service.AuthService;
import co.edu.uniquindio.huellitas.util.DocumentoSoporteUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BarrioRepository barrioRepository;
    private final MunicipioRepository municipioRepository;
    private final OrganizacionRepository organizacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final UsuarioMapper usuarioMapper;

    @Override
    public RegistroResponseDTO registro(RegistroUsuarioDTO dto) {
        return registro(dto, null);
    }

    @Override
    public RegistroResponseDTO registro(RegistroUsuarioDTO dto, MultipartFile documentoSoporte) {
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new ConflictException("El correo ya se encuentra registrado: " + dto.correo());
        }

        Rol rol = dto.rol() != null ? dto.rol() : Rol.CIUDADANO_VOLUNTARIO;

        Barrio barrio = null;
        if (dto.barrioId() != null) {
            barrio = barrioRepository.findById(dto.barrioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Barrio", dto.barrioId()));
        }

        Organizacion organizacion = crearOrganizacionInstitucional(dto, rol);
        if (documentoSoporte != null) {
            if (organizacion == null) {
                throw new IllegalArgumentException("El documento de soporte solo aplica a cuentas institucionales.");
            }
            DocumentoSoporteUtils.adjuntar(organizacion, documentoSoporte);
            organizacionRepository.save(organizacion);
        }
        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre().trim())
                .correo(dto.correo().trim().toLowerCase())
                .contrasena(passwordEncoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .rol(rol)
                .barrio(barrio)
                .municipioInteres(barrio != null
                        ? barrio.getMunicipio()
                        : organizacion != null ? organizacion.getMunicipio() : null)
                .organizacion(organizacion)
                .verificado(false)
                .activo(true)
                .aceptoTratamientoDatos(true)
                .fechaAceptacionDatos(Instant.now())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return usuarioMapper.toRegistroResponseDTO(guardado);
    }

    private Organizacion crearOrganizacionInstitucional(RegistroUsuarioDTO dto, Rol rol) {
        if (rol != Rol.FUNDACION_REFUGIO && rol != Rol.VETERINARIA) {
            return null;
        }
        if (dto.nombreEntidad() == null || dto.nombreEntidad().isBlank()
                || dto.nit() == null || dto.nit().isBlank()
                || dto.municipioOperacion() == null || dto.municipioOperacion().isBlank()) {
            throw new IllegalArgumentException(
                    "Nombre de entidad, NIT y municipio de operación son obligatorios para cuentas institucionales.");
        }

        String nit = dto.nit().trim();
        if (organizacionRepository.existsByNit(nit)) {
            throw new ConflictException("El NIT ya se encuentra registrado.");
        }

        var municipio = municipioRepository.findFirstByNombreIgnoreCase(dto.municipioOperacion().trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el municipio de operación: " + dto.municipioOperacion().trim()));
        Organizacion organizacion;
        if (rol == Rol.FUNDACION_REFUGIO) {
            organizacion = Fundacion.builder()
                    .nombre(dto.nombreEntidad().trim())
                    .nit(nit)
                    .municipio(municipio)
                    .capacidadAlbergue(0)
                    .aceptaCustodia(false)
                    .build();
        } else {
            organizacion = Veterinaria.builder()
                    .nombre(dto.nombreEntidad().trim())
                    .nit(nit)
                    .municipio(municipio)
                    .registroProfesional(dto.documentoLegal())
                    .build();
        }
        return organizacionRepository.save(organizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String correo = dto.correo().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, dto.contrasena())
        );

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
        String token = jwtUtils.generateToken(userDetails, usuario.getNombre());

        UsuarioDTO usuarioDTO = usuarioMapper.toUsuarioDTO(usuario);

        return AuthResponseDTO.bearer(token, usuarioDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuarioActual(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        return usuarioMapper.toUsuarioDTO(usuario);
    }
}
