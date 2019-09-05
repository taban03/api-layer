/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.sampleservice.config;


import com.ca.mfaas.enable.EnableApiDiscovery;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableApiDiscovery
@ComponentScan({"com.ca.mfaas.enable", "com.ca.mfaas.product"})
public class ApiMlEnablerConfig {
}
