package com.tisd.c4change;

import com.google.firebase.auth.FirebaseToken;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.lang.String;

import java.util.Collections;
import java.util.Map;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal; // Can be either Map or String
    @Getter
    private final FirebaseToken token;

    public FirebaseAuthenticationToken(Object principal, FirebaseToken token) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}