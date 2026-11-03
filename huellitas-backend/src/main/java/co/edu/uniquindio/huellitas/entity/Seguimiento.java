package co.edu.uniquindio.huellitas.entity;

import co.edu.uniquindio.huellitas.entity.enums.TipoAporte;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import co.edu.uniquindio.huellitas.entity.Barrio;

import java.time.Instant;

@Entity
@Table(name = "seguimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAporte tipo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(nullable = false)
    private Instant fecha;

    @Builder.Default
    private boolean visible = true;

    // Optional barrio asociado al aporte (sub‑tarea 13.1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barrio_id")
    private Barrio barrio;

    // Motivo de ocultación para moderación (sub‑tarea 13.2)
    @Column(length = 255)
    private String motivoOcultacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caso_id", nullable = false)
    @JsonBackReference("caso-seguimientos")
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;
}
