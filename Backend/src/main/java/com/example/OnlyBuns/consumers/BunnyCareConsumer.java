package com.example.OnlyBuns.consumers;

import com.example.OnlyBuns.dto.RabbitMQLocationDto;
import com.example.OnlyBuns.model.BunnyCareLocation;
import com.example.OnlyBuns.repository.BunnyCareLocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BunnyCareConsumer {

    private final BunnyCareLocationRepository bunnyCareLocationRepository;

    @RabbitListener(queuesToDeclare = @Queue("bunny-care-queue"))
    public void consumeMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RabbitMQLocationDto locationDto = objectMapper.readValue(message, RabbitMQLocationDto.class);
            System.out.println("Primljena poruka: " + locationDto);

            BunnyCareLocation location = new BunnyCareLocation();
            location.setIdentifier(String.valueOf(locationDto.getId())); // Postavi ID kao identifier, možeš promeniti prema potrebi
            location.setName(locationDto.getName());
            location.setLocation(locationDto.getLocation());

            // Spremi entitet u bazu
            bunnyCareLocationRepository.save(location);
            location.setIdentifier(String.valueOf(locationDto.getId())); // Postavi ID kao identifier, možeš promeniti prema potrebi
            location.setName(locationDto.getName());
            location.setLocation(locationDto.getLocation());

            // Spremi entitet u bazu
            bunnyCareLocationRepository.save(location);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
