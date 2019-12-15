/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.client;

import com.ca.apiml.enable.EnableApiDiscovery;
import com.ca.mfaas.service.ServiceStartupEventHandler;
import com.ca.mfaas.product.version.BuildInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.annotation.Nonnull;

@SpringBootApplication
@EnableApiDiscovery
@EnableConfigurationProperties
@EnableWebSocket
@ComponentScan(value = {
    "com.ca.mfaas.client"})
public class DiscoverableClientSampleApplication implements ApplicationListener<ApplicationReadyEvent> {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DiscoverableClientSampleApplication.class);
        System.getProperties().setProperty("LatencyUtils.useActualTime", "false");
        app.setLogStartupInfo(false);
        new BuildInfo().logBuildInfo();
        app.run(args);
    }

    @Override
    public void onApplicationEvent(@Nonnull final ApplicationReadyEvent event) {
        new ServiceStartupEventHandler().onServiceStartup("Discoverable Client Service",
            ServiceStartupEventHandler.DEFAULT_DELAY_FACTOR);
    }
}
