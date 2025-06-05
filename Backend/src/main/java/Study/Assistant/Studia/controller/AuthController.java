package Study.Assistant.Studia.controller;

import Study.Assistant.Studia.dto.auth.JwtAuthenticationResponse;
import Study.Assistant.Studia.dto.auth.LoginRequest;
import Study.Assistant.Studia.dto.auth.SignupRequest;
import Study.Assistant.Studia.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@Valid @RequestBody SignupRequest request) {
        JwtAuthenticationResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        JwtAuthenticationResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
