package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.dto.bitacora.*;

import java.util.List;

public interface BitacoraService {
    BitacoraResponseDTO abrirBitacora(Long casoId, AbrirBitacoraDTO dto, String correoVeterinaria);
    BitacoraDetalleDTO obtenerBitacora(Long casoId);
    BitacoraDetalleDTO obtenerBitacora(Long casoId, String correoUsuario);
    RegistroMedicoResponseDTO agregarEvolucion(Long casoId, EvolucionDTO dto, String correoVeterinaria);
    AltaMedicaResponseDTO registrarAlta(Long casoId, AltaMedicaDTO dto, String correoVeterinaria);
    List<PacienteResponseDTO> listarMisPacientes(String correoVeterinaria);
    byte[] generarPdfResumen(Long casoId);
}
