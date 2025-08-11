package com.spring.LibraryManagement.Service;


import com.spring.LibraryManagement.DTO.UserDTO;
import com.spring.LibraryManagement.Entity.User;
import com.spring.LibraryManagement.Mapper.UserMapper;
import com.spring.LibraryManagement.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {
    UserRepository userRepository;
    UserMapper userMapper;

    @Transactional
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(UserDTO userDTO, Long id) {
        return findUserById(id).
                map(existingUser ->{
                        existingUser.setUsername(userDTO.getUsername());
                        existingUser.setPassword(userDTO.getPassword());
                        existingUser.setEmail(userDTO.getEmail());
                        return saveUser(existingUser);
                }).orElse(null);
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
