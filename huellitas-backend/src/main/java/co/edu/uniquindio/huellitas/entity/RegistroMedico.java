package co.edu.uniquindio.huellitas.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "registros_medicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String procedimiento;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Column(columnDefinition = "TEXT")
    private String evolucion;

    @Column(nullable = false)
    private Instant fecha;

    private Instant fechaFirma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bitacora_id", nullable = false)
    @JsonBackReference("bitacora-registros")
    private BitacoraMedica bitacoraMedica;

    /** Veterinario o usuario que firmó el registro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firmante_id")
    private Usuario firmante;
}
