package com.shop.service;

import com.shop.entity.User;
import com.shop.exception.NotLoggedInException;
import com.shop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Cài đặt CurrentUserService bằng phiên (HttpSession): sau khi đăng nhập,
 * AuthController lưu id người dùng vào session với khóa SESSION_KEY.
 * Spring tiêm vào một "proxy" của HttpSession, nên mỗi request đọc đúng session của nó.
 */
@Service
public class SessionCurrentUserService implements CurrentUserService {

    public static final String SESSION_KEY = "USER_ID";

    private final HttpSession session;
    private final UserRepository userRepository;

    public SessionCurrentUserService(HttpSession session, UserRepository userRepository) {
        this.session = session;
        this.userRepository = userRepository;
    }

    @Override
    public User getCurrentUser() {
        return findCurrentUser().orElseThrow(NotLoggedInException::new);
    }

    @Override
    public Optional<User> findCurrentUser() {
        Object id = session.getAttribute(SESSION_KEY);
        return id == null ? Optional.empty() : userRepository.findById((Long) id);
    }
}
