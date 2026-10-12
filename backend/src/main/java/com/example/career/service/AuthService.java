package com.example.career.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.dto.AuthResponse;
import com.example.career.dto.LoginRequest;
import com.example.career.dto.RegisterRequest;
import com.example.career.exception.EmailAlreadyRegisteredException;
import com.example.career.exception.InvalidRequestException;
import com.example.career.model.Role;
import com.example.career.model.Student;
import com.example.career.model.User;
import com.example.career.repository.StudentRepository;
import com.example.career.repository.UserRepository;
import com.example.career.security.JwtService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new InvalidRequestException("Administrator accounts cannot be registered publicly");
        }
        if (request.role() == Role.RECRUITER) {
            throw new InvalidRequestException("Recruiter accounts can only be created by an administrator");
        }

        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = userRepository.save(new User(
                request.name().trim(), email, passwordEncoder.encode(request.password()), request.role()));
        if (request.role() == Role.STUDENT) {
            studentRepository.save(new Student(user));
        }

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidRequestException("Email or password is incorrect"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.createToken(user), user.getId(), user.getName(),
                user.getEmail(), user.getRole());
    }
}
