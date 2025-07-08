package com.example.OnlyBuns.controller;

import com.example.OnlyBuns.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:4200")
public class ImageServingController {

    private final PostService postService;

    @Autowired
    public ImageServingController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/uploads/**")
    public ResponseEntity<byte[]> getImage(HttpServletRequest request) {

        String requestUri = request.getRequestURI();
        System.out.println("ImageServingController: Primljen zahtev URI: " + requestUri);

        int uploadsIndex = requestUri.indexOf("/uploads/");
        String relativeImagePath;
        if (uploadsIndex != -1) {
            relativeImagePath = requestUri.substring(uploadsIndex);
        } else {
            System.err.println("ImageServingController ERROR: URL putanja ne sadrži '/uploads/': " + requestUri);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        System.out.println(">>> Zahtev za sliku preko ImageServingController-a (rekonstruisano): " + relativeImagePath);

        try {
            byte[] imageBytes = postService.getImageBytes(relativeImagePath);
            System.out.println("ImageServingController: Sliku uspešno dohvaćena iz PostService-a.");

            HttpHeaders headers = new HttpHeaders();
            // Izvuci filename iz relativeImagePath
            String filename = relativeImagePath.substring(relativeImagePath.lastIndexOf('/') + 1);
            String extension = "";
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = filename.substring(dotIndex + 1).toLowerCase();
            }

            switch (extension) {
                case "jpg":
                case "jpeg":
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    break;
                case "png":
                case "PNG":
                    headers.setContentType(MediaType.IMAGE_PNG);
                    break;
                case "gif":
                    headers.setContentType(MediaType.IMAGE_GIF);
                    break;
                default:
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            System.out.println("ImageServingController: Postavljen Content-Type: " + headers.getContentType());

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            System.err.println("ImageServingController ERROR: IOException pri učitavanju slike: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("ImageServingController ERROR: Neočekivana greška: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}