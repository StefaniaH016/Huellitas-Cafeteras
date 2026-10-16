package co.edu.uniquindio.huellitas.entity;

import co.edu.uniquindio.huellitas.entity.enums.Rol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @Builder.Default
    @Column(nullable = false)
    private boolean verificado = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    private boolean disponibleRescate = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean aceptoTratamientoDatos = false;

    private Instant fechaAceptacionDatos;

    /** Organización a la que pertenece (null si es ciudadano independiente). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacion_id")
    private Organizacion organizacion;

    /** Municipio de interés / zona donde actúa el usuario. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_interes_id")
    private Municipio municipioInteres;

    /** Barrio de residencia o referencia del usuario. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barrio_id")
    private Barrio barrio;
}
