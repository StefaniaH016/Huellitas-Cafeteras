package co.edu.uniquindio.huellitas.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bitacoras_medicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant fechaApertura;

    private boolean altaMedica = false;

    private Instant fechaAlta;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caso_id", nullable = false, unique = true)
    @JsonBackReference("caso-bitacora")
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinaria_id")
    private Veterinaria veterinaria;

    @OneToMany(mappedBy = "bitacoraMedica", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("bitacora-registros")
    @Builder.Default
    private List<RegistroMedico> registros = new ArrayList<>();
}
