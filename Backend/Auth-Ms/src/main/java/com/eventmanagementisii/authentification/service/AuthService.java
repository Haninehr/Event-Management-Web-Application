package com.eventmanagementisii.authentification.service;

import com.eventmanagementisii.authentification.controller.InternalUserController;
import com.eventmanagementisii.authentification.controller.InternalUserController.UserInfo;
import com.eventmanagementisii.authentification.entity.User;
import com.eventmanagementisii.authentification.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String useremail) throws UsernameNotFoundException {
        User user = userRepository.findByemail(useremail);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with useremail: " + useremail);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()  
        );
    }


public Map<Long, UserInfo> getPublicInfoByIds(List<Long> userIds) {
    return userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(
                User::getId,
                user -> new InternalUserController.UserInfo(
                    user.getUsername(),
                    user.getEmail()
                )
            ));
}

}