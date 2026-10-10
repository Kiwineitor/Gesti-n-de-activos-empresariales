package com.sath.sistema_sath.Config.Auditoria;

import com.sath.sistema_sath.Config.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {

    private final JwtService jwtService;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(0L);
        }

        try {
            // El token viene como credentials en el contexto de seguridad
            Object credentials = authentication.getCredentials();
            if (credentials instanceof String token) {
                Long id = jwtService.extractIdUsuario(token);
                return Optional.of(id != null ? id : 0L);
            }
        } catch (Exception e) {
            return Optional.of(0L);
        }

        return Optional.of(0L);
    }
}