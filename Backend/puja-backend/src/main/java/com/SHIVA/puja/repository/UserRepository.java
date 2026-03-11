package com.SHIVA.puja.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.SHIVA.puja.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}