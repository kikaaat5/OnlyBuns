package com.example.OnlyBuns;
import jakarta.persistence.EntityManager;
import com.example.OnlyBuns.exception.ResourceNotFoundException;
import com.example.OnlyBuns.model.Address;
import com.example.OnlyBuns.model.Client;
import com.example.OnlyBuns.repository.AddressRepository;
import com.example.OnlyBuns.repository.ClientRepository;
import com.example.OnlyBuns.repository.FollowRelationRepository;
import com.example.OnlyBuns.service.FollowRelationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import com.example.OnlyBuns.repository.ChatMessageRepository;
import com.example.OnlyBuns.repository.PostRepository;
import com.example.OnlyBuns.repository.RoleRepository;
import com.example.OnlyBuns.repository.CommentRepository;
import com.example.OnlyBuns.repository.ChatRoomRepository;
import com.example.OnlyBuns.repository.ChatRoomMemberRepository;
import com.example.OnlyBuns.repository.AdministratorRepository;
import com.example.OnlyBuns.model.User;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FollowRelationServiceIntegrationTest {

    @Autowired
    private FollowRelationService followRelationService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private FollowRelationRepository followRelationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired(required = false)
    private ChatRoomRepository chatRoomRepository;
    @Autowired(required = false)
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Autowired(required = false)
    private AdministratorRepository administratorRepository;

    @Autowired
    private EntityManager entityManager;

    private Client clientA;
    private Client clientB;
    private Client clientC;

    @BeforeEach
    @Transactional
    void setUp() {

        if (commentRepository != null) commentRepository.deleteAllInBatch();
        if (chatMessageRepository != null) chatMessageRepository.deleteAllInBatch();
        followRelationRepository.deleteAllInBatch();
        if (chatRoomMemberRepository != null) chatRoomMemberRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();

        if (chatRoomRepository != null) chatRoomRepository.deleteAllInBatch();

        clientRepository.deleteAllInBatch();
        if (administratorRepository != null) administratorRepository.deleteAllInBatch();

        if (roleRepository != null) roleRepository.deleteAllInBatch();
        addressRepository.deleteAllInBatch();

        Address addressA = new Address();
        addressA.setCity("Test Grad A");
        addressA.setCountry("Test Država A");
        addressA.setPostalCode(10001);
        addressA.setStreet("Test Ulica 1");

        clientA = new Client();
        clientA.setActive(true);
        clientA.setEnabled(true);
        clientA.setEmail("clientA@example.com");
        clientA.setUsername("userA");
        clientA.setPassword("passwordA");
        clientA.setFirstName("ImeA");
        clientA.setLastName("PrezimeA");
        clientA.setFollowing(0);
        clientA.setFollowers(0);
        clientA.setNumberOfPosts(0);
        clientA.setAddress(addressA);
        clientA.setLastPasswordResetDate(Timestamp.valueOf(LocalDateTime.now()));
        clientA = clientRepository.save(clientA);

        Address addressB = new Address();
        addressB.setCity("Test Grad B");
        addressB.setCountry("Test Država B");
        addressB.setPostalCode(10002);
        addressB.setStreet("Test Ulica 2");

        clientB = new Client();
        clientB.setActive(true);
        clientB.setEnabled(true);
        clientB.setEmail("clientB@example.com");
        clientB.setUsername("userB");
        clientB.setPassword("passwordB");
        clientB.setFirstName("ImeB");
        clientB.setLastName("PrezimeB");
        clientB.setFollowing(0);
        clientB.setFollowers(0);
        clientB.setNumberOfPosts(0);
        clientB.setAddress(addressB);
        clientB.setLastPasswordResetDate(Timestamp.valueOf(LocalDateTime.now()));
        clientB = clientRepository.save(clientB);

        Address addressC = new Address();
        addressC.setCity("Test Grad C");
        addressC.setCountry("Test Država C");
        addressC.setPostalCode(10003);
        addressC.setStreet("Test Ulica 3");

        clientC = new Client();
        clientC.setActive(true);
        clientC.setEnabled(true);
        clientC.setEmail("clientC@example.com");
        clientC.setUsername("userC");
        clientC.setPassword("passwordC");
        clientC.setFirstName("ImeC");
        clientC.setLastName("PrezimeC");
        clientC.setFollowing(0);
        clientC.setFollowers(0);
        clientC.setNumberOfPosts(0);
        clientC.setAddress(addressC);
        clientC.setLastPasswordResetDate(Timestamp.valueOf(LocalDateTime.now()));
        clientC = clientRepository.save(clientC);

        clientRepository.flush();
    }


    @Test
    void testConcurrentFollowAttemptsToSameClient() throws InterruptedException, ExecutionException {
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Callable<Void> clientAFollowsC = () -> {
            try {
                latch.countDown();
                latch.await();
                followRelationService.followClient(Math.toIntExact(clientA.getId()), Math.toIntExact(clientC.getId()));
                System.out.println("Client A successfully followed Client C.");
            } catch (IllegalStateException e) {
                System.out.println("Client A caught an expected error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Client A encountered an unexpected error: " + e.getMessage());
                throw e;
            }
            return null;
        };

        Callable<Void> clientBFollowsC = () -> {
            try {
                latch.countDown();
                latch.await();
                followRelationService.followClient(Math.toIntExact(clientB.getId()), Math.toIntExact(clientC.getId()));
                System.out.println("Client B successfully followed Client C.");
            } catch (IllegalStateException e) {
                System.out.println("Client B caught an expected error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Client B encountered an unexpected error: " + e.getMessage());
                throw e;
            }
            return null;
        };

        Future<Void> futureA = executorService.submit(clientAFollowsC);
        Future<Void> futureB = executorService.submit(clientBFollowsC);

        futureA.get();
        futureB.get();

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate in time.");

        Client updatedClientA = clientRepository.findById(Math.toIntExact(clientA.getId())).orElseThrow();
        Client updatedClientB = clientRepository.findById(Math.toIntExact(clientB.getId())).orElseThrow();
        Client updatedClientC = clientRepository.findById(Math.toIntExact(clientC.getId())).orElseThrow();

        assertEquals(1, updatedClientA.getFollowing(), "Client A should be following 1 client.");
        assertEquals(1, updatedClientB.getFollowing(), "Client B should be following 1 client.");

        assertEquals(2, updatedClientC.getFollowers(), "Client C should have 2 followers.");

        long followRelationsCount = followRelationRepository.count();
        assertEquals(2, followRelationsCount, "There should be exactly 2 follow relations created.");
    }

    @AfterEach
    @Transactional
    void tearDown() {

        if (commentRepository != null) {
            commentRepository.deleteAllInBatch();
        }

        if (chatMessageRepository != null) {
            chatMessageRepository.deleteAllInBatch();
        }
        if (chatRoomMemberRepository != null) {
            chatRoomMemberRepository.deleteAllInBatch();
        }

        followRelationRepository.deleteAllInBatch();


        if (postRepository != null) {
            postRepository.deleteAllInBatch();
        }

        if (chatRoomRepository != null) {
            chatRoomRepository.deleteAllInBatch();
        }

        clientRepository.deleteAllInBatch();
        if (administratorRepository != null) {
            administratorRepository.deleteAllInBatch();
        }

        if (roleRepository != null) {
            roleRepository.deleteAllInBatch();
        }

        addressRepository.deleteAllInBatch();
    }



    @Test
    void testConcurrentFollowAndUnfollowOnSameClient() throws InterruptedException, ExecutionException {
        // Postavi početno stanje: clientA već prati clientC
        followRelationService.followClient(Math.toIntExact(clientA.getId()), Math.toIntExact(clientC.getId()));

        // Proveri da li je početno stanje ispravno postavljeno
        Client initialClientA = clientRepository.findById(Math.toIntExact(clientA.getId())).orElseThrow();
        Client initialClientC = clientRepository.findById(Math.toIntExact(clientC.getId())).orElseThrow();
        assertEquals(1, initialClientA.getFollowing());
        assertEquals(1, initialClientC.getFollowers());
        assertTrue(followRelationService.isFollowing(Math.toIntExact(clientA.getId()), Math.toIntExact(clientC.getId())));


        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Zadatak za clientB da prati clientC
        Callable<Void> followTask = () -> {
            try {
                latch.countDown();
                latch.await();
                followRelationService.followClient(Math.toIntExact(clientB.getId()), Math.toIntExact(clientC.getId()));
                System.out.println("Client B successfully followed Client C in concurrent test.");
            } catch (IllegalStateException e) {
                System.out.println("Follow task for Client B caught an expected error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Follow task for Client B encountered an unexpected error: " + e.getMessage());
                throw e;
            }
            return null;
        };

        // Zadatak za clientA da otprati clientC
        Callable<Void> unfollowTask = () -> {
            try {
                latch.countDown();
                latch.await();
                followRelationService.unfollowClient(Math.toIntExact(clientA.getId()), Math.toIntExact(clientC.getId()));
                System.out.println("Client A successfully unfollowed Client C in concurrent test.");
            } catch (ResourceNotFoundException e) {
                // Ovo se može desiti ako je operacija praćenja već poništena, mada je manje verovatno sa zaključavanjem
                System.out.println("Unfollow task for Client A caught an expected error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unfollow task for Client A encountered an unexpected error: " + e.getMessage());
                throw e;
            }
            return null;
        };

        Future<Void> futureFollow = executorService.submit(followTask);
        Future<Void> futureUnfollow = executorService.submit(unfollowTask);

        // Sačekaj da se oba zadatka završe
        futureFollow.get();
        futureUnfollow.get();

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate in time.");

        // Provera konačnog stanja
        Client updatedClientA = clientRepository.findById(Math.toIntExact(clientA.getId())).orElseThrow();
        Client updatedClientB = clientRepository.findById(Math.toIntExact(clientB.getId())).orElseThrow();
        Client updatedClientC = clientRepository.findById(Math.toIntExact(clientC.getId())).orElseThrow();

        // clientA je otpratio clientC, pa njegov 'following' brojač treba biti 0
        assertEquals(0, updatedClientA.getFollowing(), "Client A should no longer be following any client.");

        // clientB je zapratio clientC, pa njegov 'following' brojač treba biti 1
        assertEquals(1, updatedClientB.getFollowing(), "Client B should be following 1 client.");

        // clientC: Počeo je sa 1 pratiocem (clientA). clientA ga je otpratio (-1), a clientB ga je zapratio (+1).
        // Stoga, konačan broj pratilaca za clientC treba biti 1.
        assertEquals(1, updatedClientC.getFollowers(), "Client C should have 1 follower remaining.");

        // Proveri broj FollowRelation entiteta: trebala bi postojati samo veza clientB -> clientC
        long followRelationsCount = followRelationRepository.count();
        assertEquals(1, followRelationsCount, "There should be exactly 1 follow relation remaining.");
        assertTrue(followRelationService.isFollowing(Math.toIntExact(clientB.getId()), Math.toIntExact(clientC.getId())), "Client B should still be following Client C.");
        assertFalse(followRelationService.isFollowing(Math.toIntExact(clientA.getId()), Math.toIntExact(clientC.getId())), "Client A should no longer be following Client C.");
    }
}