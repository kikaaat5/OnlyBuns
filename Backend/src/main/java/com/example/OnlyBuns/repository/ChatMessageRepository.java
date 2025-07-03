package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.ChatMessage;
import com.example.OnlyBuns.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);

    List<ChatMessage> findTop10ByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);
}
