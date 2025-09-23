package com.taskmanager.task.manager.repository;


import com.taskmanager.task.manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserName(String userName);

    User  findByEmailAddress(String emailAddress);

    Optional<User> findByIdAndDeleted(String id, boolean deleted);

    Optional<User> findByIdAndDeletedFalse(String id);
    Optional<User> findByEmailAddressAndDeleted(String emailAddress, boolean deleted);

}
