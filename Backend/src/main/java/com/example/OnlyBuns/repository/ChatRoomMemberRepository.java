package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.ChatRoom;
import com.example.OnlyBuns.model.ChatRoomMember;
import com.example.OnlyBuns.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Integer>{

    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);

    List<ChatRoomMember> findByClient(Client client);

    Optional<ChatRoomMember> findByClientAndChatRoom(Client client, ChatRoom chatRoom);

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN cr.members crm1 JOIN cr.members crm2 " +
            "WHERE cr.type = 'PRIVATE' " +
            "AND crm1.client.id = :user1Id AND crm2.client.id = :user2Id " +
            "AND crm1.id != crm2.id " + // Osigurava da se radi o dva različita clana
            "AND (SELECT COUNT(m) FROM ChatRoomMember m WHERE m.chatRoom = cr) = 2") // Osigurava da soba ima TACNO 2 clana
    Optional<ChatRoom> findPrivateChatRoomByTwoMembers(@Param("user1Id") Integer user1Id, @Param("user2Id") Integer user2Id);

}
