package com.eventmanagementisii.authentification.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventmanagementisii.authentification.entity.User;
import com.eventmanagementisii.authentification.repository.UserRepository;
import com.eventmanagementisii.authentification.security.JwtUtil;

@RestController
@RequestMapping("/authentifications")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository AuthRepository;
    private final PasswordEncoder PWencoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UserRepository AuthRepository,
            PasswordEncoder PWencoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.AuthRepository = AuthRepository;
        this.PWencoder = PWencoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
       
        String useremail = loginRequest.get("email");
        String password = loginRequest.get("password");

       
        User dbUser = AuthRepository.findByemail(useremail);
        
        if (dbUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: User does not exist!!");
        }

        
        if (!PWencoder.matches(password, dbUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Incorrect password!!");
        }

    
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(useremail, password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        String token = jwtUtil.generateToken(userDetails.getUsername(),dbUser.getId(), dbUser.getRole().name());

        // return jwtToken
        return ResponseEntity.ok(Map.of(
                "token", token,
                "useremail", useremail,
                "role", dbUser.getRole().name(),
                "id_user",dbUser.getId()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
      if (AuthRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }
        if (AuthRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists!");
        }

        User newUser = new User(
                null,
                user.getUsername(),
                PWencoder.encode(user.getPassword()),
                user.getEmail(),
                user.getRole()
        );
        AuthRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully!");
    }



    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = AuthRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Hide password 
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}