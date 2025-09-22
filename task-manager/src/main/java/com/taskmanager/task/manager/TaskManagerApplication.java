package com.taskmanager.task.manager;

import com.taskmanager.task.manager.model.Role;
import com.taskmanager.task.manager.repository.RoleRepository;
import com.taskmanager.task.manager.service.AppInitializerService;
import com.taskmanager.task.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.Date;

@SpringBootApplication
public class TaskManagerApplication implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private AppInitializerService appInitializerService;

	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException {
		if (roleRepository.findByRole("admin") == null) {
			Role adminRole = new Role();
			adminRole.setRole("admin");
			adminRole.setCreatedAt(new Date());
			adminRole.setUpdatedAt(new Date());
			roleRepository.save(adminRole);

		}

		if (roleRepository.findByRole("user") == null) {
			Role userRole = new Role();
			userRole.setRole("user");
			userRole.setCreatedAt(new Date());
			userRole.setUpdatedAt(new Date());
			roleRepository.save(userRole);
		}

		userService.createSuperAdmin();
	}
}
