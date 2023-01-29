/*  ---------------------------------------------------------------------------
 *  * Copyright 2020-2021 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class Transformers {
    private Transformers() {
    }

    /**
     * Transformer query result to POJO (Java Bean)
     * @param clazz The target class for transformer
     * @return The result of class after mapping
     * @param <T> Generic target class
     */
    public static <T> RowMapper<T> toBean(Class<T> clazz) {
        return new NestedBeanMapper<>(clazz);
    }
}
