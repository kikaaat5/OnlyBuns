package com.example.OnlyBuns.service;

import com.example.OnlyBuns.model.Post;
import com.example.OnlyBuns.repository.PostRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ImageCompressionService {
    private final PostRepository postRepository;

    private final String uploadDir = "uploads";


    public ImageCompressionService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    @Scheduled(cron = "0 22 23 * * *")  // Svaki dan u 19:20
    public void scheduledImageCompression() {
        System.out.println("⏰ Pokrećem zakazanu dnevnu kompresiju slika");
        compressOldImages();
    }

   // @Scheduled(cron = "0 24  21 * * *")
    public void compressOldImages() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(1);
        List<Post> posts = postRepository.findAll();

        for (Post post : posts) {
            if(post.getCreatedAt().isBefore(oneMonthAgo) &&  post.getImagePath() != null) {
                try{
                    Path original = Paths.get(uploadDir, Paths.get(post.getImagePath()).getFileName().toString());
                    if(!Files.exists(original)) {
                        continue;
                    }
                    String compressedName = UUID.randomUUID().toString() + "_compressed.jpg";
                    Path compressedPath = Paths.get(uploadDir, compressedName);

                    if(Files.exists(compressedPath)){
                        continue;
                    }

                    Thumbnails.of(original.toFile())
                            .scale(1.0)
                            .outputQuality(0.6)
                            .toFile(compressedPath.toFile());
                    post.setCompressedImagePath("/uploads/" + compressedName);
                    postRepository.save(post);
                    System.out.println("✔ Kompresovana slika: " + compressedPath);
                } catch (IOException e){
                    System.err.println("✖ Greška pri kompresiji slike: " + post.getImagePath());
                    e.printStackTrace();
                }

            }
        }
    }


}
