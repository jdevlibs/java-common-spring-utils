package io.github.jdevlibs.spring.client.request;

import lombok.*;

/**
 * @author supot.jdev
 * @version 1.0
 */
@Setter
@Getter
@ToString(callSuper = true)
public class JsonRequest<T> extends Request {
    private T model;

    public boolean isJsonString() {
        return (model instanceof String);
    }
}
