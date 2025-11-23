package com.smarthfashion.admin.service;

import com.smarthfashion.admin.domain.Devolucion;
import com.smarthfashion.admin.domain.Orders;
import com.smarthfashion.admin.domain.Reclamacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender; // may be null when mail is not configured
    private final SpringTemplateEngine templateEngine; // may be null when not available

    @Value("${app.mail.from:no-reply@smartfashion.local}")
    private String from;

    @Value("${app.base-url:https://smartfashion.local}")
    private String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        ObjectProvider<SpringTemplateEngine> templateProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.templateEngine = templateProvider.getIfAvailable();
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

    public void sendOrderStatusEmail(Orders order) {
        if (order == null || order.getEmail() == null || order.getEmail().isBlank()) return;
        String to = order.getEmail();
        String subject = "Actualización de estado - Pedido " + order.getOrderNumber();
        try {
            if (mailSender == null) {
                log.info("Email not configured. Skipping HTML email to {}", to);
                // fallback to simple text
                String body = "Hola " + safe(order.getOrderNumber()) + ",\n\nTu pedido ha cambiado de estado a: " + safe(order.getStatus() == null ? "" : order.getStatus().name()) + "\n\n" + baseUrl + "/perfil";
                sendSafe(to, subject, body);
                return;
            }
            if (templateEngine != null) {
                Context ctx = new Context();
                ctx.setVariable("orderNumber", order.getOrderNumber());
                ctx.setVariable("status", order.getStatus() == null ? "" : order.getStatus().name());
                ctx.setVariable("name", safe(order.getOrderNumber()));
                ctx.setVariable("total", order.getTotal());
                String html = templateEngine.process("email/status-update", ctx);
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
                helper.setFrom(from);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(html, true);
                mailSender.send(mime);
                log.info("Sent HTML status email to {}", to);
                return;
            }
            // no template engine -> plain text
            String body = "Hola,\n\nTu pedido " + safe(order.getOrderNumber()) + " ha cambiado de estado a: " + safe(order.getStatus() == null ? "" : order.getStatus().name()) + "\n\n" + baseUrl + "/perfil";
            sendSafe(to, subject, body);
        } catch (Exception e) {
            log.warn("Failed to send order status email to {}: {}", to, e.getMessage());
        }
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
