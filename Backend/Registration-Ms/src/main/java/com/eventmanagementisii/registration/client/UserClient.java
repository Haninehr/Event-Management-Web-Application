// src/main/java/com/eventmanagement/registration/client/UserClient.java
package com.eventmanagementisii.registration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "authentification-service", url = "${app.authenification-service-url}") 
public interface UserClient {

    @GetMapping("/Internal/users/batch")
    Map<Long, UserInfo> getUsersByIds(@RequestParam("userIds") List<Long> userIds);

    // Copy the same record here (yes, duplicate record is OK for client)
    record UserInfo(String username, String email) {}
}