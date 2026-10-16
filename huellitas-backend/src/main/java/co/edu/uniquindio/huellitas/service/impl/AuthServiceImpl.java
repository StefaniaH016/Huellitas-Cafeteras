package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.auth.AuthResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroResponseDTO;
import co.edu.uniquindio.huellitas.dto.auth.RegistroUsuarioDTO;
import co.edu.uniquindio.huellitas.dto.auth.UsuarioDTO;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Rol;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.BarrioRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.security.JwtUtils;
import co.edu.uniquindio.huellitas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BarrioRepository barrioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public RegistroResponseDTO registro(RegistroUsuarioDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new ConflictException("El correo ya se encuentra registrado: " + dto.correo());
        }

        Rol rol = dto.rol() != null ? dto.rol() : Rol.CIUDADANO_VOLUNTARIO;

        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre().trim())
                .correo(dto.correo().trim().toLowerCase())
                .contrasena(passwordEncoder.encode(dto.contrasena()))
                .telefono(dto.telefono())
                .rol(rol)
                .barrio(null)
                .municipioInteres(null)
                .verificado(false)
                .activo(true)
                .aceptoTratamientoDatos(true)
                .fechaAceptacionDatos(Instant.now())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return new RegistroResponseDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getCorreo(),
                guardado.getRol(),
                guardado.isVerificado()
        );
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
        String token = jwtUtils.generateToken(userDetails);

        UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isVerificado()
        );

        return AuthResponseDTO.bearer(token, usuarioDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuarioActual(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isVerificado()
        );
    }
}
