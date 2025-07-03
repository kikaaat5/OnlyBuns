package com.example.OnlyBuns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequestDto {
    private Integer chatRoomId;
    private String content;
}
