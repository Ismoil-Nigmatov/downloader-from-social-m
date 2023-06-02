package com.example.downloaderfromsocialm.repository;

import com.example.downloaderfromsocialm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
