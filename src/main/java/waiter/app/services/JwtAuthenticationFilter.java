package waiter.app.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import waiter.app.utils.JwtUtil;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtUtil.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Εξαγωγή ρόλου από το token (π.χ. "WAITER")
            String role = jwtUtil.extractClaim(jwt, claims -> claims.get("role", String.class));

            // Δημιουργία granted authority με βάση το ρόλο
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // Δημιουργία authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Logging για έλεγχο
            System.out.println("🛡️ JWT AUTH FILTER: jwt = " + jwt);
            System.out.println("→ Username: " + username);
            System.out.println("→ Role from token: " + role);

            // Θέσε το authentication στο security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
