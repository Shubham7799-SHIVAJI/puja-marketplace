package com.SHIVA.puja.serviceimpl;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public void loginUser(LoginRequest request){

        User user = new User();

        user.setFullName(request.getName());

        if(request.getContact().contains("@")){
            user.setEmail(request.getContact());
        }else{
            user.setPhoneNumber(request.getContact());
        }

        // user.setRole("USER");
        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");
        user.setPhoneVerified(false);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}