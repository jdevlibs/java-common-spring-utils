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
package io.github.jdevlibs.spring.client.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */

@EqualsAndHashCode(of = {"id"})
@Data
public class Request implements Serializable {
    private String id;
    private Map<String, String> headers;

    public void addHeader(String name, String value) {
        if (name == null || name.isEmpty()) {
            return;
        }
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    public void authorizationBearer(String token) {
        addHeader("Authorization", "Bearer " + token);
    }

    public void authorizationBasic(String token) {
        addHeader("Authorization", "Basic " + token);
    }

    public void clearHeader() {
        headers = null;
    }

}