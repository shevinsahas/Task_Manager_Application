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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public User findByUsernameWithPassword(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }


    public Optional<UserDTO> getUserById(String id) {
        Optional<UserDTO> userDTO= userRepository.findById(id).map(User::convertToDTO);
        if(userDTO.isEmpty()){
            throw new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND);
        }
        return userDTO;
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


    public User findByUserIdToken(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND));
    }

    public Boolean verifyOtp(String email){
        if(otpValidation(email)){
            User user = userRepository.findByEmailAddress(email);
            user.setStatus(Messages.status_Active);
            userRepository.save(user);
            return true;
        }else {
            return false;
        }
    }

    public User findUserByTokenHeader(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(),Messages.USER_NOT_FOUND,Messages.NOT_FOUND));
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

    public ResponseEntity<?> authenticateUser(User user) throws CustomException{
        try {
            if(!validateEmail(user.getEmailAddress())){
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.EMAIL_NOT_VALID, Messages.EMAIL_NOT_VALID_MESSAGE);
            }
            if (userRepository.findByEmailAddress(user.getEmailAddress())==null) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND);
            }
            if (userRepository.findByEmailAddressAndDeleted(user.getEmailAddress(), false).isEmpty()) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), Messages.ACCOUNT_DISABLED, Messages.NOT_FOUND);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmailAddress(), user.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.createToken(authentication);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserDTO userDTO = findByUsername(userDetails.getUsername()).get();
            return new ResponseEntity<>(userDTO, headers, HttpStatus.OK);
        }catch (AuthenticationException ex) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.value(),Messages.INVALID_EMAIL_OR_PASSWORD, Messages.UNAUTHORIZED);
        }

    }

    public ResponseEntity<Map<String, Object>> otpAuthenticateUser(User user) throws CustomException{
        try {
            if(!validateEmail(user.getEmailAddress())){
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.EMAIL_NOT_VALID, Messages.EMAIL_NOT_VALID_MESSAGE);
            }
            if (userRepository.findByEmailAddress(user.getEmailAddress())==null) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND);
            }
            if (userRepository.findByEmailAddressAndDeleted(user.getEmailAddress(), false).isEmpty()) {
                throw new CustomException(HttpStatus.NOT_FOUND.value(), Messages.ACCOUNT_DISABLED, Messages.NOT_FOUND);
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmailAddress(), user.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.createEmailToken(userDetails.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            Map<String, Object> response = new HashMap<>();
            response.put("id", userRepository.findByEmailAddress(user.getEmailAddress()).getId());
            response.put("email", user.getEmailAddress());
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }catch (AuthenticationException ex) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.value(),Messages.INVALID_EMAIL_OR_PASSWORD, Messages.UNAUTHORIZED);
        }

    }

    public User findUserByToken(String auth) {
        String token = auth.split("\\s+")[1];
        String userId = jwtTokenProvider.getUserId(token);
        return userRepository.findByIdAndDeleted(userId,false)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(),Messages.USER_NOT_FOUND,Messages.NOT_FOUND));
    }

    public ResponseEntity<Map<String, Object>> updateUserStatus(String email, String status) {
        User user = findByEmailAddress(email);
        if (user != null && status != null) {
            user.setStatus(status);
            userRepository.save(user);
        }
        String token= jwtTokenProvider.createEmailToken(email);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.OK.value());
        response.put("message", Messages.OTP_VERIFIED);
        response.put("id", Messages.SUCCESSFUL);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);

    }



    public Boolean checkUserStatus(String id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND));
        if (user.getStatus().equals(Messages.status_Pending)){
            Date updatedAtDate = user.getUpdatedAt();
            LocalDateTime createdAt = updatedAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(createdAt, now);
            if (duration.toMinutes() > 10) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.LINK_EXPIRED, Messages.BAD_REQUEST);
            }
        }else{
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.USER_ALREADY_VERIFIED, Messages.BAD_REQUEST);
        }
        return true;
    }

    public Boolean otpValidation(String email){
        User user = userRepository.findByEmailAddress(email);
        if(user!=null) {
            Date updatedAtDate = user.getUpdatedAt();
            LocalDateTime updatedAt = updatedAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(updatedAt, now);
            if (duration.toMinutes() > 5) {
                throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.OTP_NOT_VERIFIED, Messages.BAD_REQUEST);
            }
        }else{
            throw new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND);
        }

        return true;
    }


    public List<UserDTO> convertToDTO(List<User> users) {
        return users.stream()
                .map(User::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createSuperAdmin() {
        if (userRepository.findByUserName(Messages.ADMIN_ROLE).isEmpty()) {
            // Create admin role if it doesn't exist
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


    @Transactional
    public UserDTO patchUser(String userId, UserDTO updatedUser) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND));

        String userID= jwtTokenProvider.getUserId(jwtAuthenticationFilter.getToken());
        User permisionUser = userRepository.findById(userID)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND.value(), Messages.USER_NOT_FOUND, Messages.NOT_FOUND));
        if (permisionUser.getRole().getRole().equals("user")) {
            if (updatedUser.getEmail() != null || updatedUser.getUsername() != null  || updatedUser.getRole() != null || updatedUser.getStatus() != null) {
                throw new CustomException(HttpStatus.FORBIDDEN.value(), Messages.FORBIDDEN_MESSAGE, Messages.FORBIDDEN);
            }
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setContactNumber(updatedUser.getContactNumber());

        }
        if (permisionUser.getRole().getRole().equals("admin")) {
            if (updatedUser.getFirstName() != null) {
                existingUser.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null) {
                existingUser.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getEmail() != null) {
                if(!validateEmail(updatedUser.getEmail())){
                    throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.EMAIL_NOT_VALID, Messages.EMAIL_NOT_VALID_MESSAGE);
                }
                existingUser.setEmailAddress(updatedUser.getEmail());
            }
            if (updatedUser.getUsername() != null) {
                existingUser.setUserName(updatedUser.getUsername());
            }
            if (updatedUser.getContactNumber() != null) {
                existingUser.setContactNumber(updatedUser.getContactNumber());
            }
            if (updatedUser.getStatus() != null) {
                existingUser.setStatus(updatedUser.getStatus());
            }
            if (updatedUser.getRole() != null) {
                existingUser.setRole(roleRepository.findByRole(updatedUser.getRole()));
            }

        }
        existingUser.setUpdatedAt(new Date());
        User user= userRepository.save(existingUser);
        return User.convertToDTO(user);
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAddress(email);
        return new CustomUserDetails(user);
    }


}









