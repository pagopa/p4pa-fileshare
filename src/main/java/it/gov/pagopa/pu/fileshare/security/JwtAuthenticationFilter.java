package it.gov.pagopa.pu.fileshare.security;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AuthorizationService authorizationService;

  public JwtAuthenticationFilter(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain) throws ServletException, IOException {
    try {
      String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (StringUtils.hasText(authorization)) {
        String token = authorization.replace("Bearer ", "");
        UserInfo userInfo = authorizationService.validateToken(token);
        Collection<? extends GrantedAuthority> authorities = null;
        if (userInfo.getOrganizationAccess() != null) {
          authorities = userInfo.getOrganizations().stream()
            .filter(o -> userInfo.getOrganizationAccess().equals(o.getOrganizationIpaCode()))
            .flatMap(r -> r.getRoles().stream())
            .map(SimpleGrantedAuthority::new)
            .toList();
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userInfo, token, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        MDC.put("externalUserId", userInfo.getMappedExternalUserId());
      }
    } catch (InvalidAccessTokenException e){
      log.info("An invalid accessToken has been provided: " + e.getMessage());
    } catch (Exception e){
      log.error("Something gone wrong while validate accessToken", e);
    }
    filterChain.doFilter(request, response);
  }
}
