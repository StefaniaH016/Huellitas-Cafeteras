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

    @Column(columnDefinition = "TEXT", updatable = false)
    private String diagnostico;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String procedimiento;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String tratamiento;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String medicamentos;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String evolucion;

    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @Column(updatable = false)
    private Instant fechaFirma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bitacora_id", nullable = false, updatable = false)
    @JsonBackReference("bitacora-registros")
    private BitacoraMedica bitacoraMedica;

    /** Veterinario o usuario que firmó el registro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firmante_id", updatable = false)
    private Usuario firmante;

    @PreUpdate
    public void preUpdate() {
        throw new UnsupportedOperationException("Los registros médicos son inmutables y no se pueden modificar.");
    }
}
