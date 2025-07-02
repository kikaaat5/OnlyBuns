package com.example.OnlyBuns.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("api/images")
public class ImageUploadController {

    private final String uploadDir = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<String> handleImageUpload(@RequestParam("image") MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //jedinstveno ime slike
            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            //cuvamo fajl
            Path filePath = Paths.get(uploadDir, uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + uniqueFileName;
            return ResponseEntity.ok(fileUrl);
        }catch (IOException e){
            return new ResponseEntity<>("Upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
