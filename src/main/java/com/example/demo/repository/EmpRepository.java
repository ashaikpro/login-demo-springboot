package com.example.demo.repository;

import com.example.demo.entity.Emp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpRepository extends JpaRepository<Emp, String> {
    Optional<Emp> findByUsername(String username);
}

