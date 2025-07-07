package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.ChatMessage;
import com.example.OnlyBuns.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    @Query("SELECT DISTINCT m FROM ChatMessage m JOIN FETCH m.sender s WHERE m.chatRoom = :chatRoom ORDER BY m.timestamp ASC")
    List<ChatMessage> findByChatRoomOrderByTimestampAscWithSender(@Param("chatRoom") ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    List<ChatMessage> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);

    List<ChatMessage> findTop10ByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);

    @Query(value = "SELECT * FROM chat_message cm WHERE cm.chat_room_id = :chatRoomId AND cm.timestamp < :joinedAt ORDER BY cm.timestamp DESC LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findTopNByChatRoomIdAndTimestampBefore(
            @Param("chatRoomId") Integer chatRoomId,
            @Param("joinedAt") LocalDateTime joinedAt,
            @Param("limit") int limit);


    List<ChatMessage> findByChatRoomIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
            Integer chatRoomId,
            LocalDateTime joinedAt);

    @Query(value = "SELECT * FROM chat_message cm WHERE cm.chat_room_id = :chatRoomId ORDER BY cm.timestamp DESC LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findTopNByChatRoomIdOrderByTimestampDesc(
            @Param("chatRoomId") Integer chatRoomId,
            @Param("limit") int limit);
}
