package com.example.OnlyBuns.dto;

import com.example.OnlyBuns.model.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMemberDto {
    private Integer id;
    private ClientDto client;
    private Integer chatRoomId;
    private MemberRole role;
}
