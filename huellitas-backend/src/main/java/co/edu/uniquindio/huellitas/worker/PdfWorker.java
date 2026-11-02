package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.BitacoraMedica;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.RegistroMedico;
import co.edu.uniquindio.huellitas.exception.ResourceNotFoundException;
import co.edu.uniquindio.huellitas.repository.BitacoraMedicaRepository;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import co.edu.uniquindio.huellitas.repository.RegistroMedicoRepository;
import co.edu.uniquindio.huellitas.util.FechaUtils;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Worker W-03: Reportes Médicos en PDF.
 *
 * <p>Genera el documento PDF descargable con la historia clínica del animal
 * a partir de la bitácora médica registrada en el sistema. Consolida todos los
 * registros de diagnóstico, tratamiento, procedimiento y evolución en un documento
 * estructurado y firmado digitalmente por la veterinaria responsable.
 *
 * <p>Opera de forma síncrona dentro de la petición HTTP dado que el cliente
 * necesita la respuesta inmediata con los bytes del archivo.
 * Utiliza la librería OpenPDF (fork de iText) para la generación del documento.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PdfWorker {

    private final CasoRepository casoRepository;
    private final BitacoraMedicaRepository bitacoraMedicaRepository;
    private final RegistroMedicoRepository registroMedicoRepository;

    /**
     * Genera el PDF con el resumen clínico completo del caso indicado.
     *
     * @param casoId identificador del caso cuya bitácora se va a exportar
     * @return arreglo de bytes con el contenido del PDF
     * @throws ResourceNotFoundException si el caso o la bitácora no existen
     * @throws RuntimeException          si falla la generación del documento
     */
    public byte[] generarResumenClinico(Long casoId) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", casoId));

        BitacoraMedica bitacora = bitacoraMedicaRepository.findByCasoId(casoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bitácora médica no encontrada para el caso: " + casoId));

        List<RegistroMedico> registros =
                registroMedicoRepository.findByBitacoraMedicaIdOrderByFechaAsc(bitacora.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ── Encabezado ───────────────────────────────────────────────
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18,
                    new Color(40, 116, 166));
            Paragraph titulo = new Paragraph("HUELLITAS CAFETERAS", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14,
                    Color.DARK_GRAY);
            Paragraph subtitulo = new Paragraph(
                    "Resumen Clínico y Bitácora de Atención Veterinaria\n\n", fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitulo);

            // ── Tabla de información general ─────────────────────────────
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            PdfPTable tableInfo = new PdfPTable(2);
            tableInfo.setWidthPercentage(100);

            PdfPCell cellHeader = new PdfPCell(
                    new Phrase("Información General del Caso #" + casoId, fontHeader));
            cellHeader.setColspan(2);
            cellHeader.setBackgroundColor(new Color(40, 116, 166));
            cellHeader.setPadding(6);
            tableInfo.addCell(cellHeader);

            Animal a = caso.getAnimal();
            tableInfo.addCell(new Phrase(
                    "Nombre: " + (a != null && a.getNombre() != null ? a.getNombre() : "No registrado"),
                    fontNormal));
            tableInfo.addCell(new Phrase(
                    "Especie: " + (a != null && a.getEspecie() != null ? a.getEspecie().name() : "N/A"),
                    fontNormal));
            tableInfo.addCell(new Phrase(
                    "Raza: " + (a != null && a.getRaza() != null ? a.getRaza() : "Mestizo"),
                    fontNormal));
            tableInfo.addCell(new Phrase(
                    "Sexo: " + (a != null && a.getSexo() != null ? a.getSexo().name() : "Indeterminado"),
                    fontNormal));
            tableInfo.addCell(new Phrase("Estado del Caso: " + caso.getEstado().name(), fontNormal));
            tableInfo.addCell(new Phrase(
                    "Fecha de Apertura: " + FechaUtils.formatear(bitacora.getFechaApertura()),
                    fontNormal));

            document.add(tableInfo);
            document.add(new Paragraph("\n"));

            // ── Tabla de evoluciones clínicas ────────────────────────────
            Paragraph secEvoluciones = new Paragraph(
                    "Historial de Atenciones y Evoluciones:",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
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
                tableEvoluciones.addCell(new Phrase(
                        rm.getDiagnostico() != null ? rm.getDiagnostico() : "-", fontNormal));
                String trat = (rm.getTratamiento() != null ? rm.getTratamiento() : "")
                        + (rm.getProcedimiento() != null ? " (" + rm.getProcedimiento() + ")" : "");
                tableEvoluciones.addCell(new Phrase(trat.isBlank() ? "-" : trat, fontNormal));
                tableEvoluciones.addCell(new Phrase(
                        rm.getFirmante() != null ? rm.getFirmante().getNombre() : "Veterinario",
                        fontNormal));
            }

            document.add(tableEvoluciones);
            document.add(new Paragraph("\n"));

            // ── Alta médica ──────────────────────────────────────────────
            if (bitacora.isAltaMedica()) {
                Paragraph altaPar = new Paragraph(
                        "ESTADO DE ALTA MÉDICA: REGISTRADA (Fecha: "
                                + FechaUtils.formatear(bitacora.getFechaAlta()) + ")",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11,
                                new Color(30, 132, 73)));
                document.add(altaPar);
            }

            document.close();
            log.info("PDF clínico generado correctamente para el caso {}", casoId);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF clínico para el caso {}", casoId, e);
            throw new RuntimeException("Error generando el resumen clínico en PDF", e);
        }
    }
}
