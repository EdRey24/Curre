package edu.bu.cs411.group10.curre.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO dto) {
        try {
            AuthResponse response = userService.register(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new AuthResponse(null, dto.getEmail(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO dto) {
        try {
            AuthResponse response = userService.login(dto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new AuthResponse(null, dto.getEmail(), e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }
}
