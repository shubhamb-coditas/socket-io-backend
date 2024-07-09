package com.chat.socketIO.repository;

import com.chat.socketIO.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;



public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByToken(String token);
    Optional<User> findByUsernameAndRoom(String username, String room);
    List<User> findByRoom(String room);

}