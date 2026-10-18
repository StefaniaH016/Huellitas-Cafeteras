package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.aporte.AporteResponseDTO;
import co.edu.uniquindio.huellitas.dto.aporte.CrearAporteDTO;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Foto;
import co.edu.uniquindio.huellitas.entity.Seguimiento;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.FotoRepository;
import co.edu.uniquindio.huellitas.repository.SeguimientoRepository;
import co.edu.uniquindio.huellitas.repository.UsuarioRepository;
import co.edu.uniquindio.huellitas.service.AporteService;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AporteServiceImpl implements AporteService {

    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final FotoRepository fotoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AporteResponseDTO> listarAportes(Long casoId) {
        if (!casoRepository.existsById(casoId)) {
            throw new ResourceNotFoundException("Caso", casoId);
        }

        List<Seguimiento> seguimientos = seguimientoRepository.findByCasoIdAndVisibleTrueOrderByFechaAsc(casoId);
        return seguimientos.stream().map(s -> new AporteResponseDTO(
                s.getId(),
                casoId,
                s.getAutor() != null ? s.getAutor().getId() : null,
                s.getComentario(),
                null,
                Collections.emptyList(),
                FechaUtils.formatear(s.getFecha())
        )).toList();
    }

    @Override
    public AporteResponseDTO crearAporte(Long casoId, CrearAporteDTO dto, String correoUsuario) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        Usuario autor = usuarioRepository.findByCorreo(correoUsuario.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoUsuario));

        TipoAporte tipo = (dto.ubicacionTexto() != null && !dto.ubicacionTexto().isBlank())
                ? TipoAporte.AVISTAMIENTO
                : TipoAporte.COMENTARIO;

        Seguimiento seguimiento = Seguimiento.builder()
                .caso(caso)
                .autor(autor)
                .tipo(tipo)
                .comentario(dto.descripcion())
                .fecha(Instant.now())
                .visible(true)
                .build();

        Seguimiento guardado = seguimientoRepository.save(seguimiento);

        List<String> fotosGuardadas = new ArrayList<>();
        if (dto.fotografias() != null && !dto.fotografias().isEmpty()) {
            for (String url : dto.fotografias()) {
                if (url != null && !url.isBlank()) {
                    Foto foto = Foto.builder()
                            .caso(caso)
                            .url(url)
                            .principal(false)
                            .build();
                    fotoRepository.save(foto);
                    fotosGuardadas.add(url);
                }
            }
        }

        caso.setFechaUltimaActividad(Instant.now());
        casoRepository.save(caso);

        return new AporteResponseDTO(
                guardado.getId(),
                caso.getId(),
                autor.getId(),
                guardado.getComentario(),
                dto.ubicacionTexto(),
                fotosGuardadas,
                FechaUtils.formatear(guardado.getFecha())
        );
    }
}
