package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.ChatRoom;
import com.example.OnlyBuns.model.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public  interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer>{

    List<ChatRoom> findByType(ChatRoomType type);

    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.members m JOIN FETCH m.client c WHERE m.client.id = :clientId")
    List<ChatRoom> findChatRoomsByClientWithMembers(@Param("clientId") Integer clientId);

    @Query("SELECT DISTINCT crm.chatRoom FROM ChatRoomMember crm WHERE crm.client.id = :clientId")
    List<ChatRoom> findChatRoomsByClientId(@Param("clientId") Integer clientId);
}
