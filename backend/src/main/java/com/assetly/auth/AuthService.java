package com.assetly.auth;

import com.assetly.auth.dto.LoginRequest;
import com.assetly.auth.dto.LoginResponse;
import com.assetly.auth.dto.SignupRequest;
import com.assetly.auth.dto.SignupResponse;
import com.assetly.common.BusinessException;
import com.assetly.security.JwtTokenProvider;
import com.assetly.user.User;
import com.assetly.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("USER_EMAIL_DUPLICATED", "이미 가입된 이메일입니다.");
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );

        User savedUser = userRepository.save(user);
        return SignupResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        return LoginResponse.of(accessToken, user);
    }
}
