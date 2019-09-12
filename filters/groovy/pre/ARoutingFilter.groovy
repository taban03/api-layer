
/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package com.ca.mfaas.gateway.filters.routing;

import java.util.regex.Pattern

import javax.servlet.http.HttpServletResponse

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext

/**
 * Abstract class to return content directly fron Zuul,
 * If this filter is executed, the  "route" filters will be bypassed,
 * so the request will not be forwarded to an origin.
 * the uri() method may return a String or a List of matching URI's.
 * A matching request uri will return the String in the responseBody() method.
 *
 * @author Mikey Cohen
 * Date: 2/2/12
 * Time: 1:34 PM
 */
public abstract class ARoutingFilter extends ZuulFilter {

    /**
     * Define a URI eg /static/content/path or List of URIs for this filter to return a static response.
     * @return String URI or java.util.List of URIs
     */
    abstract def uri()

    abstract String responseBody()

    @Override
    String filterType() {
        return "static"
    }

    @Override
    int filterOrder() {
        return 0
    }

    boolean shouldFilter() {
        String path = RequestContext.currentContext.getRequest().getRequestURI()
        if (checkPath(path)) return true
        if (checkPath("/" + path)) return true
        return false
    }

    /**
     * checks if the path matches the uri()
     * @param path usually the RequestURI()
     * @return true if the pattern matches
     */
    boolean checkPath(String path) {
        def uri = uri()
        if (uri instanceof String) {
            return uri.equals(path)
        } else if (uri instanceof List) {
            return uri.contains(path)
        } else if (uri instanceof Pattern) {
            return uri.matcher(path).matches();
        }
        return false;
    }

    @Override
    Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // Set the default response code for static filters to be 200
        ctx.setResponseStatusCode(HttpServletResponse.SC_OK)
        // first StaticResponseFilter instance to match wins, others do not set body and/or status
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(responseBody())
            ctx.sendZuulResponse = false;
        }
    }

}
