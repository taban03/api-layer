/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.gateway.security.login.zosmf;

import org.zowe.apiml.security.common.config.AuthConfigurationProperties;
import org.zowe.apiml.security.common.error.ServiceNotAccessibleException;
import org.zowe.apiml.gateway.security.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZosmfAuthenticationProviderTest {
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String SERVICE_ID = "service";
    private static final String HOST = "localhost";
    private static final int PORT = 0;
    private static final String ZOSMF = "zosmf";
    private static final String COOKIE1 = "JwtToken=test";
    private static final String COOKIE2 = "LtpaToken2=test";
    private static final String DOMAIN = "realm";
    private static final String RESPONSE = "{\"zosmf_saf_realm\": \"" + DOMAIN + "\"}";

    private UsernamePasswordAuthenticationToken usernamePasswordAuthentication;
    private AuthConfigurationProperties authConfigurationProperties;
    private DiscoveryClient discovery;
    private ObjectMapper mapper;
    private RestTemplate restTemplate;
    private ServiceInstance zosmfInstance;
    private AuthenticationService authenticationService;


    @BeforeEach
    public void setUp() {
        usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
        authConfigurationProperties = new AuthConfigurationProperties();
        discovery = mock(DiscoveryClient.class);
        authenticationService = mock(AuthenticationService.class);
        mapper = new ObjectMapper();
        restTemplate = mock(RestTemplate.class);
        zosmfInstance = new DefaultServiceInstance(SERVICE_ID, HOST, PORT, false);
    }

    @Test
    public void loginWithExistingUser() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, COOKIE1);
        headers.add(HttpHeaders.SET_COOKIE, COOKIE2);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenReturn(new ResponseEntity<>(RESPONSE, headers, HttpStatus.OK));

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Authentication tokenAuthentication
            = zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication);

        assertTrue(tokenAuthentication.isAuthenticated());
        assertEquals(USERNAME, tokenAuthentication.getPrincipal());
    }

    @Test
    public void loginWithBadUser() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        HttpHeaders headers = new HttpHeaders();
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenReturn(new ResponseEntity<>(RESPONSE, headers, HttpStatus.OK));

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(BadCredentialsException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not BadCredentialsException");
        assertEquals("Username or password are invalid.", exception.getMessage());

    }

    @Test
    public void noZosmfInstance() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);
        List<ServiceInstance> zosmfInstances = Collections.singletonList(null);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(ServiceNotAccessibleException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not ServiceNotAccessibleException");
        assertEquals("z/OSMF instance not found or incorrectly configured.", exception.getMessage());

    }

    @Test
    public void noZosmfServiceId() {
        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(AuthenticationServiceException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not AuthenticationServiceException");
        assertEquals("The parameter 'zosmfServiceId' is not configured.", exception.getMessage());

    }

    @Test
    public void notValidZosmfResponse() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, COOKIE1);
        headers.add(HttpHeaders.SET_COOKIE, COOKIE2);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenReturn(new ResponseEntity<>("", headers, HttpStatus.OK));

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(AuthenticationServiceException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not AuthenticationServiceException");
        assertEquals("z/OSMF domain cannot be read.", exception.getMessage());

    }

    @Test
    public void noDomainInResponse() {
        String invalidResponse = "{\"saf_realm\": \"" + DOMAIN + "\"}";

        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, COOKIE1);
        headers.add(HttpHeaders.SET_COOKIE, COOKIE2);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenReturn(new ResponseEntity<>(invalidResponse, headers, HttpStatus.OK));

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(AuthenticationServiceException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not AuthenticationServiceException");
        assertEquals("z/OSMF domain cannot be read.", exception.getMessage());

    }

    @Test
    public void invalidCookieInResponse() {
        String invalidCookie = "LtpaToken=test";

        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, invalidCookie);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenReturn(new ResponseEntity<>(RESPONSE, headers, HttpStatus.OK));

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(BadCredentialsException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not BadCredentialsException");
        assertEquals("Username or password are invalid.", exception.getMessage());

    }

    @Test
    public void cookieWithSemicolon() {
        String cookie = "LtpaToken2=test;";

        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenReturn(new ResponseEntity<>(RESPONSE, headers, HttpStatus.OK));

        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Authentication tokenAuthentication = zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication);

        assertTrue(tokenAuthentication.isAuthenticated());
        assertEquals(USERNAME, tokenAuthentication.getPrincipal());
    }

    @Test
    public void shouldThrowNewExceptionIfRestClientException() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenThrow(RestClientException.class);
        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(AuthenticationServiceException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not AuthenticationServiceException");
        assertEquals("A failure occurred when authenticating.", exception.getMessage());


    }

    @Test
    public void shouldThrowNewExceptionIfResourceAccessException() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);
        when(restTemplate.exchange(Mockito.anyString(),
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.<Class<Object>>any()))
            .thenThrow(ResourceAccessException.class);
        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        Exception exception = assertThrows(ServiceNotAccessibleException.class,
            () -> zosmfAuthenticationProvider.authenticate(usernamePasswordAuthentication),
            "Expected exception is not ServiceNotAccessibleException");
        assertEquals("Could not get an access to z/OSMF service.", exception.getMessage());


    }

    @Test
    public void shouldReturnTrueWhenSupportMethodIsCalledWithCorrectClass() {
        authConfigurationProperties.setZosmfServiceId(ZOSMF);

        List<ServiceInstance> zosmfInstances = Collections.singletonList(zosmfInstance);
        when(discovery.getInstances(ZOSMF)).thenReturn(zosmfInstances);
        ZosmfAuthenticationProvider zosmfAuthenticationProvider
            = new ZosmfAuthenticationProvider(authConfigurationProperties, authenticationService, discovery, mapper, restTemplate);

        boolean supports = zosmfAuthenticationProvider.supports(usernamePasswordAuthentication.getClass());
        assertTrue(supports);

    }
}
