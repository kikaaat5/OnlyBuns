package com.example.OnlyBuns;

import com.example.OnlyBuns.dto.UserRequest;
import com.example.OnlyBuns.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        // Ovaj metod može biti korišćen za inicijalizaciju podataka ako je potrebno
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testConcurrentUserRegistrationWithSameUsernameAndEmail() throws Throwable {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> future1 = executor.submit(new Runnable() {

            @Override
            public void run() {
                System.out.println("Startovana nit 1");
                UserRequest user1 = new UserRequest();
                user1.setUsername("testuser");
                user1.setPassword("password123");
                user1.setFirstname("Zeljka");
                user1.setLastname("Zeljka");
                user1.setEmail("test1@example.com");
                user1.setStreet("Ljermontova 6");
                user1.setCity("Novi Sad");
                user1.setPostalCode(23432);
                user1.setCountry("Serbia");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }// thread uspavan na 3 sekunde da bi drugi thread mogao da izvrsi istu operaciju
                userService.save(user1);
            }
        });

        executor.submit(new Runnable() {

            @Override
            public void run() {

                //Future<?> future2 = executor.submit(() -> {
                System.out.println("Startovana nit 2");
                UserRequest user2 = new UserRequest();
                user2.setUsername("testuser"); // Isti username
                user2.setPassword("password456");
                user2.setFirstname("Nada");
                user2.setLastname("Nada");
                user2.setEmail("test1@example.com"); // Isti email
                user2.setStreet("Ljermontova 6");
                user2.setCity("Novi Sad");
                user2.setPostalCode(23432);
                user2.setCountry("Serbia");
                userService.save(user2);
            }// Drugi pokušaj registracije sa istim podacima
        });

        try {
                future1.get(); // podize ExecutionException za bilo koji izuzetak iz prvog child threada
            } catch (ExecutionException e) {
                System.out.println("Exception from thread " + e.getCause().getClass()); // u pitanju je bas ObjectOptimisticLockingFailureException
                throw e.getCause();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
		executor.shutdown();
    }
}
