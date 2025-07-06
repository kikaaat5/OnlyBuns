package com.example.OnlyBuns.dto;

import com.example.OnlyBuns.model.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {

    private Integer id;
    private String name;
    private ChatRoomType type;
    private ClientDto admin;
    private List<ClientDto> members;
    private ChatMessageDto lastMessage;
}
