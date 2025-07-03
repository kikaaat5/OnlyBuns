package com.example.OnlyBuns.repository;

import com.example.OnlyBuns.model.ChatRoom;
import com.example.OnlyBuns.model.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public  interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer>{

    List<ChatRoom> findByType(ChatRoomType type);
}
