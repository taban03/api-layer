/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.apicatalog.health;

import com.ca.mfaas.product.constants.CoreService;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiCatalogHealthIndicatorTest {

    private final DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
    private final ApiCatalogHealthIndicator apiCatalogHealthIndicator = new ApiCatalogHealthIndicator(discoveryClient);
    private final Health.Builder builder = new Health.Builder();

    @Test
    public void testStatusIsUpWhenGatewayIsAvailable() {
        when(discoveryClient.getInstancesById(CoreService.GATEWAY.getServiceId())).thenReturn(
            Collections.singletonList(
                InstanceInfo.Builder.newBuilder()
                    .setInstanceId(CoreService.GATEWAY.getServiceId())
                    .setAppName(CoreService.GATEWAY.getServiceId())
                    .setStatus(InstanceInfo.InstanceStatus.UP)
                    .setPort(10010)
                    .build()
            ));

        apiCatalogHealthIndicator.doHealthCheck(builder);

        assertEquals(Status.UP, builder.build().getStatus());
    }

    @Test
    public void testStatusIsDownWhenGatewayIsNotAvailable() {
        when(discoveryClient.getInstancesById(CoreService.GATEWAY.getServiceId())).thenReturn(
                Collections.emptyList()
        );

        apiCatalogHealthIndicator.doHealthCheck(builder);

        assertEquals(Status.DOWN, builder.build().getStatus());
    }
}
