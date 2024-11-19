package io.github.jdevlibs.spring.utils;

import io.github.jdevlibs.spring.models.RequestInfo;
import io.github.jdevlibs.utils.Validators;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

public final class HttpUtils {
    private static final String PARAM_TOKEN = "token";
    private static final String TOKEN_BEARER = "Bearer ";
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private HttpUtils() {

    }

    /**
     * Get Request information
     * @return The information of request
     */
    public static RequestInfo getRequestInfo() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return new RequestInfo();
        }

        return getRequestInfo(requestAttributes.getRequest());
    }

    /**
     * Get Request information
     * @param req HttpServletRequest
     * @return The information of request
     */
    public static RequestInfo getRequestInfo(final HttpServletRequest req) {
        RequestInfo requestInfo = new RequestInfo();
        if (Validators.isNull(req)) {
            return requestInfo;
        }

        requestInfo.setUrl(getRequestPath());
        requestInfo.setMethod(req.getMethod());
        requestInfo.setClient(getRequestIPAddress(req));
        if (Validators.isNotNull(req.getSession())) {
            requestInfo.setSessionId(req.getSession().getId());
        }

        List<String> headers = getHeaderNames(req);
        if (Validators.isNotEmpty(headers)) {
            for (String header : headers) {
                requestInfo.addHeader(header, req.getHeader(header));
            }
        }

        List<Cookie> cookies = getCookies(req);
        if (Validators.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                requestInfo.addCookie(cookie.getName(), cookie.getValue());
            }
        }

        return requestInfo;
    }

    /**
     * Returns a list of all the header names this request contains.
     * If the request has no headers, this method returns an empty.
     *
     * @return a list of all the header names.
     */
    public static List<String> getHeaderNames() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return Collections.emptyList();
        }
        return getHeaderNames(requestAttributes.getRequest());
    }

    /**
     * Returns a list of all the header names this request contains.
     * If the request has no headers, this method returns an empty.
     *
     * @param req HttpServletRequest
     * @return a list of all the header names.
     */
    public static List<String> getHeaderNames(final HttpServletRequest req) {
        if (req == null) {
            return Collections.emptyList();
        }

        Enumeration<String> headerNames = req.getHeaderNames();
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
     * Returns the value of the specified request header as a String.
     *
     * @param name The specifying the header name
     * @return a value pf header name, If the request did not include return null.
     */
    public static String getHeader(String name) {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        return getHeader(requestAttributes.getRequest(), name);
    }

    /**
     * Returns the value of the specified request header as a String.
     * @param req HttpServletRequest
     * @param name The header key name
     * @return a value pf header name, If the request did not include return null.
     */
    public static String getHeader(final HttpServletRequest req, String name) {
        if (req == null) {
            return null;
        }

        return req.getHeader(name);
    }

    /**
     * List of all cookies
     * @return The list of cookies
     */
    public static List<Cookie> getCookies() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        return getCookies(requestAttributes.getRequest());
    }

    /**
     * List of all cookies
     * @param request HttpServletRequest
     * @return The list of cookies
     */
    public static List<Cookie> getCookies(final HttpServletRequest request) {
        if (Validators.isNull(request)) {
            return Collections.emptyList();
        }
        if (Validators.isEmpty(request.getCookies())) {
            return Collections.emptyList();
        }
        return Arrays.stream(request.getCookies()).toList();
    }

    /**
     * Get cookie by name
     * @param name The cookie name
     * @return The Cookie
     */

    public static Cookie getCookie(String name) {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        return getCookie(requestAttributes.getRequest(), name);
    }

    /**
     * Get cookie by name
     * @param request HttpServletRequest
     * @param name The cookie name
     * @return The Cookie
     */
    public static Cookie getCookie(final HttpServletRequest request, String name) {
        if (Validators.isNullOne(request, name)) {
            return null;
        }
        if (Validators.isEmpty(request.getCookies())) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findAny().orElse(null);
    }

    /**
     * Get cookie by name
     * @param name The cookie name
     * @return The cookie value.
     */
    public static String getCookieValue(String name) {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        return getCookieValue(requestAttributes.getRequest(), name);
    }

    /**
     * Get cookie by name
     * @param request HttpServletRequest
     * @param name The cookie name
     * @return The cookie value.
     */
    public static String getCookieValue(final HttpServletRequest request, String name) {
        if (Validators.isNullOne(request, name)) {
            return null;
        }
        if (Validators.isEmpty(request.getCookies())) {
            return null;
        }
        
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findAny().orElse(null);
    }

    /**
     * Get current Authorization token (remove 'Bearer' prefix when exists)
     * <pre>
     *     1. get value from header "Authorization" example : Authorization: Bearer xyz-token
     *     2. If cannot get from Authorization get from parameter name 'token' or 'accessToken'
     * </pre>
     *
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
     *
     * @param req The HttpServletRequest
     * @return Current Authorization token, Or null when not found.
     */
    public static String getBearerToken(final HttpServletRequest req) {
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
     * Get an active locale from http header AcceptLanguage
     *
     * @return Active locale, If null return system default
     */
    public static Locale getCurrentLocale() {
        return getCurrentLocale(Locale.getDefault());
    }

    /**
     * Get an active locale from http header AcceptLanguage
     *
     * @param defaultLocale The default locale
     * @return Active locale, If null return input default
     */
    public static Locale getCurrentLocale(Locale defaultLocale) {
        try {
            String lang = getAcceptLanguage();
            if (lang == null || lang.isEmpty()) {
                return defaultLocale;
            }
            return new Locale(lang);
        } catch (Exception ex) {
            return defaultLocale;
        }
    }

    /**
     * Get AcceptLanguage, If the request has no set, this method returns null.
     *
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
     *
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
     * Get AcceptLanguage If the request has no set returns null.
     *
     * @param req The HttpServletRequest
     * @return Current AcceptLanguage, Or null when not found.
     */
    public static String getAcceptLanguage(HttpServletRequest req) {
        return getAcceptLanguage(req, null);
    }

    /**
     * Get AcceptLanguage If the request has no set returns default value.
     *
     * @param req             The HttpServletRequest
     * @param defaultLanguage the default language
     * @return Current AcceptLanguage, Or default value when not found.
     */
    public static String getAcceptLanguage(HttpServletRequest req, String defaultLanguage) {
        if (Validators.isNull(req)) {
            return defaultLanguage;
        }
        return getLanguage(req, defaultLanguage);
    }

    /**
     * Get the current request path If the request has no set returns null.
     *
     * @return current request path
     */
    public static String getRequestPath() {
        try {
            return ServletUriComponentsBuilder.fromCurrentRequestUri().build().getPath();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getRequestIPAddress() {
        ServletRequestAttributes requestAttributes = getServletRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        return getRequestIPAddress(requestAttributes.getRequest());
    }

    public static String getRequestIPAddress(HttpServletRequest request) {
        if (Validators.isNull(request)) {
            return null;
        }
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (Validators.isEmpty(value)) {
                continue;
            }
            String[] parts = value.split("\\s*,\\s*");
            return parts[0];
        }
        return request.getRemoteAddr();
    }

    private static ServletRequestAttributes getServletRequestAttributes() {
        try {
            return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private static String getLanguage(HttpServletRequest req, String defaultLanguage) {
        if (Validators.isNull(req)) {
            return defaultLanguage;
        }
        String language = req.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (Validators.isEmpty(language)) {
            return defaultLanguage;
        } else {
            String[] lang = language.split(",");
            return lang[0];
        }
    }
}
