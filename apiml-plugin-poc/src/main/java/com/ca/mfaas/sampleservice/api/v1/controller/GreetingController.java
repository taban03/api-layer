/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package com.ca.mfaas.sampleservice.api.v1.controller;

import java.util.concurrent.atomic.AtomicLong;

import com.ca.mfaas.sampleservice.api.v1.model.Greeting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Api(
        value = "/api/v1/",
        consumes = "application/json",
        tags = {"The greetings API"}
    )
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    @ApiOperation(
            value = "Shows personalized greeting",
            notes = "Returns personalized greetings information")
    @ApiResponses( value = {
            @ApiResponse(code = 200, message = "Personalized greeting response")
    })
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
}
