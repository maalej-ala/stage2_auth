package stage.authentification.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationPendingEmail(String to, String firstName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Inscription en attente d'activation");
        helper.setFrom("no-reply@yourdomain.com");

        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Bonjour, %s !</h2>
                    <p>Merci de vous être inscrit sur notre plateforme.</p>
                    <p>Votre compte a été créé avec succès, mais il doit être activé par un administrateur avant que vous puissiez vous connecter.</p>
                    <p>Vous recevrez un email de confirmation une fois votre compte activé.</p>
                    <p>Si vous avez des questions, contactez-nous à <a href="mailto:support@yourdomain.com">support@yourdomain.com</a>.</p>
                    <p>Cordialement,<br>L'équipe de la plateforme</p>
                </body>
            </html>
            """.formatted(firstName);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendAccountActivatedEmail(String to, String firstName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Votre compte est maintenant activé");
        helper.setFrom("no-reply@yourdomain.com");

        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Bonjour, %s !</h2>
                    <p>Bonne nouvelle ! Votre compte a été activé par un administrateur.</p>
                    <p>Vous pouvez maintenant vous connecter et explorer nos services.</p>
                    <p><a href="http://localhost:4200/login" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Se connecter</a></p>
                    <p>Si vous avez des questions, contactez-nous à <a href="mailto:support@yourdomain.com">support@yourdomain.com</a>.</p>
                    <p>Cordialement,<br>L'équipe de la plateforme</p>
                </body>
            </html>
            """.formatted(firstName);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendAccountDeactivatedEmail(String to, String firstName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Votre compte a été désactivé");
        helper.setFrom("no-reply@yourdomain.com");

        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Bonjour, %s !</h2>
                    <p>Votre compte a été désactivé par un administrateur.</p>
                    <p>Vous ne pouvez plus vous connecter à la plateforme. Pour plus d'informations ou pour demander une réactivation, veuillez nous contacter.</p>
                    <p><a href="mailto:support@yourdomain.com" style="background-color: #D32F2F; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Contacter le support</a></p>
                    <p>Cordialement,<br>L'équipe de la plateforme</p>
                </body>
            </html>
            """.formatted(firstName);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}