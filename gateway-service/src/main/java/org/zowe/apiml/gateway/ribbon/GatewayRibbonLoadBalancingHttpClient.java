/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.gateway.ribbon;

import java.net.URI;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.loadbalancer.Server;

import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;
import org.zowe.apiml.gateway.security.service.ServiceCacheEvict;

public interface GatewayRibbonLoadBalancingHttpClient extends ServiceInstanceChooser, ServiceCacheEvict {

    public InstanceInfo putInstanceInfo(String serviceId, String instanceId, InstanceInfo instanceInfo);

    public InstanceInfo getInstanceInfo(String serviceId, String instanceId);

    public URI reconstructURIWithServer(Server server, URI request);
}
