package co.edu.uniquindio.huellitas.entity;

import co.edu.uniquindio.huellitas.entity.enums.EstadoCaso;
import co.edu.uniquindio.huellitas.entity.enums.TipoCaso;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "casos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCaso tipoCaso;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCaso estado;

    @Column(length = 300)
    private String direccionReferencia;

    @Column(nullable = false)
    private Instant fechaRegistro;

    private Instant fechaUltimaActividad;
    private Instant fechaCustodia;
    private Instant fechaArchivado;

    private boolean visible = true;

    @Column(columnDefinition = "TEXT")
    private String motivoModeracion;

    /** Control de concurrencia optimista. */
    @Version
    private Long version;

    // ── Relaciones ManyToOne ──────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barrio_id")
    private Barrio barrio;

    /** Fundación que tomó custodia del animal (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fundacion_custodio_id")
    private Fundacion fundacionCustodio;

    /** Caso original del que este es duplicado (auto-referencia). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caso_original_id")
    private Caso casoOriginal;

    // ── Composición (owned por Caso) ──────────────────────────────

    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("caso-fotos")
    @Builder.Default
    private List<Foto> fotos = new ArrayList<>();

    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("caso-seguimientos")
    @Builder.Default
    private List<Seguimiento> seguimientos = new ArrayList<>();

    @OneToOne(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("caso-bitacora")
    private BitacoraMedica bitacoraMedica;

    @OneToOne(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("caso-cierre")
    private CierreCaso cierreCaso;
}
