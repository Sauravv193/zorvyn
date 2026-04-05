package com.financeapp.service;

import com.financeapp.dto.response.UserResponse;
import com.financeapp.entity.User;
import com.financeapp.entity.User.UserStatus;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.mapper.UserMapper;
import com.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    public List<UserResponse> findAll() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    public UserResponse findById(Long id) {
        return userMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public UserResponse setStatus(Long id, UserStatus status) {
        User user = getOrThrow(id);
        user.setStatus(status);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse setRole(Long id, User.Role role) {
        User user = getOrThrow(id);
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
