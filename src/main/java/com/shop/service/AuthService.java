package com.shop.service;

import com.shop.dto.RegisterRequest;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đăng ký và kiểm tra đăng nhập.
 * Mật khẩu chỉ lưu dạng băm BCrypt (có muối ngẫu nhiên), không bao giờ lưu mật khẩu gốc.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email " + email + " đã được đăng ký");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu nhập lại không khớp");
        }
        User user = new User(email, request.getFullName().trim(), request.getPhone().trim(), request.getAddress());
        user.setPasswordHash(hash(request.getPassword()));
        return userRepository.save(user);
    }

    /** Trả về người dùng nếu đúng email và mật khẩu; ngược lại báo lỗi chung (không nói sai phần nào). */
    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        return userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .filter(u -> u.getPasswordHash() != null && rawPassword != null
                        && encoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> new BusinessException("Email hoặc mật khẩu không đúng"));
    }
}
