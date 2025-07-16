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
        String link = "http://localhost:4200/#/activate/" + token;
        System.out.println(link);
        // Kreiranje emaila sa HTML sadržajem
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mail = new MimeMessageHelper(mimeMessage, true);

        mail.setTo(client.getEmail());
        mail.setSubject("OnlyBuns App - Aktivacija Naloga");
        mail.setText("<html><body>"
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

    public void sendWeeklyStatsEmail(Client client, int newFollowers, int newLikes, int newPosts) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mail = new MimeMessageHelper(mimeMessage, true);

        mail.setTo(client.getEmail());
        mail.setSubject("OnlyBuns App - Vaša nedeljna aktivnost");
        mail.setText("<html><body>"
                + "<div style='margin-top: 10px;'>"
                + "<div style='margin: 25px;'>"
                + "Dear " + client.getFirstName() + ",<br/><br/>"
                + "Here is a summary of your activity in the last 7 days:<br/><br/>"
                + "<ul>"
                + "<li>New followers: <b>" + newFollowers + "</b></li>"
                + "<li>New likes: <b>" + newLikes + "</b></li>"
                + "<li>New posts: <b>" + newPosts + "</b></li>"
                + "</ul><br/>"
                + "We miss you! Come back and see what's new :)<br/><br/>"
                + "Best regards,<br/>"
                + "<span>OnlyBuns app team</span>"
                + "</div>"
                + "</div>"
                + "</body></html>", true);

        javaMailSender.send(mimeMessage);

        System.out.println("Weekly summary email sent to " + client.getEmail());
    }
}
