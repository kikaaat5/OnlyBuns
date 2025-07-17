package com.example.OnlyBuns.scheduling;

import com.example.OnlyBuns.dto.WeeklyStatsDto;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.service.ClientService;
import com.example.OnlyBuns.service.EmailService;
import com.example.OnlyBuns.service.StatsService;
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
    private StatsService statsService;

    @Autowired
    private EmailService emailService;

    // Izvršava se svaki dan u 2:00 ujutru
    @Scheduled(cron = "0 50 21 * * ?")
    public void notifyInactiveClients() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        System.err.println("📺usao u metodu za slanje mejlova ") ;

        List<Client> inactiveClients = clientService.findClientsNotLoggedInSince(sevenDaysAgo);

        for (Client client : inactiveClients) {
            try {
                WeeklyStatsDto stats = statsService.getStatsForClientLast7Days(client);
                System.out.println("✅ USPESNO slanje emaila korisniku: " + client.getEmail());

                emailService.sendWeeklyStatsEmail(client);
            } catch (MessagingException e) {
                System.err.println("❌ Neuspešno slanje emaila korisniku: " + client.getEmail());
                e.printStackTrace();
            }
        }
    }
}
