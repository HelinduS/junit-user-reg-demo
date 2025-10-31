// UserRepository.java
package com.example.user;
public interface UserRepository {
    boolean existsByEmail(String email);
    User save(User user);
    User findByEmail(String email);
}