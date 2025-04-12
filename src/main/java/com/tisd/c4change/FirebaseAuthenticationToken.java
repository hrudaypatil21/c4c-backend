package com.tisd.c4change;

import com.google.firebase.auth.FirebaseToken;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {
    private final String uid;
    @Getter
    private final FirebaseToken token;

    public FirebaseAuthenticationToken(String uid, FirebaseToken token) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.uid = uid;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return uid;
    }

    }
