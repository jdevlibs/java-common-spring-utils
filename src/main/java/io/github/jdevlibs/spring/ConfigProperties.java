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

package io.github.jdevlibs.spring;

/**
 * @author supot.jdev
 * @version 1.0
 */
public final class ConfigProperties {
    private ConfigProperties(){

    }
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String JSON_DATE_FORMAT_KEY = "conf.json.date.format";

    public static String getJsonDateFormat() {
        return System.getProperty(JSON_DATE_FORMAT_KEY, JSON_DATE_FORMAT);
    }

    public static String getConfigValue(String property) {
        return System.getProperty(property);
    }
}
