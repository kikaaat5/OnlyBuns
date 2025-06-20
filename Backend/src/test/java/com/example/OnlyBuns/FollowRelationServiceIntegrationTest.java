package com.example.OnlyBuns;

// Importi za JUnit 4
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// Spring Boot test importi
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.dao.DataIntegrityViolationException; // Dodato za potencijalne greške
import org.springframework.mail.javamail.JavaMailSender; // Za mockovanje

// Tvoji modeli, repozitorijumi i servisi
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.model.Address;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.repository.FollowRelationRepository;
import com.example.OnlyBuns.service.FollowRelationService;
import com.example.OnlyBuns.exception.ResourceNotFoundException;
import com.example.OnlyBuns.dto.UserRequest; // Dodato, iako se ne koristi direktno u ovom testu,
// UserServiceTests ga koristi.

// Java konkurentnost
import java.sql.Timestamp;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

// JUnit Assertions
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail; // Za fail() metodu

@RunWith(SpringRunner.class) // Koristi SpringRunner za JUnit 4 integracione testove
@SpringBootTest // Podiže ceo Spring Boot ApplicationContext
// Bez @ActiveProfiles("test") ili specifičnih properties fajlova u test/resources,
// oslanjamo se na glavni application.properties (npr. ddl-auto=create-drop)
// i kako Spring i Hibernate automatski konfigurišu H2 bazu.
public class FollowRelationServiceIntegrationTest {

    @Autowired
    private FollowRelationService followRelationService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FollowRelationRepository followRelationRepository;

    // VAŽNO: Mock-ujemo JavaMailSender i Address.
    // Ovo je ključno za podizanje Spring konteksta bez grešaka zavisnosti,
    // jer tvoja aplikacija verovatno ima autowired EmailService (koji koristi JavaMailSender)
    // i Address je povezan sa User/Client entitetima.
    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private Address address;

    @Before
    public void setUp() {
        // Ovaj metod je sada PRAZAN, kao u tvom UserServiceTests.
        // Oslanjamo se na spring.jpa.hibernate.ddl-auto=create-drop
        // (ili sličnu konfiguraciju iz tvog glavnog application.properties)
        // da automatski očisti i ponovo kreira šemu baze podataka pre svakog testa.
        System.out.println("Globalni setUp() je pokrenut (prazan).");
    }

