package co.edu.uniquindio.huellitas.service.impl;

import co.edu.uniquindio.huellitas.dto.bitacora.*;
import co.edu.uniquindio.huellitas.entity.*;
import co.edu.uniquindio.huellitas.exception.ConflictException;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.*;
import co.edu.uniquindio.huellitas.service.BitacoraService;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BitacoraServiceImpl implements BitacoraService {

    private final BitacoraMedicaRepository bitacoraMedicaRepository;
    private final RegistroMedicoRepository registroMedicoRepository;
    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;

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

        return new BitacoraResponseDTO(
                guardada.getId(),
                caso.getId(),
                veterinaria.getId(),
                FechaUtils.formatear(guardada.getFechaApertura()),
                "ABIERTA"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BitacoraDetalleDTO obtenerBitacora(Long casoId) {
        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso con id: " + casoId));

        List<RegistroMedico> registros = registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(bitacora.getId());
        List<EvolucionDetalleDTO> evoluciones = registros.stream().map(r -> new EvolucionDetalleDTO(
                r.getId(),
                FechaUtils.formatear(r.getFecha()),
                r.getDiagnostico(),
                r.getTratamiento(),
                r.getEvolucion()
        )).toList();

        Map<String, Object> altaInfo = null;
        if (bitacora.isAltaMedica()) {
            altaInfo = new LinkedHashMap<>();
            altaInfo.put("fechaAlta", FechaUtils.formatear(bitacora.getFechaAlta()));
            altaInfo.put("estado", "ALTA_REGISTRADA");
        }

        String estado = bitacora.isAltaMedica() ? "ALTA_REGISTRADA" : "ABIERTA";
        return new BitacoraDetalleDTO(bitacora.getId(), casoId, estado, evoluciones, altaInfo);
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

        return new RegistroMedicoResponseDTO(
                guardado.getId(),
                bitacora.getId(),
                FechaUtils.formatear(guardado.getFecha())
        );
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

        return new AltaMedicaResponseDTO(
                casoId,
                FechaUtils.formatear(bitacora.getFechaAlta()),
                "ALTA_REGISTRADA"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfResumen(Long casoId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bitácora médica no encontrada para el caso: " + casoId));

        List<RegistroMedico> registros = registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(bitacora.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(40, 116, 166));
            Paragraph titulo = new Paragraph("HUELLITAS CAFETERAS", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Paragraph subtitulo = new Paragraph("Resumen Clínico y Bitácora de Atención Veterinaria\n\n", fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitulo);

            // Información del Animal y Caso
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            PdfPTable tableInfo = new PdfPTable(2);
            tableInfo.setWidthPercentage(100);

            PdfPCell cellHeader = new PdfPCell(new Phrase("Información General del Caso #" + casoId, fontHeader));
            cellHeader.setColspan(2);
            cellHeader.setBackgroundColor(new Color(40, 116, 166));
            cellHeader.setPadding(6);
            tableInfo.addCell(cellHeader);

            Animal a = caso.getAnimal();
            tableInfo.addCell(new Phrase("Nombre: " + (a != null && a.getNombre() != null ? a.getNombre() : "No registrado"), fontNormal));
            tableInfo.addCell(new Phrase("Especie: " + (a != null && a.getEspecie() != null ? a.getEspecie().name() : "N/A"), fontNormal));
            tableInfo.addCell(new Phrase("Raza: " + (a != null && a.getRaza() != null ? a.getRaza() : "Mestizo"), fontNormal));
            tableInfo.addCell(new Phrase("Sexo: " + (a != null && a.getSexo() != null ? a.getSexo().name() : "Indeterminado"), fontNormal));
            tableInfo.addCell(new Phrase("Estado del Caso: " + caso.getEstado().name(), fontNormal));
            tableInfo.addCell(new Phrase("Fecha de Apertura: " + FechaUtils.formatear(bitacora.getFechaApertura()), fontNormal));

            document.add(tableInfo);
            document.add(new Paragraph("\n"));

            // Tabla de Evoluciones Clínicas
            Paragraph secEvoluciones = new Paragraph("Historial de Atenciones y Evoluciones:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(secEvoluciones);
            document.add(new Paragraph("\n"));

            PdfPTable tableEvoluciones = new PdfPTable(4);
            tableEvoluciones.setWidthPercentage(100);
            tableEvoluciones.setWidths(new float[]{2.5f, 3.5f, 3f, 2f});

            String[] headers = {"Fecha", "Diagnóstico", "Tratamiento / Procedimiento", "Firmante"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, fontHeader));
                c.setBackgroundColor(new Color(52, 73, 94));
                c.setPadding(5);
                tableEvoluciones.addCell(c);
            }

            for (RegistroMedico rm : registros) {
                tableEvoluciones.addCell(new Phrase(FechaUtils.formatear(rm.getFecha()), fontNormal));
                tableEvoluciones.addCell(new Phrase(rm.getDiagnostico() != null ? rm.getDiagnostico() : "-", fontNormal));
                String trat = (rm.getTratamiento() != null ? rm.getTratamiento() : "") +
                        (rm.getProcedimiento() != null ? " (" + rm.getProcedimiento() + ")" : "");
                tableEvoluciones.addCell(new Phrase(trat.isBlank() ? "-" : trat, fontNormal));
                tableEvoluciones.addCell(new Phrase(rm.getFirmante() != null ? rm.getFirmante().getNombre() : "Veterinario", fontNormal));
            }

            document.add(tableEvoluciones);
            document.add(new Paragraph("\n"));

            // Sección Alta Médica
            if (bitacora.isAltaMedica()) {
                Paragraph altaPar = new Paragraph("ESTADO DE ALTA MÉDICA: REGISTRADA (Fecha: " + FechaUtils.formatear(bitacora.getFechaAlta()) + ")",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 132, 73)));
                document.add(altaPar);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF clínico para el caso {}", casoId, e);
            throw new RuntimeException("Error generando el resumen clínico en PDF", e);
        }
    }
}
