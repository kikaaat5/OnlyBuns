package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.ChatMessage;
import com.example.OnlyBuns.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    @Query("SELECT DISTINCT m FROM ChatMessage m JOIN FETCH m.sender s WHERE m.chatRoom = :chatRoom ORDER BY m.timestamp ASC")
    List<ChatMessage> findByChatRoomOrderByTimestampAscWithSender(@Param("chatRoom") ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);

    List<ChatMessage> findTop10ByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);
}
