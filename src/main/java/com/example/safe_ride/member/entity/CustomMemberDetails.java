package com.example.safe_ride.member.entity;

import com.example.safe_ride.member.entity.Authority;
import com.example.safe_ride.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMemberDetails implements UserDetails {
    private Long id;
    private String userId;
    private String userName;
    private String password;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String birthday;
    private Authority authority;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authority != null) {
            String role = authority.getAuthority();
            return Collections.singleton(new SimpleGrantedAuthority(role));
        } else {
            return Collections.emptySet();
        }
    }
    private Member member;
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
