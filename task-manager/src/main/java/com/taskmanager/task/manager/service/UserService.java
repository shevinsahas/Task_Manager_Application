package com.taskmanager.task.manager.service;

import com.taskmanager.task.manager.ResponseHandler.CustomException;
import com.taskmanager.task.manager.dto.UserDTO;
import com.taskmanager.task.manager.model.CustomUserDetails;
import com.taskmanager.task.manager.model.Role;
import com.taskmanager.task.manager.model.User;
import com.taskmanager.task.manager.repository.RoleRepository;
import com.taskmanager.task.manager.repository.UserRepository;
import com.taskmanager.task.manager.security.JwtAuthenticationFilter;
import com.taskmanager.task.manager.security.JwtTokenProvider;
import com.taskmanager.task.manager.util.AppConfig;
import com.taskmanager.task.manager.util.Messages;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Lazy
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public Optional<UserDTO> findByUsername(String userName) {
        return userRepository.findByUserName(userName).map(User::convertToDTO);
    }


    public boolean existsByUsername(String userName) {
        Optional<User> existingUser = userRepository.findByUserName(userName);
        return existingUser.isPresent();
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setId(UUID.randomUUID().toString());
        user.setRole(roleRepository.findByRole(Messages.USER));
        user.setStatus(Messages.status_Active);
        return userRepository.save(user);
    }
    public User getCurrentUserDetails() {
        String userId = jwtTokenProvider.getUserId(jwtAuthenticationFilter.getToken());
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND));
    }


    public User findByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public boolean validateEmail(String email) {
        String regex = "^(?=.{1,320}@)(?!\\.)(?!.*\\.\\.)[A-Za-z0-9+_-]+(\\.[A-Za-z0-9+_-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    @Transactional
    public void createSuperAdmin() {
        if (userRepository.findByUserName(Messages.ADMIN_ROLE).isEmpty()) {
            Role adminRole = roleRepository.findByRole(Messages.ADMIN_ROLE);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setRole("admin");
                adminRole.setCreatedAt(new Date());
                adminRole.setUpdatedAt(new Date());
                adminRole=roleRepository.save(adminRole);
            }

            User admin = new User();
            admin.setFirstName(AppConfig.ADMIN_FIRST_NAME);
            admin.setLastName(AppConfig.ADMIN_LAST_NAME);
            admin.setUserName(AppConfig.USERNAME);
            admin.setPassword(passwordEncoder.encode(AppConfig.PASSWORD));
            admin.setEmailAddress(AppConfig.EMAIL);
            admin.setRole(adminRole);
            admin.setStatus(Messages.status_Active);
           this.userRepository.save(admin);

        }
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAddress(email);
        return new CustomUserDetails(user);
    }


}




