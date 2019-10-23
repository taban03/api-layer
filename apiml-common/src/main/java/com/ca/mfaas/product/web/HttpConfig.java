/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.product.web;

import com.ca.mfaas.security.HttpsConfig;
import com.ca.mfaas.security.HttpsFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
public class HttpConfig {

    private static final Logger log = LoggerFactory.getLogger(HttpConfig.class);

    @Value("${server.ssl.protocol:TLSv1.2}")
    private String protocol;

    @Value("${server.ssl.trustStore:#{null}}")
    private String trustStore;

    @Value("${server.ssl.trustStorePassword:#{null}}")
    private String trustStorePassword;

    @Value("${server.ssl.trustStoreType:PKCS12}")
    private String trustStoreType;

    @Value("${server.ssl.keyAlias:#{null}}")
    private String keyAlias;

    @Value("${server.ssl.keyStore:#{null}}")
    private String keyStore;

    @Value("${server.ssl.keyStorePassword:#{null}}")
    private String keyStorePassword;

    @Value("${server.ssl.keyPassword:#{null}}")
    private String keyPassword;

    @Value("${server.ssl.keyStoreType:PKCS12}")
    private String keyStoreType;

    @Value("${apiml.security.ssl.verifySslCertificatesOfServices:true}")
    private boolean verifySslCertificatesOfServices;

    @Value("${spring.application.name}")
    private String serviceId;

    @Value("${server.ssl.trustStoreRequired:false}")
    private boolean trustStoreRequired;

    @Value("${eureka.client.serviceUrl.defaultZone}")
    private String eurekaServerUrl;

    @Value("${server.ssl.enabled}")
    private String serverSslEnabled;

    private CloseableHttpClient secureHttpClient;
    private SSLContext secureSslContext;
    private HostnameVerifier secureHostnameVerifier;
    //private EurekaJerseyClientBuilder eurekaJerseyClientBuilder;


    @ConditionalOnProperty(
        prefix = "server.ssl",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
    private class SecureOne {

        @PostConstruct
        public void initHttps() {
            try {
                HttpsConfig httpsConfig = HttpsConfig.builder().protocol(protocol).keyAlias(keyAlias).keyStore(keyStore).keyPassword(keyPassword)
                    .keyStorePassword(keyStorePassword).keyStoreType(keyStoreType).trustStore(trustStore)
                    .trustStoreType(trustStoreType).trustStorePassword(trustStorePassword).trustStoreRequired(trustStoreRequired)
                    .verifySslCertificatesOfServices(verifySslCertificatesOfServices).build();

                log.info("Using HTTPS configuration: {}", httpsConfig.toString());

                HttpsFactory factory = new HttpsFactory(httpsConfig);
                secureHttpClient = factory.createSecureHttpClient();
                secureSslContext = factory.createSslContext();
                secureHostnameVerifier = factory.createHostnameVerifier();
                //eurekaJerseyClientBuilder = factory.createEurekaJerseyClientBuilder(eurekaServerUrl, serviceId);

                factory.setSystemSslProperties();
            } catch (Exception e) {
                log.error("Error in HTTPS configuration: {}", e.getMessage(), e);
                System.exit(1); // NOSONAR
            }
        }
    }

    @ConditionalOnProperty(
        prefix = "server.ssl",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true)
    private class UnsecureOne {

        @PostConstruct
        public void initHttp() {
            try {
                HttpsConfig httpsConfig = HttpsConfig.builder().protocol(protocol).keyAlias(keyAlias).keyStore(keyStore).keyPassword(keyPassword)
                    .keyStorePassword(keyStorePassword).keyStoreType(keyStoreType).trustStore(trustStore)
                    .trustStoreType(trustStoreType).trustStorePassword(trustStorePassword).trustStoreRequired(trustStoreRequired)
                    .verifySslCertificatesOfServices(verifySslCertificatesOfServices).build();

                log.info("Using HTTPS configuration: {}", httpsConfig.toString());

                HttpsFactory factory = new HttpsFactory(httpsConfig);
                //secureHttpClient = factory.createSecureHttpClient();
                //secureSslContext = factory.createSslContext();
                //secureHostnameVerifier = factory.createHostnameVerifier();
                //eurekaJerseyClientBuilder = factory.createEurekaJerseyClientBuilder(eurekaServerUrl, serviceId);

                factory.setSystemSslProperties();
            } catch (Exception e) {
                log.error("Error in HTTPS configuration: {}", e.getMessage(), e);
                System.exit(1); // NOSONAR
            }
        }
    }
/*
    @Bean
    @ConditionalOnProperty(
        value = "server.ssl.enabled",
        havingValue = "true",
        matchIfMissing = true)
    public SslContextFactory jettySslContextFactory() {
        SslContextFactory sslContextFactory = new SslContextFactory(SecurityUtils.replaceFourSlashes(keyStore));
        sslContextFactory.setProtocol(protocol);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyStoreType(keyStoreType);
        sslContextFactory.setCertAlias(keyAlias);

        if (trustStore != null) {
            sslContextFactory.setTrustStorePath(SecurityUtils.replaceFourSlashes(trustStore));
            sslContextFactory.setTrustStoreType(trustStoreType);
            sslContextFactory.setTrustStorePassword(trustStorePassword);
        }
        log.debug("jettySslContextFactory: {}", sslContextFactory.dump());

        if (!verifySslCertificatesOfServices) {
            sslContextFactory.setTrustAll(true);
        }

        return sslContextFactory;
    }
*/

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = null;
        if (secureHttpClient != null) {
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(secureHttpClient);
            template = new RestTemplate(factory);
        } else {
            template = new RestTemplate();
        }

        return template;
    }

    /*@Bean
    public CloseableHttpClient secureHttpClient() {
        return secureHttpClient;
    }*/

    /*@Bean
    public SSLContext secureSslContext() {
        return secureSslContext;
    }*/

    /*@Bean
    public HostnameVerifier secureHostnameVerifier() {
        return secureHostnameVerifier;
    }*/

    /*public static class MyPropertyCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return false;//... place your root detecting logic here ...
        }
    }*/
}
