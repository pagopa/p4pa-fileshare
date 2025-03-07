package it.gov.pagopa.pu.fileshare.util;

import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

public class TestUtils {

  private TestUtils(){}

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field "+f.getName()+" of the input object of type "+o.getClass()+" is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

  public static void addSampleUserIntoSecurityContext(){
    UserInfo userInfo = getSampleUser();

    Collection<? extends GrantedAuthority> authorities = null;
    if (userInfo.getOrganizationAccess() != null) {
      authorities = userInfo.getOrganizations().stream()
        .filter(o -> userInfo.getOrganizationAccess().equals(o.getOrganizationIpaCode()))
        .flatMap(r -> r.getRoles().stream())
        .map(SimpleGrantedAuthority::new)
        .toList();
    }
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userInfo, "token", authorities);
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  public static UserInfo getSampleAdminUser(){
    return getSampleUser(true, AuthorizationService.ROLE_ADMIN);
  }

  public static UserInfo getSampleUser(){
    return getSampleUser(true, "ROLE_OPER");
  }

  public static UserInfo getSampleUser(Boolean organizationAccess, String role){
    List<UserOrganizationRoles> organizations = List.of(
      new UserOrganizationRoles()
        .operatorId("operator1")
        .organizationId(1L)
        .organizationIpaCode("ORG")
        .email("email1@example.com")
        .roles(List.of(role)),
      new UserOrganizationRoles()
        .operatorId("operator2")
        .organizationId(2L)
        .organizationIpaCode("ORG2")
        .email("email2@example.com")
        .roles(List.of(role))
    );
    return new UserInfo().mappedExternalUserId("MAPPEDEXTERNALUSERID")
      .fiscalCode("FISCALCODE")
      .familyName("FAMILYNAME")
      .name("NAME")
      .issuer("ISSUER")
      .organizationAccess(organizationAccess?"ORG":null)
      .organizations(organizations);
  }

}
