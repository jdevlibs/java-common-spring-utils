package io.github.jdevlibs.spring.client.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author supot.jdev
 * @version 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JsonRequest<T> extends Request {
    private T model;

    public boolean isJsonString() {
        return (model instanceof String);
    }
}
