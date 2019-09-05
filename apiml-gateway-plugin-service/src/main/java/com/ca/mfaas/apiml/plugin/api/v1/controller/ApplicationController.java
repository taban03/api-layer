/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.apiml.plugin.api.v1.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Api(
        value = "/api/v1/",
        consumes = "application/json",
        tags = {"The application info & health check API"}
    )
public class ApplicationController {

    @Value("${mfaas.catalog-ui-tile.description}")
    private String applicationInfo;

    //@Value("${eureka.client.enabled:true}")
    private boolean enabled = true;

    //@Value("${eureka.client.healthcheck.enabled}")
    private boolean healthCheckEnabled = true;

    @RequestMapping("/info")
    @ApiOperation(
            value = "Shows basic info about this service.",
            notes = "This application provides a sample implementation of on-bourding API service to ZOWE API ML.")
    @ApiResponses( value = {
            @ApiResponse(code = 200, message = "Application info response")
    })
    public String info() {
        return applicationInfo;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    @RequestMapping("/health-check")
    @ApiOperation(
            value = "Shows health-check status of this service.",
            notes = "This service health-check status.")
    @ApiResponses( value = {
            @ApiResponse(code = 200, message = "Application health-check response")
    })
    public Boolean healthCheck() {
        return healthCheckEnabled;
    }
}
