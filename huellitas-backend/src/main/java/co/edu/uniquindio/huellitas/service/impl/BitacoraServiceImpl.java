package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.mapper.BitacoraMapper;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.BitacoraService;
import co.edu.uniquindio.huellitas.worker.PdfWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public BitacoraResponseDTO abrirBitacora(Long casoId, AbrirBitacoraDTO dto, String correoVeterinaria) {
        if (bitacoraMedicaRepository.existsByCasoId(casoId)) {
            throw new ConflictException("Ya existe una bitácora médica registrada para este caso");
        }

        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        Usuario veterinaria = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario veterinaria no encontrado con correo: " + correoVeterinaria));

        BitacoraMedica bitacora = BitacoraMedica.builder()
                .caso(caso)
                .fechaApertura(Instant.now())
                .altaMedica(false)
                .build();

        BitacoraMedica guardada = bitacoraMedicaRepository.save(bitacora);

        if (dto != null && dto.observacionInicial() != null && !dto.observacionInicial().isBlank()) {
            RegistroMedico registroInicial = RegistroMedico.builder()
                    .bitacoraMedica(guardada)
                    .diagnostico("Observación Inicial: " + dto.observacionInicial())
                    .fecha(Instant.now())
                    .firmante(veterinaria)
                    .build();
            registroMedicoRepository.save(registroInicial);
        }

        return bitacoraMapper.toResponseDTO(guardada, caso.getId(), veterinaria.getId(), "ABIERTA");
    }

    @Override
    @Transactional(readOnly = true)
    public BitacoraDetalleDTO obtenerBitacora(Long casoId) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso con id: " + casoId));

        List<RegistroMedico> registros = registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(bitacora.getId());
        List<EvolucionDetalleDTO> evoluciones = bitacoraMapper.toEvolucionDetalleDTOList(registros);

        return bitacoraMapper.toDetalleDTO(bitacora, casoId, evoluciones);
    }

    @Override
    public RegistroMedicoResponseDTO agregarEvolucion(Long casoId, EvolucionDTO dto, String correoVeterinaria) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso: " + casoId));

        if (bitacora.isAltaMedica()) {
            throw new ConflictException("No se pueden agregar evoluciones a una bitácora con alta médica registrada");
        }

        Usuario veterinaria = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinaria no encontrada con correo: " + correoVeterinaria));

        String evolucionTexto = dto.observaciones() != null ? dto.observaciones() : "";
        if (dto.medicamentos() != null && !dto.medicamentos().isBlank()) {
            evolucionTexto += " | Medicamentos: " + dto.medicamentos();
        }

        RegistroMedico registro = RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico(dto.diagnostico())
                .procedimiento(dto.procedimientos())
                .tratamiento(dto.tratamiento())
                .evolucion(evolucionTexto)
                .fecha(Instant.now())
                .fechaFirma(Instant.now())
                .firmante(veterinaria)
                .build();

        RegistroMedico guardado = registroMedicoRepository.save(registro);

        return bitacoraMapper.toRegistroMedicoResponseDTO(guardado, bitacora.getId());
    }

    @Override
    public AltaMedicaResponseDTO registrarAlta(Long casoId, AltaMedicaDTO dto, String correoVeterinaria) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso: " + casoId));

        if (bitacora.isAltaMedica()) {
            throw new ConflictException("El alta médica ya fue registrada previamente para este caso");
        }

        Usuario veterinaria = usuarioRepository.findByCorreo(correoVeterinaria.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinaria no encontrada con correo: " + correoVeterinaria));

        bitacora.setAltaMedica(true);
        bitacora.setFechaAlta(Instant.now());
        bitacoraMedicaRepository.save(bitacora);

        RegistroMedico registroAlta = RegistroMedico.builder()
                .bitacoraMedica(bitacora)
                .diagnostico("ALTA MÉDICA: " + dto.condicionAlta())
                .tratamiento("Indicaciones: " + (dto.indicaciones() != null ? dto.indicaciones() : "Sin indicaciones adicionales"))
                .fecha(Instant.now())
                .fechaFirma(Instant.now())
                .firmante(veterinaria)
                .build();
        registroMedicoRepository.save(registroAlta);

        return bitacoraMapper.toAltaMedicaResponseDTO(bitacora, casoId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfResumen(Long casoId) {
        // W-03: delegar la generación del PDF al PdfWorker
        return pdfWorker.generarResumenClinico(casoId);
    }
}
