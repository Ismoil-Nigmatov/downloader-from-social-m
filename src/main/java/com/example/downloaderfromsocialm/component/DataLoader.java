package com.example.downloaderfromsocialm.component;

import com.example.downloaderfromsocialm.entity.Role;
import com.example.downloaderfromsocialm.entity.User;
import com.example.downloaderfromsocialm.repository.RoleRepository;
import com.example.downloaderfromsocialm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Value("${spring.sql.init.mode}")
    String mode;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args){
        if (mode.equals("always")){
            Role admin = roleRepository.save(new Role(1L,"ADMIN"));

            userRepository.save(new User("688008330",null,null,null,100L,passwordEncoder.encode("mdyiloveyou"),true,true,true,true,admin));
        }
    }
}
