package co.edu.uniquindio.huellitas.controller;

import co.edu.uniquindio.huellitas.dto.imagen.ImagenResponseDTO;
import co.edu.uniquindio.huellitas.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImagenResponseDTO> subirImagen(
            @RequestParam(value = "archivo", required = false) MultipartFile archivo,
            @RequestParam(value = "casoId", required = false) Long casoId) {
        ImagenResponseDTO response = cloudinaryService.subirImagen(archivo, casoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
