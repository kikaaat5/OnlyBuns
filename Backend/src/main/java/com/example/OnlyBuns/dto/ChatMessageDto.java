package com.example.OnlyBuns.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Integer id;
    private ClientDto sender;
    private Integer chatRoomId;
    private String content;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") // Formatira LocalDateTime za JSON
    private LocalDateTime timestamp;
}
