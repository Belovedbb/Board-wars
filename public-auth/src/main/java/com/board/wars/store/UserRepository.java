package com.board.wars.store;

import com.board.wars.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findAllByExpired(boolean isExpired);
    List<User> findUsersByManagementLinkedEquals(boolean linked);
}
