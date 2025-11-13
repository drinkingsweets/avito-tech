package org.example.avitotech.controller;

import org.example.avitotech.dto.TokenDTO;
import org.example.avitotech.exception.ApiException;
import org.example.avitotech.exception.ErrorCode;
import org.example.avitotech.jwt.JwtTokenProvider;
import org.example.avitotech.model.User;
import org.example.avitotech.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/admin-token")
    public ResponseEntity<?> getAdminToken(@RequestBody TokenDTO tokenDTO) {
        return generateToken(tokenDTO.getUserId(), "ADMIN", true);
    }

    @PostMapping("/user-token")
    public ResponseEntity<?> getUserToken(@RequestBody TokenDTO tokenDTO) {
        return generateToken(tokenDTO.getUserId(), "USER", false);
    }

    private ResponseEntity<?> generateToken(String userId, String role, boolean isAdmin) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        String token = isAdmin
                ? jwtTokenProvider.createAdminToken(userId)
                : jwtTokenProvider.createUserToken(userId);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", role,
                "userId", user.getUserId()
        ));
    }
}

