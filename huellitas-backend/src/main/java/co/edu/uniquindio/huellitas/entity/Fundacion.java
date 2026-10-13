package co.edu.uniquindio.huellitas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "fundaciones")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Fundacion extends Organizacion {

    private int capacidadAlbergue;

    private boolean aceptaCustodia;
}
