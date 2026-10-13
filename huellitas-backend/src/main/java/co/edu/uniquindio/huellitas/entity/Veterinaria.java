package co.edu.uniquindio.huellitas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "veterinarias")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Veterinaria extends Organizacion {

    @Column(length = 100)
    private String registroProfesional;

    @Column(length = 100)
    private String especialidad;

    @Column(length = 200)
    private String horarioAtencion;
}
