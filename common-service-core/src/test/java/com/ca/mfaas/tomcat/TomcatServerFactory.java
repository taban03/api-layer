/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.tomcat;

import static org.junit.Assert.assertEquals;

import com.ca.mfaas.security.HttpsConfig;
import com.ca.mfaas.security.HttpsFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class TomcatServerFactory {
    private final static String SERVLET_NAME = "hello";

    public Tomcat startTomcat(HttpsConfig httpsConfig) throws IOException {
        Tomcat tomcat = new Tomcat();
        String contextPath = new File(".").getCanonicalPath();
        log.info("Tomcat contextPath: {}", contextPath);
        Context ctx = tomcat.addContext("", contextPath);
        tomcat.setConnector(createHttpsConnector(httpsConfig));
        Tomcat.addServlet(ctx, SERVLET_NAME, new HttpServlet() {
            private static final long serialVersionUID = 3405324813032378347L;

            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/plain");
                try (Writer writer = response.getWriter()) {
                    writer.write("OK");
                    writer.flush();
                }
            }
        });
        ctx.addServletMappingDecoded("/*", SERVLET_NAME);
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        return tomcat;
    }

    private Connector createHttpsConnector(HttpsConfig httpsConfig) {
        Connector httpsConnector = new Connector();
        httpsConnector.setPort(0);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keystoreFile", httpsConfig.getKeyStore());
        httpsConnector.setAttribute("clientAuth", Boolean.toString(httpsConfig.isClientAuth()));
        httpsConnector.setAttribute("keystorePass", httpsConfig.getKeyPassword());
        httpsConnector.setAttribute("sslProtocol", httpsConfig.getProtocol());
        httpsConnector.setAttribute("SSLEnabled", true);
        return httpsConnector;
    }

    static int getLocalPort(Tomcat tomcat) {
        Service[] services = tomcat.getServer().findServices();
        for (Service service : services) {
            for (Connector connector : service.findConnectors()) {
                ProtocolHandler protocolHandler = connector.getProtocolHandler();
                if (protocolHandler instanceof Http11AprProtocol || protocolHandler instanceof Http11NioProtocol) {
                    return connector.getLocalPort();
                }
            }
        }
        return 0;
    }

    public static void main(String[] args) throws LifecycleException, ClientProtocolException, IOException {
        log.debug("Cwd: {}", System.getProperty("user.dir"));

        HttpsConfig httpsConfig = HttpsConfig.builder()
                .keyStore(new File("keystore/localhost/localhost.keystore.p12").getCanonicalPath())
                .keyStorePassword("password").keyPassword("password")
                .trustStore(new File("keystore/localhost/localhost.truststore.p12").getCanonicalPath())
                .trustStorePassword("password").protocol("TLSv1.2").build();
        HttpsFactory httpsFactory = new HttpsFactory(httpsConfig);

        Tomcat tomcat = new TomcatServerFactory().startTomcat(httpsConfig);
        try {
            HttpClient client = httpsFactory.createSecureHttpClient();

            int port = getLocalPort(tomcat);
            HttpGet get = new HttpGet(String.format("https://localhost:%d", port));
            HttpResponse response = client.execute(get);

            String responseBody = EntityUtils.toString(response.getEntity());

            assertEquals(200, response.getStatusLine().getStatusCode());
            assertEquals("OK", responseBody);
        } finally {
            tomcat.stop();
        }
    }
}