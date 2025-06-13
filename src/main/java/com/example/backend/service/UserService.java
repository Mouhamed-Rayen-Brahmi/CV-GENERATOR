package com.example.backend.service;

import com.example.backend.dto.UserDto;
import com.example.backend.model.User;

public interface UserService {
    User createUser(UserDto userDto);
    User findByEmail(String email);
    User findByUsername(String username);
    User findById(Long id);
    boolean existsByEmail(String email);
    void saveUser(User user);
}