package co.edu.uniquindio.huellitas.service;

import co.edu.uniquindio.huellitas.entity.Animal;
import co.edu.uniquindio.huellitas.entity.Barrio;
import co.edu.uniquindio.huellitas.entity.Caso;
import co.edu.uniquindio.huellitas.entity.enums.Especie;
import co.edu.uniquindio.huellitas.entity.enums.Urgencia;
import co.edu.uniquindio.huellitas.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceImplTest {

    @Test
    void emergencyEmailUsesConfiguredVerifiedSenderInsteadOfSmtpLogin() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailServiceImpl emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "correoRemitente", "verified@example.test");

        Caso caso = Caso.builder()
                .id(12L)
                .urgencia(Urgencia.ALTA)
                .descripcion("Caso de prueba")
                .animal(Animal.builder().nombre("Luna").especie(Especie.PERRO).build())
                .barrio(Barrio.builder().nombre("Centro").build())
                .build();

        emailService.enviarCorreoEmergencia(caso, "rescatista@example.test");

        org.mockito.ArgumentCaptor<SimpleMailMessage> mensajeCaptor =
                org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mensajeCaptor.capture());
        assertEquals("verified@example.test", mensajeCaptor.getValue().getFrom());
    }
}
