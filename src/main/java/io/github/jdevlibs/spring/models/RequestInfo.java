package io.github.jdevlibs.spring.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
@Getter
@Setter
@ToString
public class RequestInfo implements Serializable {
    private String url;
    private String method;
    private String client;
    private String sessionId;
    private Map<String, Object> headers;
    private Map<String, String> cookies;

    public void addHeader(String name, Object value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    public void addCookie(String name, String value) {
        if (cookies == null) {
            cookies = new HashMap<>();
        }
        cookies.put(name, value);
    }
}
