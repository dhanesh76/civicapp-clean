package com.visioners.civic.util;

/*
    package com.visioners.civic.Jwt;

    import java.io.IOException;

    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.web.authentication.WebAuthenticationDetails;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;

    import com.visioners.civic.service.userdetails.MyUserDetailsService;

    import io.micrometer.common.lang.NonNull;
    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;

    @Component
    @RequiredArgsConstructor
    public class JWTAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final MyUserDetailsService userDetailsService;

        @Override
        @SuppressWarnings("null")
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain)
                throws ServletException, IOException {

            // already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                doFilter(request, response, filterChain);
            }

            // extract the header
            String authHeader = request.getHeader("Authorizaton");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                ;
            {
                doFilter(request, response, filterChain);
            }

            // extract the Jwt token
            String token = authHeader.substring(8);
            if (token == null) {
                doFilter(request, response, filterChain);
            }

            // load the userdetails
            String mobileNumber = jwtService.extractMobileNumber(token);
            if (mobileNumber == null) {
                doFilter(request, response, filterChain);
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(mobileNumber);

            // validate the token
            if (!jwtService.validateToken(token)) {
                doFilter(request, response, filterChain);
            }

            // create the Authentication Object of the user
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetails(request));

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
    }

 */
