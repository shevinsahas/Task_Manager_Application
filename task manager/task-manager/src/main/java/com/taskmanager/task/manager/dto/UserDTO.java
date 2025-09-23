package com.taskmanager.task.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String email;
    private String contactNumber;
    private String profilePicture;
    private String status;
    private Date createdAt;
    private Date updatedAt;


}
