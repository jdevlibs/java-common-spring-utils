/*
 * ---------------------------------------------------------------------------
 *  Copyright (c)  2023-2023.  the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ---------------------------------------------------------------------------
 */

package io.github.jdevlibs.spring.utils;

import io.github.jdevlibs.utils.Validators;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author supot.jdev
 * @version 1.0
 */
public final class SpringUtils {
    private static final String PARAM_TOKEN 	= "token";
    private static final String TOKEN_BEARER 	= "Bearer ";

    private SpringUtils() {

    }

    /**
     * Returns a list of all the header names this request contains.
     * If the request has no headers, this method returns an empty.
     * @return a list of all the header names.
     */
    public static List<String> getHeaderNames() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return Collections.emptyList();
        }

        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        if (Validators.isEmpty(headerNames)) {
            return Collections.emptyList();
        }

        List<String> headers = new ArrayList<>();
        while (headerNames.hasMoreElements()) {
            headers.add(headerNames.nextElement());
        }
        return headers;
    }

    /**
     * Get current Authorization token (remove 'Bearer' prefix when exists)
     * <pre>
     *     1. get value from header "Authorization" example : Authorization: Bearer xyz-token
     *     2. If cannot get from Authorization get from parameter name 'token' or 'accessToken'
     * </pre>
     * @return Current Authorization token, Or null when not found.
     */
    public static String getBearerToken() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return getBearerToken(requestAttributes.getRequest());
    }

    /**
     * Get current Authorization token (remove 'Bearer' prefix when exists)
     * <pre>
     *     1. get value from header "Authorization" example : Authorization: Bearer xyz-token
     *     2. If cannot get from Authorization get from parameter name 'token' or 'accessToken'
     * </pre>
     * @param req The HttpServletRequest
     * @return Current Authorization token, Or null when not found.
     */
    public static String getBearerToken(HttpServletRequest req) {
        if (Validators.isNull(req)) {
            return null;
        }

        String token = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (Validators.isNotEmpty(token) && token.startsWith(TOKEN_BEARER)) {
            token = token.substring(7);
        } else {
            token = req.getParameter(PARAM_TOKEN);
            if (Validators.isEmpty(token)) {
                token = req.getParameter("accessToken");
            }
        }
        return token;
    }

    /**
     * Get AcceptLanguage, If the request has no set, this method returns null.
     * @return Current AcceptLanguage, Or null when not found.
     */
    public static String getAcceptLanguage() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return getAcceptLanguage(requestAttributes.getRequest(), null);
    }

    /**
     * Get AcceptLanguage, If the request has no set, this method returns null.
     * @param defaultLanguage the default language
     * @return Current AcceptLanguage, Or null when not found.
     */
    public static String getAcceptLanguage(String defaultLanguage) {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return getAcceptLanguage(requestAttributes.getRequest(), defaultLanguage);
    }

    /**
     * Get AcceptLanguage, If the request has no set returns null.
     * @param req The HttpServletRequest
     * @return Current AcceptLanguage, Or null when not found.
     */
    public static String getAcceptLanguage(HttpServletRequest req) {
        return getAcceptLanguage(req, null);
    }

    /**
     * Get AcceptLanguage, If the request has no set returns default value.
     * @param req The HttpServletRequest
     * @param defaultLanguage the default language
     * @return Current AcceptLanguage, Or default value when not found.
     */
    public static String getAcceptLanguage(HttpServletRequest req, String defaultLanguage) {
        if (Validators.isNull(req)) {
            return defaultLanguage;
        }
        String language = req.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        return (Validators.isNotEmpty(language) ? language : defaultLanguage);
    }

    /**
     * Get current request path, If the request has no set returns null.
     * @return current request path
     */
    public static String getRequestPath() {
        try {
            return ServletUriComponentsBuilder.fromCurrentRequestUri().build().getPath();
        } catch (Exception ex) {
            return null;
        }
    }
    private static ServletRequestAttributes getServletRequestAttributes() {
        try {
            return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        } catch (IllegalStateException ex) {
            return null;
        }
    }
}
