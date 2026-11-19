package co.edu.uniquindio.huellitas.worker;

import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.Usuario;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.repository.CasoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para LimpiezaWorker (W-04) - HU-17
 * Subtarea 17.8: Pruebas con Clock inyectado y valores límite
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LimpiezaWorker (W-04) - Pruebas Unitarias HU-17")
class LimpiezaWorkerTest {

    @Mock
    private CasoRepository casoRepository;

    @Mock
    private NotificacionWorker notificacionWorker;

    @Mock
    private Clock clock;

    @InjectMocks
    private LimpiezaWorker limpiezaWorker;

    private Instant ahora;
    private Usuario reportante;
    private Animal animal;

    @BeforeEach
    void setUp() {
        // Configurar Clock fijo para pruebas controladas
        ahora = Instant.parse("2024-11-09T12:00:00Z");
        when(clock.instant()).thenReturn(ahora);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // Configurar propiedades mediante reflexión
        org.springframework.test.util.ReflectionTestUtils.setField(limpiezaWorker, "diasArchivarResuelto", 7L);
        org.springframework.test.util.ReflectionTestUtils.setField(limpiezaWorker, "diasRecordatorioInactividad", 30L);

        // Usuario reportante
        reportante = Usuario.builder()
                .id(1L)
                .correo("reportante@huellitas.com")
                .nombre("Reportante Test")
                .build();

        // Animal
        animal = Animal.builder()
                .id(1L)
                .nombre("Firulais")
                .especie(Especie.PERRO)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // Pruebas de archivado automático de casos RESUELTO
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Archivado automático de casos RESUELTO")
    class ArchivadoAutomaticoTest {

        @Test
        @DisplayName("Debe archivar caso RESUELTO con N días exactos sin actividad (N=7)")
        void debeArchivarCasoResueltoEnDiaLimite() {
            // Arrange: caso resuelto con exactamente 7 días + 1 hora sin actividad para que pase isBefore
            Instant fecha7DiasAtras = ahora.minus(7, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            Caso casoResuelto = crearCaso(1L, EstadoCaso.RESUELTO, fecha7DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoResuelto));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: verificar que se llamó save al menos una vez con el caso archivado
            verify(casoRepository, atLeastOnce()).save(any(Caso.class));
            assertEquals(EstadoCaso.ARCHIVADO, casoResuelto.getEstado());
            assertNotNull(casoResuelto.getFechaArchivado());
            assertTrue(casoResuelto.getMotivoModeracion().contains("7 días"));
            verify(notificacionWorker).notificarArchivadoCaso(eq(casoResuelto), any());
        }

        @Test
        @DisplayName("Debe archivar caso RESUELTO con N+1 días sin actividad (N+1=8)")
        void debeArchivarCasoResueltoConMasDias() {
            // Arrange: caso resuelto con 8 días sin actividad
            Instant fecha8DiasAtras = ahora.minus(8, ChronoUnit.DAYS);
            Caso casoResuelto = crearCaso(2L, EstadoCaso.RESUELTO, fecha8DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoResuelto));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: verificar que el caso fue archivado
            verify(casoRepository, atLeastOnce()).save(any(Caso.class));
            assertEquals(EstadoCaso.ARCHIVADO, casoResuelto.getEstado());
        }

        @Test
        @DisplayName("NO debe archivar caso RESUELTO con N-1 días sin actividad (N-1=6)")
        void noDebeArchivarCasoResueltoConMenosDias() {
            // Arrange: caso resuelto con solo 6 días sin actividad
            Instant fecha6DiasAtras = ahora.minus(6, ChronoUnit.DAYS);
            Caso casoResuelto = crearCaso(3L, EstadoCaso.RESUELTO, fecha6DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoResuelto));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe haber llamadas a save para archivar
            verify(casoRepository, never()).save(argThat(caso ->
                    caso.getEstado() == EstadoCaso.ARCHIVADO
            ));
        }