    @Test
    public void testConcurrentFollows_ShouldIncrementFollowersCorrectly() throws InterruptedException {
        // Uvek obrišemo podatke specifične za ovaj test na početku
        // kako bismo osigurali čisto stanje i izolaciju testa.
        // Ovo je ključno jer @SpringBootTest podiže kontekst jednom,
        // a ddl-auto=create-drop čisti bazu samo prilikom podizanja/gašenja konteksta,
        // ne između individualnih @Test metoda unutar iste klase.
        followRelationRepository.deleteAllInBatch();
        clientRepository.deleteAllInBatch();

        // Podaci se kreiraju OVDE, unutar samog test metoda.
        // Važno: Koristimo UNIKATNE username-ove i email-ove za svaki test metod
        // kako bismo izbegli UNIQUE constraint greške ukoliko ddl-auto=create-drop
        // ne uspe u potpunosti da očisti sekvence ili ako kontekst nije potpuno nov.
        Client follower1 = new Client(
                null,                           // id - baza će generisati
                "f1_concurrent_follows@test.com",       // email (unikatan)
                "follower1_user_concurrent_follows",    // username (unikatan)
                "pass123",                      // password
                "Follower",                     // name
                "One",                          // surname
                0,                              // numberOfPosts
                0,                              // following
                true                            // active
        );
        follower1.setFollowers(0);
        follower1.setEnabled(true);
        follower1.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        follower1.setAddress(null);
        follower1.setRoles(Collections.emptyList());
        follower1 = clientRepository.save(follower1);

        Client follower2 = new Client(
                null,
                "f2_concurrent_follows@test.com",
                "follower2_user_concurrent_follows",
                "pass123",
                "Follower",
                "Two",
                0,
                0,
                true
        );
        follower2.setFollowers(0);
        follower2.setEnabled(true);
        follower2.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        follower2.setAddress(null);
        follower2.setRoles(Collections.emptyList());
        follower2 = clientRepository.save(follower2);

        Client targetUser = new Client(
                null,
                "target_concurrent_follows@test.com",
                "target_user_concurrent_follows",
                "pass123",
                "Target",
                "User",
                0,
                0,
                true
        );
        targetUser.setFollowers(0);
        targetUser.setEnabled(true);
        targetUser.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        targetUser.setAddress(null);
        targetUser.setRoles(Collections.emptyList());
        targetUser = clientRepository.save(targetUser);

        System.out.println("Setup završen za testConcurrentFollows. ID-jevi: Follower1=" + follower1.getId() + ", Follower2=" + follower2.getId() + ", TargetUser=" + targetUser.getId());


        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        System.out.println("Pokreće se test: Konkurentno praćenje i provera brojača.");

        // ZADATAK 1: follower1 prati targetUser-a.
        Client finalFollower = follower1;
        Client finalTargetUser1 = targetUser;
        executorService.submit(() -> {
            try {
                // Namerno kašnjenje za simulaciju konkurentnosti (samo za testiranje, ne za produkciju!)
                Thread.sleep(100);
                // Math.toIntExact() je potreban JER Client.getId() vraća Long (od Usera),
                // a servis prima Integer.
                followRelationService.followClient(Math.toIntExact(finalFollower.getId()), Math.toIntExact(finalTargetUser1.getId()));
            } catch (IllegalStateException e) {
                System.out.println("Očekivana IllegalStateException u niti 1 (već prati): " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Neočekivana greška u niti 1: " + e.getMessage());
                // Rethrow kao RuntimeException da se vidi u test reportu
                throw new RuntimeException("Test nit je pala sa neočekivanom greškom", e);
            } finally {
                latch.countDown();
            }
        });

        // ZADATAK 2: follower2 prati targetUser-a.
        Client finalFollower3 = follower2;
        Client finalTargetUser2 = targetUser;
        executorService.submit(() -> {
            try {
                // Namerno kašnjenje za simulaciju konkurentnosti (samo za testiranje, ne za produkciju!)
                Thread.sleep(100);
                // Math.toIntExact() je potreban JER Client.getId() vraća Long (od Usera),
                // a servis prima Integer.
                followRelationService.followClient(Math.toIntExact(finalFollower3.getId()), Math.toIntExact(finalTargetUser2.getId()));
            } catch (IllegalStateException e) {
                System.out.println("Očekivana IllegalStateException u niti 2 (već prati): " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Neočekivana greška u niti 2: " + e.getMessage());
                // Rethrow kao RuntimeException da se vidi u test reportu
                throw new RuntimeException("Test nit je pala sa neočekivanom greškom", e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue("Test je istekao, niti nisu završile na vreme.", latch.await(10, TimeUnit.SECONDS));
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Ponovo učitavamo korisnike iz baze da dobijemo najsvežije podatke.
        Client finalTargetUser = clientRepository.findById(Math.toIntExact(targetUser.getId())).orElseThrow();
        Client finalFollower1 = clientRepository.findById(Math.toIntExact(follower1.getId())).orElseThrow();
        Client finalFollower2 = clientRepository.findById(Math.toIntExact(follower2.getId())).orElseThrow();

        assertEquals(2, finalTargetUser.getFollowers());
        assertEquals(0, finalTargetUser.getFollowing());
        assertEquals(1, finalFollower1.getFollowing());
        assertEquals(1, finalFollower2.getFollowing());

        assertTrue("Follower1 treba da prati TargetUsera.", followRelationService.isFollowing(Math.toIntExact(follower1.getId()), Math.toIntExact(targetUser.getId())));
        assertTrue("Follower2 treba da prati TargetUsera.", followRelationService.isFollowing(Math.toIntExact(follower2.getId()), Math.toIntExact(targetUser.getId())));
        assertEquals(2L, followRelationRepository.count());
    }

    @Test
    public void testConcurrentUnfollows_ShouldDecrementFollowersCorrectly() throws InterruptedException {
        // Uvek obrišemo podatke specifične za ovaj test na početku da osiguramo čisto stanje.
        followRelationRepository.deleteAllInBatch();
        clientRepository.deleteAllInBatch();

        // Podaci se kreiraju OVDE, unutar samog test metoda.
        Client follower1 = new Client(
                null, "f1_unfollow@test.com", "f1_unfollow_user", "pass123", "Follower", "One", 0, 0, true);
        follower1.setFollowers(0); follower1.setEnabled(true); follower1.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        follower1.setAddress(null); follower1.setRoles(Collections.emptyList());
        follower1 = clientRepository.save(follower1);

        Client follower2 = new Client(
                null, "f2_unfollow@test.com", "f2_unfollow_user", "pass123", "Follower", "Two", 0, 0, true);
        follower2.setFollowers(0); follower2.setEnabled(true); follower2.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        follower2.setAddress(null); follower2.setRoles(Collections.emptyList());
        follower2 = clientRepository.save(follower2);

        Client targetUser = new Client(
                null, "target_unfollow@test.com", "target_unfollow_user", "pass123", "Target", "User", 0, 0, true);
        targetUser.setFollowers(0); targetUser.setEnabled(true); targetUser.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        targetUser.setAddress(null); targetUser.setRoles(Collections.emptyList());
        targetUser = clientRepository.save(targetUser);

        // Inicijalno stanje: Podesi da follower1 i follower2 prate targetUsera
        followRelationService.followClient(Math.toIntExact(follower1.getId()), Math.toIntExact(targetUser.getId()));
        followRelationService.followClient(Math.toIntExact(follower2.getId()), Math.toIntExact(targetUser.getId()));

        Client initialTargetUser = clientRepository.findById(Math.toIntExact(targetUser.getId())).orElseThrow();
        assertEquals(2, initialTargetUser.getFollowers());
        assertEquals(2L, followRelationRepository.count());

        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        System.out.println("Pokreće se test: Konkurentno otpraćivanje i provera brojača.");

        // ZADATAK 1: follower1 otprati targetUser-a.
        Client finalFollower = follower1;
        Client finalTargetUser1 = targetUser;
        executorService.submit(() -> {
            try {
                // Namerno kašnjenje za simulaciju konkurentnosti (samo za testiranje, ne za produkciju!)
                Thread.sleep(100);
                followRelationService.unfollowClient(Math.toIntExact(finalFollower.getId()), Math.toIntExact(finalTargetUser1.getId()));
            } catch (ResourceNotFoundException e) {
                System.out.println("Očekivana ResourceNotFoundException u niti 1 (već otpraćeno): " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Neočekivana greška u niti 1: " + e.getMessage());
                throw new RuntimeException("Test nit je pala sa neočekivanom greškom", e);
            } finally {
                latch.countDown();
            }
        });

        // ZADATAK 2: follower2 otprati targetUser-a.
        Client finalFollower3 = follower2;
        Client finalTargetUser2 = targetUser;
        executorService.submit(() -> {
            try {
                // Namerno kašnjenje za simulaciju konkurentnosti (samo za testiranje, ne za produkciju!)
                Thread.sleep(100);
                followRelationService.unfollowClient(Math.toIntExact(finalFollower3.getId()), Math.toIntExact(finalTargetUser2.getId()));
            } catch (ResourceNotFoundException e) {
                System.out.println("Očekivana ResourceNotFoundException u niti 2 (već otpraćeno): " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Neočekivana greška u niti 2: " + e.getMessage());
                throw new RuntimeException("Test nit je pala sa neočekivanom greškom", e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue("Test je istekao, niti nisu završile na vreme.", latch.await(10, TimeUnit.SECONDS));
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        Client finalFollower1 = clientRepository.findById(Math.toIntExact(follower1.getId())).orElseThrow();
        Client finalFollower2 = clientRepository.findById(Math.toIntExact(follower2.getId())).orElseThrow();
        Client finalTargetUser = clientRepository.findById(Math.toIntExact(targetUser.getId())).orElseThrow();

        assertEquals(0, finalTargetUser.getFollowers());
        assertEquals(0, finalTargetUser.getFollowing());
        assertEquals(0, finalFollower1.getFollowing());
        assertEquals(0, finalFollower2.getFollowing());
        assertEquals(0L, followRelationRepository.count());
    }

    // Test za duple pokušaje praćenja (slično tvojem UserServiceTests)
    @Test(expected = IllegalStateException.class) // Očekujemo IllegalStateException iz servisa
    public void testConcurrentFollows_ShouldHandleDuplicateAttempts() throws Throwable {
        // Uvek obrišemo podatke specifične za ovaj test na početku da osiguramo čisto stanje.
        followRelationRepository.deleteAllInBatch();
        clientRepository.deleteAllInBatch();

        // Podaci se kreiraju OVDE, unutar samog test metoda.
        Client follower1 = new Client(
                null, "f1_duplicate@test.com", "f1_duplicate_user", "pass123", "Follower", "One", 0, 0, true);
        follower1.setFollowers(0); follower1.setEnabled(true); follower1.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        follower1.setAddress(null); follower1.setRoles(Collections.emptyList());
        follower1 = clientRepository.save(follower1);

        Client targetUser = new Client(
                null, "target_duplicate@test.com", "target_duplicate_user", "pass123", "Target", "User", 0, 0, true);
        targetUser.setFollowers(0); targetUser.setEnabled(true); targetUser.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));
        targetUser.setAddress(null); targetUser.setRoles(Collections.emptyList());
        targetUser = clientRepository.save(targetUser);

        // Inicijalno stanje: follower1 već prati targetUsera
        followRelationService.followClient(Math.toIntExact(follower1.getId()), Math.toIntExact(targetUser.getId()));

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        System.out.println("Pokreće se test: Konkurentni dupli pokušaji praćenja.");

        // Zadatak 1: follower1 ponovo pokušava da prati targetUsera
        Client finalTargetUser1 = targetUser;
        Client finalFollower1 = follower1;
        Future<?> future1 = executorService.submit(() -> {
            System.out.println("Nit 1 pokušava drugi put da prati TargetUsera.");
            try {
                followRelationService.followClient(Math.toIntExact(finalFollower1.getId()), Math.toIntExact(finalTargetUser1.getId()));
                // Ako ova linija prođe, znači da test nije bacio očekivani izuzetak
                return null;
            } catch (IllegalStateException e) {
                System.out.println("Nit 1 uhvatila očekivani IllegalStateException: " + e.getMessage());
                // Rethrow izuzetak da bi ga future.get() uhvatio
                throw e;
            } catch (Exception e) {
                System.err.println("Neočekivana greška u niti 1: " + e.getMessage());
                throw new RuntimeException("Test nit je pala sa neočekivanom greškom", e);
            } finally {
                latch.countDown();
            }
        });

        // Zadatak 2: Drugi nebitan korisnik (ili isti) ponovo pokušava da prati istog
        Client finalFollower = follower1;
        Client finalTargetUser = targetUser;
        Future<?> future2 = executorService.submit(() -> {
            System.out.println("Nit 2 pokušava drugi put da prati TargetUsera.");
            try {
                followRelationService.followClient(Math.toIntExact(finalFollower.getId()), Math.toIntExact(finalTargetUser.getId()));
                // Ako ova linija prođe, znači da test nije bacio očekivani izuzetak
                return null;
            } catch (IllegalStateException e) {
                System.out.println("Nit 2 uhvatila očekivani IllegalStateException: " + e.getMessage());
                // Rethrow izuzetak da bi ga future.get() uhvatio
                throw e;
            } catch (Exception e) {
                System.err.println("Neočekivana greška u niti 2: " + e.getMessage());
                throw new RuntimeException("Test nit je pala sa neočekivanom greškom", e);
            } finally {
                latch.countDown();
            }
        });

        try {
            // Čekamo da se oba zadatka završe.
            // Važno je uhvatiti ExecutionException jer ona obavija stvarni izuzetak
            // koji se desio u niti (u ovom slučaju IllegalStateException).
            future1.get();
            future2.get();
            // Ako obe linije prođu bez izuzetka, to znači da servis nije bacio IllegalStateException
            fail("Očekivao se IllegalStateException (klijent već prati), ali se nije desio.");
        } catch (ExecutionException e) {
            // Uhvatili smo ExecutionException. Sada izvlačimo stvarni uzrok (getCause()).
            System.out.println("Uhvaćen izuzetak iz niti: " + e.getCause().getClass().getName());
            // Ako je stvarni uzrok IllegalStateException, prosleđujemo ga dalje.
            if (e.getCause() instanceof IllegalStateException) {
                throw e.getCause(); // Ovo će zadovoljiti @Test(expected = IllegalStateException.class)
            } else {
                // Ako se desi neki drugi, neočekivani izuzetak, bacamo ga kao RuntimeException.
                System.err.println("Neočekivan izuzetak tokom konkurentne operacije.");
                throw new RuntimeException("Neočekivan izuzetak tokom konkurentne operacije.", e.getCause());
            }
        } finally {
            executorService.shutdown(); // Uvek ugasi ExecutorService
            // Opciono: Čekaj da se ExecutorService potpuno ugasi
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("ExecutorService nije završio u roku od 5 sekundi.");
            }
        }
    }
}
