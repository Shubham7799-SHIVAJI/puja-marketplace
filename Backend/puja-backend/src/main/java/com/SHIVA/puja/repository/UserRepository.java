package com.SHIVA.puja.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findTopByEmailOrderByIdDesc(String email);

	boolean existsByEmail(String email);

}