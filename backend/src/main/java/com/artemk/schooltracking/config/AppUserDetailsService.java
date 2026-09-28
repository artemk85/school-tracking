package com.artemk.schooltracking.config;

import com.artemk.schooltracking.domain.User;
import com.artemk.schooltracking.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new AppUserPrincipal(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new AppUserPrincipal(user);
    }
}