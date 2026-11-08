package co.edu.uniquindio.huellitas.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fotos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 200)
    private String publicId;

    @Builder.Default
    private boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caso_id", nullable = false)
    @JsonBackReference("caso-fotos")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguimiento_id")
    @JsonBackReference("seguimiento-fotos")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Seguimiento seguimiento;
}
