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

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class GroovyFiltersInitializer {
    private Logger logger = LoggerFactory.getLogger(GroovyFiltersInitializer.class);

    @Autowired
    private CustomPathZuulFilterConfig config;

    @PostConstruct
    private void initGroovyFilters() throws Exception {

        List<String> groovyFiltersPath = config.getGroovyFiltersPath();

        if (groovyFiltersPath == null || groovyFiltersPath.size() == 0) {
            return;
        }

        FilterLoader.getInstance().setCompiler(new GroovyCompiler());
        FilterFileManager.setFilenameFilter(new GroovyFileFilter());

        String[] filterDirectoryList = groovyFiltersPath.toArray(new String[0]);
        FilterFileManager.init(5, filterDirectoryList);

        logger.info("Groovy Filter file manager started");
    }
}
