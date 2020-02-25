/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.discovery;

import org.zowe.apiml.message.core.MessageService;
import org.zowe.apiml.message.log.ApimlLogger;
import org.zowe.apiml.util.EurekaUtils;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * This component is responsible for notifying all gateways about service changes. It is the easiest way how discovery
 * service sends information about a change to all gateways to make a delay between registration and stoping of service
 * and using or releasing on gateways sides the shortest.
 */
@Component
public class GatewayNotifier {

    private final ApimlLogger logger;

    private final RestTemplate restTemplate;

    public GatewayNotifier(RestTemplate restTemplate, MessageService messageService) {
        this.restTemplate = restTemplate;
        this.logger = ApimlLogger.of(GatewayNotifier.class, messageService);
    }

    private EurekaServerContext getServerContext() {
        return EurekaServerContextHolder.getInstance().getServerContext();
    }

    private PeerAwareInstanceRegistry getRegistry() {
        return getServerContext().getRegistry();
    }

    /**
     * Method notify all gateways about any change of service serviceId. This method should be call immediately after
     * any modification of service instance happened.
     *
     * @param serviceId id of modified service
     */
    public void serviceUpdated(String serviceId) {
        final PeerAwareInstanceRegistry registry = getRegistry();
        final Application application = registry.getApplication("GATEWAY");
        if (application == null) {
            logger .log("org.zowe.apiml.discovery.errorNotifyingGateway");
            return;
        }

        final List<InstanceInfo> gatewayInstances = application.getInstances();

        for (final InstanceInfo instanceInfo : gatewayInstances) {
            final StringBuilder url = new StringBuilder();
            url.append(EurekaUtils.getUrl(instanceInfo)).append("/cache/services");
            if (serviceId != null)
                url.append('/').append(serviceId);
            restTemplate.delete(url.toString());
        }
    }

}
