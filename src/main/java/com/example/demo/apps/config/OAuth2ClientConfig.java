package com.example.demo.apps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2ClientConfig {

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    ClientRegistration keycloakRegistration = ClientRegistration
        .withRegistrationId("keycloak")
        .clientName("Demo")
        .clientId("demo")
        .clientSecret("M912foJziFKqmG1LdBYO3SWLOSwcaEMa")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://localhost:8083/login/oauth2/code/keycloak")
        .scope("openid", "profile", "email")
        .authorizationUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/auth")
        .tokenUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/token")
        .userInfoUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/userinfo")
        .jwkSetUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/certs")
        .userNameAttributeName("preferred_username")
        .build();

    return new InMemoryClientRegistrationRepository(keycloakRegistration);
  }
}
