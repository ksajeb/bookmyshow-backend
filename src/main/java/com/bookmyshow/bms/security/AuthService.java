package com.bookmyshow.bms.security;

import com.bookmyshow.bms.dto.LoginResponseDto;
import com.bookmyshow.bms.dto.SignupRequestDto;
import com.bookmyshow.bms.dto.SignupResponseDto;
import com.bookmyshow.bms.entity.User;
import com.bookmyshow.bms.dto.LoginRequestDto;
import com.bookmyshow.bms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        String token = authUtil.generateAccessToken(user);

        return new LoginResponseDto(token, user.getId());
    }

    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        User user = userRepository.findByEmail(signupRequestDto.getEmail()).orElse(null);

        if(user != null) throw new IllegalArgumentException("User already exists");

        user = userRepository.save(User.builder()
                .name(signupRequestDto.getName())
                .email(signupRequestDto.getEmail())
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .build()
        );

        return new SignupResponseDto(user.getId(), user.getName());
    }
}
