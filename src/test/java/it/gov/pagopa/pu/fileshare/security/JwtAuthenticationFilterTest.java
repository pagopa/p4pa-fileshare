package it.gov.pagopa.pu.fileshare.security;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private FilterChain filterChainMock;

  @Mock
  private AuthorizationService authorizationServiceMock;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilterMock;

  @Test
  void givenValidTokenWhenDoFilterInternalThenOk() throws ServletException, IOException {
    // Given
    String accessToken = "ACCESSTOKEN";
    MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/path");
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    MockHttpServletResponse response = new MockHttpServletResponse();

    List<UserOrganizationRoles> organizations = List.of(
      new UserOrganizationRoles()
        .operatorId("operator1")
        .organizationIpaCode("ORG")
        .email("email1@example.com")
        .roles(List.of("ROLE")),
      new UserOrganizationRoles()
        .operatorId("operator2")
        .organizationIpaCode("ORG2")
        .email("email2@example.com")
        .roles(List.of("ROLE2"))
    );

    UserInfo userInfo = new UserInfo().mappedExternalUserId("MAPPEDEXTERNALUSERID")
      .fiscalCode("FISCALCODE")
      .familyName("FAMILYNAME")
      .name("NAME")
      .issuer("ISSUER")
      .organizationAccess("ORG")
      .organizations(organizations);

    Collection<? extends GrantedAuthority> authorities = null;
    if (userInfo.getOrganizationAccess() != null) {
      authorities = userInfo.getOrganizations().stream()
        .filter(o -> userInfo.getOrganizationAccess().equals(o.getOrganizationIpaCode()))
        .flatMap(r -> r.getRoles().stream())
        .map(SimpleGrantedAuthority::new)
        .toList();
    }

    Mockito.when(authorizationServiceMock.validateToken(accessToken)).thenReturn(userInfo);

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userInfo, accessToken, authorities);
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    // When
    jwtAuthenticationFilterMock.doFilterInternal(request, response, filterChainMock);

    // Then
    Mockito.verify(filterChainMock).doFilter(request, response);
    Assertions.assertEquals(
      authToken,
      SecurityContextHolder.getContext().getAuthentication()
    );
  }

  @Test
  void givenInvalidTokenWhenDoFilterInternalThenInvalidAccessTokenException() throws ServletException, IOException {
    // Given
    String accessToken = "INVALIDACCESSTOKEN";
    MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/path");
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    MockHttpServletResponse response = new MockHttpServletResponse();

    Mockito.doThrow(new InvalidAccessTokenException("An invalid accessToken has been provided")).when(authorizationServiceMock).validateToken(accessToken);

    // When
    jwtAuthenticationFilterMock.doFilterInternal(request, response, filterChainMock);

    // Then
    Mockito.verify(filterChainMock).doFilter(request, response);
  }

  @Test
  void givenInvalidTokenWhenDoFilterInternalThenRuntimeException() throws ServletException, IOException {
    // Given
    String accessToken = "EXCEPTION";
    MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/path");
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    MockHttpServletResponse response = new MockHttpServletResponse();

    Mockito.doThrow(new RuntimeException("Something gone wrong while validate accessToken")).when(authorizationServiceMock).validateToken(accessToken);

    // When
    jwtAuthenticationFilterMock.doFilterInternal(request, response, filterChainMock);

    // Then
    Mockito.verify(filterChainMock).doFilter(request, response);
  }

}