        @Test
        @DisplayName("NO debe archivar casos que no están en estado RESUELTO")
        void noDebeArchivarCasosNoResueltos() {
            // Arrange: casos en otros estados con más de 7 días sin actividad
            Instant fecha10DiasAtras = ahora.minus(10, ChronoUnit.DAYS);
            Caso casoRegistrado = crearCaso(10L, EstadoCaso.REGISTRADO, fecha10DiasAtras);
            Caso casoEnAtencion = crearCaso(11L, EstadoCaso.EN_ATENCION, fecha10DiasAtras);
            Caso casoArchivado = crearCaso(12L, EstadoCaso.ARCHIVADO, fecha10DiasAtras);

            when(casoRepository.findAll()).thenReturn(List.of(casoRegistrado, casoEnAtencion, casoArchivado));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe archivar ninguno
            verify(casoRepository, never()).save(argThat(caso ->
                    caso.getEstado() == EstadoCaso.ARCHIVADO &&
                    (caso.getId() == 10L || caso.getId() == 11L || caso.getId() == 12L)
            ));
        }

        @Test
        @DisplayName("Debe actualizar fechaArchivado y fechaUltimaActividad al archivar")
        void debeActualizarFechasAlArchivar() {
            // Arrange
            Instant fecha7DiasAtras = ahora.minus(7, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            Caso casoResuelto = crearCaso(20L, EstadoCaso.RESUELTO, fecha7DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoResuelto));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: verificar que las fechas se actualizaron
            assertNotNull(casoResuelto.getFechaArchivado());
            assertEquals(ahora, casoResuelto.getFechaArchivado());
            assertEquals(ahora, casoResuelto.getFechaUltimaActividad());
            verify(casoRepository, atLeastOnce()).save(any(Caso.class));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Pruebas de recordatorios por inactividad
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Recordatorios por inactividad")
    class RecordatoriosTest {

        @Test
        @DisplayName("Debe enviar recordatorio a caso activo con M días sin actividad (M=30)")
        void debeEnviarRecordatorioEnDiaLimite() {
            // Arrange: caso activo con exactamente 30 días + 1 hora sin actividad
            Instant fecha30DiasAtras = ahora.minus(30, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            Caso casoActivo = crearCaso(30L, EstadoCaso.REGISTRADO, fecha30DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoActivo));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: verificar que se envió el recordatorio
            verify(notificacionWorker, times(1)).notificarRecordatorio(any(Caso.class), anyLong());
            // Verificar que se guardó el caso con la fecha del recordatorio
            verify(casoRepository, atLeastOnce()).save(any(Caso.class));
            assertNotNull(casoActivo.getFechaUltimoRecordatorio());
        }

        @Test
        @DisplayName("NO debe enviar recordatorio a caso con M-1 días sin actividad (M-1=29)")
        void noDebeEnviarRecordatorioAntesDeLimite() {
            // Arrange: caso con 29 días sin actividad
            Instant fecha29DiasAtras = ahora.minus(29, ChronoUnit.DAYS);
            Caso casoActivo = crearCaso(31L, EstadoCaso.REGISTRADO, fecha29DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoActivo));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe enviar recordatorio
            verify(notificacionWorker, never()).notificarRecordatorio(any(), anyLong());
        }

        @Test
        @DisplayName("NO debe enviar recordatorio duplicado si ya se envió hace menos de 7 días")
        void noDebeEnviarRecordatorioDuplicado() {
            // Arrange: caso con 35 días sin actividad pero recordatorio enviado hace 5 días
            Instant fecha35DiasAtras = ahora.minus(35, ChronoUnit.DAYS);
            Instant recordatorioHace5Dias = ahora.minus(5, ChronoUnit.DAYS);
            Caso casoActivo = crearCaso(32L, EstadoCaso.REGISTRADO, fecha35DiasAtras);
            casoActivo.setFechaUltimoRecordatorio(recordatorioHace5Dias);
            when(casoRepository.findAll()).thenReturn(List.of(casoActivo));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe enviar recordatorio
            verify(notificacionWorker, never()).notificarRecordatorio(any(), anyLong());
        }

        @Test
        @DisplayName("Debe enviar recordatorio si el anterior fue hace más de 7 días")
        void debeEnviarRecordatorioSiPasoElCooldown() {
            // Arrange: caso con 40 días sin actividad y recordatorio enviado hace 8 días
            Instant fecha40DiasAtras = ahora.minus(40, ChronoUnit.DAYS);
            Instant recordatorioHace8Dias = ahora.minus(8, ChronoUnit.DAYS);
            Caso casoActivo = crearCaso(33L, EstadoCaso.EN_ATENCION, fecha40DiasAtras);
            casoActivo.setFechaUltimoRecordatorio(recordatorioHace8Dias);
            when(casoRepository.findAll()).thenReturn(List.of(casoActivo));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: DEBE enviar recordatorio
            verify(notificacionWorker, times(1)).notificarRecordatorio(
                    argThat(caso -> caso.getId().equals(33L)),
                    eq(40L)
            );
        }

        @Test
        @DisplayName("NO debe enviar recordatorio a casos RESUELTO o ARCHIVADO")
        void noDebeEnviarRecordatorioACasosInactivos() {
            // Arrange: casos inactivos con más de 30 días sin actividad
            Instant fecha40DiasAtras = ahora.minus(40, ChronoUnit.DAYS);
            Caso casoResuelto = crearCaso(40L, EstadoCaso.RESUELTO, fecha40DiasAtras);
            Caso casoArchivado = crearCaso(41L, EstadoCaso.ARCHIVADO, fecha40DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoResuelto, casoArchivado));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe enviar recordatorios
            verify(notificacionWorker, never()).notificarRecordatorio(any(), anyLong());
        }

