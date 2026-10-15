package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.bitacora.*;

public interface BitacoraService {
    BitacoraResponseDTO abrirBitacora(Long casoId, AbrirBitacoraDTO dto, String correoVeterinaria);
    BitacoraDetalleDTO obtenerBitacora(Long casoId);
    RegistroMedicoResponseDTO agregarEvolucion(Long casoId, EvolucionDTO dto, String correoVeterinaria);
    AltaMedicaResponseDTO registrarAlta(Long casoId, AltaMedicaDTO dto, String correoVeterinaria);
    byte[] generarPdfResumen(Long casoId);
}
