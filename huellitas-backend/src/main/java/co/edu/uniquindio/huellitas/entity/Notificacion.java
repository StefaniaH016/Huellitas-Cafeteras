package co.edu.uniquindio.huellitas.entity;

import co.edu.uniquindio.huellitas.entity.enums.TipoNotificacion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoNotificacion tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    private boolean leida = false;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Caso relacionado con la notificación (puede ser null). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caso_id")
    private Caso caso;
}
