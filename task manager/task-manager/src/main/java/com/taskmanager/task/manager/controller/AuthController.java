package com.taskmanager.task.manager.controller;


import com.taskmanager.task.manager.ResponseHandler.CustomException;
import com.taskmanager.task.manager.dto.UserDTO;
import com.taskmanager.task.manager.model.User;
import com.taskmanager.task.manager.service.UserService;
import com.taskmanager.task.manager.util.Messages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody User user) {
        log.info("Registered user:"+user);
        if (userService.findByEmailAddress(user.getEmailAddress()) != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), Messages.USER_ALREADY_EXISTS, Messages.ALREADY_EXISTS);
        }
        return User.convertToDTO(userService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletRequest request) {
        return userService.authenticateUser(user);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getUserDetails(HttpServletRequest request) {
        User user = userService.getCurrentUserDetails();
        return ResponseEntity.ok(User.convertToDTO(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate HTTP session and clear SecurityContext
        var session = request.getSession(false);
        if (session != null) session.invalidate();

        // Delete JSESSIONID cookie
        var cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

}
