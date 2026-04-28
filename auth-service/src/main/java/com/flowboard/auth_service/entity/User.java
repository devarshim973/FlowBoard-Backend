package com.flowboard.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Table(
        indexes = {
                @Index(name = "idx_email", columnList = "email")
        }
)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    /* this can be null when using oauth and no one can login to this account as the password
    can not be null when logging in using username password
    */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private ROLE role = ROLE.MEMBER;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private PROVIDER provider = PROVIDER.MANUAL;

    /* Default value is false so when user signup account is disabled and will be
       enabled by after verification of email
     */
    private boolean isActive = false;

    public void setActive(boolean isActive) {
        log.info("Is active " + isActive);
        this.isActive = isActive;
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