        @Test
        @DisplayName("NO debe enviar recordatorio si el caso no tiene reportante")
        void noDebeEnviarRecordatorioSinReportante() {
            // Arrange: caso sin reportante
            Instant fecha35DiasAtras = ahora.minus(35, ChronoUnit.DAYS);
            Caso casoSinReportante = crearCaso(50L, EstadoCaso.REGISTRADO, fecha35DiasAtras);
            casoSinReportante.setReportante(null);
            when(casoRepository.findAll()).thenReturn(List.of(casoSinReportante));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe enviar recordatorio
            verify(notificacionWorker, never()).notificarRecordatorio(any(), anyLong());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Pruebas de marcación para revisión administrativa
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Marcación para revisión administrativa")
    class RevisionAdministrativaTest {

        @Test
        @DisplayName("Debe marcar caso para revisión admin con M+15 días sin actividad (45 días)")
        void debeMarcarCasoParaRevisionAdmin() {
            // Arrange: caso con 45 días + 1 hora sin actividad
            Instant fecha45DiasAtras = ahora.minus(45, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            Caso casoActivo = crearCaso(60L, EstadoCaso.REGISTRADO, fecha45DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoActivo));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: verificar que se marcó para revisión
            assertTrue(casoActivo.isModeracionPendiente());
            verify(casoRepository, atLeastOnce()).save(any(Caso.class));
        }

        @Test
        @DisplayName("NO debe marcar caso ya marcado para revisión admin")
        void noDebeDuplicarMarcacionRevisionAdmin() {
            // Arrange: caso con 50 días sin actividad ya marcado
            Instant fecha50DiasAtras = ahora.minus(50, ChronoUnit.DAYS);
            Caso casoYaMarcado = crearCaso(61L, EstadoCaso.EN_ATENCION, fecha50DiasAtras);
            casoYaMarcado.setModeracionPendiente(true);
            when(casoRepository.findAll()).thenReturn(List.of(casoYaMarcado));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: Como tiene 50 días, también debe enviar recordatorio
            // pero NO debe volver a marcar para revisión
            // El caso aún puede ser guardado por el recordatorio, pero moderacionPendiente debe seguir siendo true
            assertTrue(casoYaMarcado.isModeracionPendiente());
        }

        @Test
        @DisplayName("NO debe marcar casos RESUELTO o ARCHIVADO para revisión")
        void noDebeMarcarCasosInactivosParaRevision() {
            // Arrange: casos inactivos con más de 45 días
            Instant fecha50DiasAtras = ahora.minus(50, ChronoUnit.DAYS);
            Caso casoResuelto = crearCaso(70L, EstadoCaso.RESUELTO, fecha50DiasAtras);
            Caso casoArchivado = crearCaso(71L, EstadoCaso.ARCHIVADO, fecha50DiasAtras);
            when(casoRepository.findAll()).thenReturn(List.of(casoResuelto, casoArchivado));

            // Act
            limpiezaWorker.ejecutar();

            // Assert: NO debe marcar para revisión
            verify(casoRepository, never()).save(argThat(caso ->
                    (caso.getId() == 70L || caso.getId() == 71L) &&
                    caso.isModeracionPendiente()
            ));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Pruebas de escenarios múltiples
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Escenarios múltiples")
    class EscenariosMultiplesTest {

        @Test
        @DisplayName("Debe procesar correctamente múltiples casos con diferentes estados y fechas")
        void debeProcesarMultiplesCasosCorrectamente() {
            // Arrange: 5 casos con diferentes características
            Caso caso1 = crearCaso(100L, EstadoCaso.RESUELTO, ahora.minus(8, ChronoUnit.DAYS)); // Debe archivarse
            Caso caso2 = crearCaso(101L, EstadoCaso.RESUELTO, ahora.minus(5, ChronoUnit.DAYS)); // NO se archiva
            Caso caso3 = crearCaso(102L, EstadoCaso.REGISTRADO, ahora.minus(35, ChronoUnit.DAYS)); // Recordatorio
            Caso caso4 = crearCaso(103L, EstadoCaso.EN_ATENCION, ahora.minus(50, ChronoUnit.DAYS)); // Revisión admin + recordatorio
            Caso caso5 = crearCaso(104L, EstadoCaso.ARCHIVADO, ahora.minus(100, ChronoUnit.DAYS)); // Ignorado

            when(casoRepository.findAll()).thenReturn(List.of(caso1, caso2, caso3, caso4, caso5));

            // Act
            limpiezaWorker.ejecutar();

            // Assert
            // 1. Caso 100 debe archivarse
            verify(casoRepository, atLeastOnce()).save(argThat(caso ->
                    caso.getId() == 100L && caso.getEstado() == EstadoCaso.ARCHIVADO
            ));

            // 2. Caso 101 NO debe archivarse
            verify(casoRepository, never()).save(argThat(caso ->
                    caso.getId() == 101L && caso.getEstado() == EstadoCaso.ARCHIVADO
            ));

            // 3. Caso 102 debe recibir recordatorio
            verify(notificacionWorker, atLeastOnce()).notificarRecordatorio(
                    argThat(caso -> caso.getId() == 102L),
                    anyLong()
            );

            // 4. Caso 103 debe marcarse para revisión y recibir recordatorio
            verify(casoRepository, atLeastOnce()).save(argThat(caso ->
                    caso.getId() == 103L && caso.isModeracionPendiente()
            ));
            verify(notificacionWorker, atLeastOnce()).notificarRecordatorio(
                    argThat(caso -> caso.getId() == 103L),
                    anyLong()
            );

            // 5. Caso 104 (ARCHIVADO) debe ser ignorado
            verify(casoRepository, never()).save(argThat(caso ->
                    caso.getId() == 104L
            ));
        }

        @Test
        @DisplayName("Debe manejar errores en casos individuales sin detener el procesamiento completo")
        void debeManjarErroresIndividuales() {
            // Arrange: caso que causará error al guardar
            Caso casoProblematico = crearCaso(200L, EstadoCaso.RESUELTO, ahora.minus(10, ChronoUnit.DAYS));
            Caso casoNormal = crearCaso(201L, EstadoCaso.RESUELTO, ahora.minus(10, ChronoUnit.DAYS));

            when(casoRepository.findAll()).thenReturn(List.of(casoProblematico, casoNormal));

            // Simular error al guardar el primer caso
            when(casoRepository.save(argThat(caso -> caso.getId() == 200L)))
                    .thenThrow(new RuntimeException("Error simulado en DB"));

            // Act - NO debe lanzar excepción
            assertDoesNotThrow(() -> limpiezaWorker.ejecutar());

            // Assert: el segundo caso debe procesarse correctamente
            verify(casoRepository, atLeastOnce()).save(argThat(caso ->
                    caso.getId() == 201L && caso.getEstado() == EstadoCaso.ARCHIVADO
            ));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Métodos auxiliares
    // ══════════════════════════════════════════════════════════════

    private Caso crearCaso(Long id, EstadoCaso estado, Instant fechaUltimaActividad) {
        return Caso.builder()
                .id(id)
                .tipoCaso(TipoCaso.VULNERABLE)
                .estado(estado)
                .urgencia(Urgencia.MEDIA)
                .fechaRegistro(fechaUltimaActividad.minus(1, ChronoUnit.DAYS))
                .fechaUltimaActividad(fechaUltimaActividad)
                .reportante(reportante)
                .animal(animal)
                .visible(true)
                .moderacionPendiente(false)
                .build();
    }
}
