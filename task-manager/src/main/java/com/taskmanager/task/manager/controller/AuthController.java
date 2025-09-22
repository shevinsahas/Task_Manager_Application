package com.taskmanager.task.manager.controller;


import com.taskmanager.task.manager.ResponseHandler.CustomException;
import com.taskmanager.task.manager.dto.UserDTO;
import com.taskmanager.task.manager.model.User;
import com.taskmanager.task.manager.service.UserService;
import com.taskmanager.task.manager.util.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


}
