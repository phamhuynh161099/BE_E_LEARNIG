package com.sakata.boilerplate.security;


import com.sakata.boilerplate.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final com.sakata.boilerplate.mapper.primary.UserMapper userMapper;
    private final PermissionCacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("::>>Run loadUserByUsername");
        var user = userMapper.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại: " + username));

        System.out.println("::>>Run loadUserByUsername 1" + user);

        // Thử lấy permissions từ Redis trước
        // List<String> cachedPerms = cacheService.getPermissions(username);
        List<String> cachedPerms = null;

        System.out.println("::>>Run loadUserByUsername 2" + cachedPerms);

        List<SimpleGrantedAuthority> authorities;

        if (cachedPerms != null) {
            // Cache hit: dùng cached permissions + thêm ROLE_ prefix từ MySQL
            var roleAuthorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()));
            var permAuthorities = cachedPerms.stream()
                .map(SimpleGrantedAuthority::new);
            authorities = Stream.concat(roleAuthorities, permAuthorities)
                .distinct().collect(Collectors.toList());
        } else {
            // Cache miss: load đầy đủ từ MySQL, rồi set cache
            var allPerms = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct().sorted().toList();

            // cacheService.setPermissions(username, allPerms);

            authorities = user.getRoles().stream()
                .flatMap(role -> Stream.concat(
                    Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getName())),
                    role.getPermissions().stream()
                        .map(p -> new SimpleGrantedAuthority(p.getName()))
                ))
                .distinct().collect(Collectors.toList());
        }

        System.out.println("::>>Run loadUserByUsername final" + authorities);

        return User.withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .disabled(!user.isEnabled())
            .build();
    }
}
