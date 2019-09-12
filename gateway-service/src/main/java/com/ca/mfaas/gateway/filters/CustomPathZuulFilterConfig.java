/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package com.ca.mfaas.gateway.filters;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "zuul")
public class CustomPathZuulFilterConfig {

    private List<String> groovyFiltersPath;

    public List<String> getGroovyFiltersPath() {
        return groovyFiltersPath;
    }

    public void setGroovyFiltersPath(List<String> groovyFiltersPath) {
        this.groovyFiltersPath = groovyFiltersPath;
    }
}
