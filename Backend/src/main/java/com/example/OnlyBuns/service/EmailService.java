package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TokenUtils tokenUtils;

    public void sendRegistrationActivation(Client client) throws MessagingException {
        // Priprema linka za aktivaciju
        String token = tokenUtils.generateToken(client.getEmail());
        String link = "http://localhost:4000/#/activate/" + token;

        // Kreiranje emaila sa HTML sadržajem
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mail = new MimeMessageHelper(mimeMessage, true);

        mail.setTo(client.getEmail());
        mail.setSubject("OnlyBuns App - Aktivacija Naloga");
        mail.setText("<html><body>"
                + "<div style='margin: 50px;'>"
                + "<div style='background-color: rgb(99, 216, 99); height: 55px;'>"
                + "<h1 style='margin-left: 15px; color: white;'>Successful</h1>"
                + "</div>"
                + "<div style='margin-top: 10px;'>"
                + "<div style='margin: 25px;'>"
                + "Dear " + client.getFirstName() + ",<br/><br/>"
                + "Click the link below to activate your account:<br/><br/>"
                + "<a href='" + link + "'>" + link + "</a><br/><br/>"
                + "Regards,<br/>"
                + "<span>OnlyBuns app team</span>"
                + "</div>"
                + "</div>"
                + "</div>"
                + "</body></html>", true); // true omogućava slanje HTML sadržaja

        // Slanje emaila
        javaMailSender.send(mimeMessage);

        // Logovanje uspešnog slanja
        System.out.println("Email sent successfully");
    }
}
