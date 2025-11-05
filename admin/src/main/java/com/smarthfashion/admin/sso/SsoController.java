package com.smarthfashion.admin.sso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class SsoController {

    private final String sharedSecret;
    private final long maxAgeSeconds;

    public SsoController(@Value("${sso.shared-secret:}") String sharedSecret,
                         @Value("${sso.max-age:60}") long maxAgeSeconds) {
        this.sharedSecret = sharedSecret;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    @GetMapping("/sso/login")
    public RedirectView ssoLogin(@RequestParam("token") String token) {
        try {
            SsoTokenService svc = new SsoTokenService(sharedSecret, maxAgeSeconds);
            SsoTokenService.SsoPayload p = svc.verify(token);
            if (!"ADMIN".equalsIgnoreCase(p.role())) {
                return new RedirectView("/login?error=notadmin");
            }
            var auth = new UsernamePasswordAuthenticationToken(
                    p.email(), "N/A", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            return new RedirectView("/admin");
        } catch (Exception ex) {
            return new RedirectView("/login?error=sso");
        }
    }
}
