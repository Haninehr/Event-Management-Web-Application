package com.eventmanagementisii.authentification.controller;



import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventmanagementisii.authentification.service.AuthService;

@RestController
@RequestMapping("/Internal")
public class InternalUserController {

    private final AuthService userService; 

    public InternalUserController(AuthService userService) {
        this.userService = userService;
    }

    //do internal batch ! (to comunicate with registration ms)
    // This endpoint is ONLY called by other microservices (registration-ms)
    @GetMapping("/users/batch")
    public Map<Long, UserInfo> getUsersByIds(@RequestParam List<Long> userIds) {
        return userService.getPublicInfoByIds(userIds);
    }

  
    public record UserInfo(String username, String email) {}
}