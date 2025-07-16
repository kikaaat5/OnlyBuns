package com.example.OnlyBuns.scheduling;

import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ClientService;
import com.example.OnlyBuns.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class InactiveUserNotifier {

    @Autowired
    private ClientService clientService;

    @Autowired
    private EmailService emailService;

    // Svaki dan u 2:00 ujutru
    @Scheduled(cron = "0 0 2 * * ?")
    public void notifyInactiveUsers() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Timestamp limit = Timestamp.valueOf(sevenDaysAgo);

        List<Client> inactiveClients = clientService.findClientsByLastLoginBefore(limit);

        for (Client client : inactiveClients) {
            try {
                // Statistika za sad može biti hardkodovana
                emailService.sendWeeklyStatsEmail(client, 3, 12, 5); // npr. 3 nova pratioca, 12 lajkova, 5 objava
            } catch (MessagingException e) {
                System.out.println("Failed to send email to " + client.getEmail());
            }
        }
    }
}
