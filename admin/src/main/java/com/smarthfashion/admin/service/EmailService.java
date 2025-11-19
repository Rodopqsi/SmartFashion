package com.smarthfashion.admin.service;

import com.smarthfashion.admin.domain.Devolucion;
import com.smarthfashion.admin.domain.Reclamacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender; // may be null when mail is not configured

    @Value("${app.mail.from:no-reply@smartfashion.local}")
    private String from;

    @Value("${app.base-url:https://smartfashion.local}")
    private String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void notifyClaimStatus(Reclamacion c) {
        String subject = "Actualización de Reclamación #" + c.getId() + " (" + safe(c.getOrderNumber()) + ")";
        String body = "Hola,\n\n" +
                "Tu reclamación ha cambiado de estado a: " + safe(c.getEstado()) + "\n\n" +
                (c.getRespuesta() != null && !c.getRespuesta().isBlank() ? ("Respuesta: " + c.getRespuesta() + "\n\n") : "") +
                "Orden: " + safe(c.getOrderNumber()) + "\n" +
                "Tipo: " + safe(c.getTipo()) + "\n\n" +
                "Puedes ver el detalle en tu perfil.\n" + baseUrl + "/perfil" + "\n\n" +
                "Saludos,\nSmartFashion";
        sendSafe(c.getEmail(), subject, body);
    }

    public void notifyReturnStatus(Devolucion d) {
        String subject = "Actualización de Devolución #" + d.getId() + " (" + safe(d.getOrderNumber()) + ")";
        String body = "Hola,\n\n" +
                "Tu solicitud de devolución ha cambiado de estado a: " + safe(d.getEstado()) + "\n\n" +
                "Orden: " + safe(d.getOrderNumber()) + "\n" +
                "Motivo: " + safe(d.getMotivo()) + "\n" +
                "Método: " + safe(d.getMetodo()) + "\n\n" +
                "Puedes ver el detalle en tu perfil.\n" + baseUrl + "/perfil" + "\n\n" +
                "Saludos,\nSmartFashion";
        sendSafe(d.getEmail(), subject, body);
    }

    private void sendSafe(String to, String subject, String text) {
        try {
            if (mailSender == null) {
                log.info("Email no configurado (sin JavaMailSender). Se omite envío a {} con asunto '{}'", to, subject);
                return;
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("Email enviado a {} con asunto '{}'", to, subject);
        } catch (Exception e) {
            log.warn("No se pudo enviar email a {}: {}", to, e.getMessage());
        }
    }

    private static String safe(String s) { return s == null ? "-" : s; }
}
