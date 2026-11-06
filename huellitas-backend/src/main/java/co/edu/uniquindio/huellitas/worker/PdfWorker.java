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
        long startTime = System.currentTimeMillis();

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

            // ── Tabla de información general (Alineada al modelo ER) ─────
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

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
                    "Paciente: " + (a != null && a.getNombre() != null ? a.getNombre() : "Sin nombre registrado"),
                    fontNormal));
            tableInfo.addCell(new Phrase(
                    "Especie / Raza: " + (a != null && a.getEspecie() != null ? a.getEspecie().name() : "N/A")
                            + " / " + (a != null && a.getRaza() != null ? a.getRaza() : "Mestizo"),
                    fontNormal));
            tableInfo.addCell(new Phrase(
                    "Sexo: " + (a != null && a.getSexo() != null ? a.getSexo().name() : "Indeterminado"),
                    fontNormal));
            tableInfo.addCell(new Phrase("Tipo de Caso: " + caso.getTipoCaso().name(), fontNormal));
            tableInfo.addCell(new Phrase("Urgencia: " + caso.getUrgencia().name(), fontNormal));
            tableInfo.addCell(new Phrase("Estado del Caso: " + caso.getEstado().name(), fontNormal));

            String ubicacion = "No especificada";
            if (caso.getBarrio() != null) {
                ubicacion = caso.getBarrio().getNombre();
                if (caso.getBarrio().getMunicipio() != null) {
                    ubicacion += ", " + caso.getBarrio().getMunicipio().getNombre();
                }
            }
            tableInfo.addCell(new Phrase("Ubicación: " + ubicacion, fontNormal));
            tableInfo.addCell(new Phrase(
                    "Fecha de Apertura: " + FechaUtils.formatear(bitacora.getFechaApertura()),
                    fontNormal));

            String responsable = "Clínica Veterinaria";
            if (bitacora.getVeterinario() != null) {
                responsable = bitacora.getVeterinario().getNombre();
            } else if (bitacora.getVeterinaria() != null) {
                responsable = bitacora.getVeterinaria().getNombre();
            }
            PdfPCell cellResp = new PdfPCell(new Phrase("Profesional / Veterinaria a Cargo: " + responsable, fontNormal));
            cellResp.setColspan(2);
            tableInfo.addCell(cellResp);

            document.add(tableInfo);
            document.add(new Paragraph("\n"));

            // ── Tabla de evoluciones clínicas (Alineada al modelo ER) ────
            Paragraph secEvoluciones = new Paragraph(
                    "Historial Clínico de Atenciones y Evoluciones:",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(secEvoluciones);
            document.add(new Paragraph("\n"));

            PdfPTable tableEvoluciones = new PdfPTable(5);
            tableEvoluciones.setWidthPercentage(100);
            tableEvoluciones.setWidths(new float[]{2f, 2.5f, 2.5f, 3f, 2f});

            String[] headers = {"Fecha", "Diagnóstico", "Procedimiento", "Tratamiento y Medicamentos", "Firmante"};
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
                tableEvoluciones.addCell(new Phrase(
                        rm.getProcedimiento() != null ? rm.getProcedimiento() : "-", fontNormal));

                String tratMed = (rm.getTratamiento() != null ? rm.getTratamiento() : "");
                if (rm.getMedicamentos() != null && !rm.getMedicamentos().isBlank()) {
                    tratMed += (tratMed.isBlank() ? "" : "\n") + "Medicamentos: " + rm.getMedicamentos();
                }
                tableEvoluciones.addCell(new Phrase(tratMed.isBlank() ? "-" : tratMed, fontNormal));

                String firmanteInfo = rm.getFirmante() != null ? rm.getFirmante().getNombre() : "Veterinario";
                if (rm.getFechaFirma() != null) {
                    firmanteInfo += "\nFirma: " + FechaUtils.formatear(rm.getFechaFirma());
                }
                tableEvoluciones.addCell(new Phrase(firmanteInfo, fontNormal));
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

            // ── Cierre del caso (si aplica) ──────────────────────────────
            if (caso.getCierreCaso() != null) {
                document.add(new Paragraph("\n"));
                Paragraph cierrePar = new Paragraph(
                        "RESOLUCIÓN Y CIERRE DEL CASO:\n"
                                + "• Resultado: " + caso.getCierreCaso().getResultado().name() + "\n"
                                + "• Fecha de Cierre: " + FechaUtils.formatear(caso.getCierreCaso().getFecha()) + "\n"
                                + (caso.getCierreCaso().getEntregadoA() != null ? "• Entregado a: " + caso.getCierreCaso().getEntregadoA() + "\n" : "")
                                + (caso.getCierreCaso().getObservaciones() != null ? "• Observaciones: " + caso.getCierreCaso().getObservaciones() : ""),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
                                new Color(40, 116, 166)));
                document.add(cierrePar);
            }

            document.close();

            long duracion = System.currentTimeMillis() - startTime;
            log.info("PDF clínico generado correctamente para el caso {} en {} ms", casoId, duracion);
            if (duracion > 5000) {
                log.warn("La generación del PDF clínico excedió el límite de 5 segundos: {} ms", duracion);
            }

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF clínico para el caso {}", casoId, e);
            throw new RuntimeException("Error generando el resumen clínico en PDF", e);
        }
    }
}
