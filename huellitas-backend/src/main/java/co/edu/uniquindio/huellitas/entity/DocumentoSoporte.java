package co.edu.uniquindio.huellitas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documentos_soporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombreArchivo;

    @Column(nullable = false, length = 100)
    private String tipoContenido;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] contenido;
}
