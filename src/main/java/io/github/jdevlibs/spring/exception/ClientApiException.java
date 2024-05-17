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
package io.github.jdevlibs.spring.exception;

import io.github.jdevlibs.spring.utils.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class ClientApiException extends RuntimeException {
    private static final int ERROR_CODE = 500;
    private final ClientApiErrorCodes error;
    private final int httpCode;
    private final String responseBody;

    public ClientApiException(Throwable cause) {
        super(cause);
        this.error = ClientApiErrorCodes.CODE_API_ERROR;
        this.httpCode = ERROR_CODE;
        this.responseBody = null;
    }

    public ClientApiException(String message) {
        super(message);
        this.error = ClientApiErrorCodes.CODE_API_ERROR;
        this.httpCode = ERROR_CODE;
        this.responseBody = null;
    }

    public ClientApiException(Throwable cause, String message) {
        super(message, cause);
        this.error = ClientApiErrorCodes.CODE_API_ERROR;
        this.httpCode = ERROR_CODE;
        this.responseBody = null;
    }

    public ClientApiException(Throwable cause, String message, int code) {
        super(message, cause);
        this.error = ClientApiErrorCodes.CODE_API_ERROR;
        this.httpCode = code;
        this.responseBody = null;
    }

    public ClientApiException(Throwable cause, String message, int code, String errorBody) {
        super(message, cause);
        this.error = ClientApiErrorCodes.CODE_API_ERROR;
        this.httpCode = code;
        this.responseBody = errorBody;
    }

    public ClientApiException(ClientApiErrorCodes errorCode, String message) {
        super(message);
        this.error = errorCode;
        this.httpCode = ERROR_CODE;
        this.responseBody = null;
    }

    public ClientApiException(ClientApiErrorCodes errorCode, int code, String message) {
        super(message);
        this.error = errorCode;
        this.httpCode = code;
        this.responseBody = null;
    }

    public ClientApiException(ClientApiErrorCodes errorCode, int code, String errorBody, String message) {
        super(message);
        this.error = errorCode;
        this.httpCode = code;
        this.responseBody = errorBody;
    }

    public ClientApiException(Throwable cause, ClientApiErrorCodes errorCode) {
        super(cause);
        this.error = errorCode;
        this.httpCode = ERROR_CODE;
        this.responseBody = null;
    }

    public ClientApiException(Throwable cause, ClientApiErrorCodes errorCode, String message) {
        super(message, cause);
        this.error = errorCode;
        this.httpCode = ERROR_CODE;
        this.responseBody = null;
    }

    public String getErrorCode(){
        return error != null ? error.getCode() : null;
    }

    public String getErrorMessage(){
        return error != null ? error.getMessage() : null;
    }

    /**
     * Auto convert error response body to model
     * @param clazz The class of model
     * @return Result model of error response
     * @param <T> Generic model type
     */
    public <T> T getResponse(Class<T> clazz) {
        if (responseBody == null || responseBody.isEmpty()) {
            return null;
        }

        return JsonUtils.model(responseBody, clazz);
    }

    /**
     * Auto convert error response body to a list of model
     * @param clazz The class of model
     * @return Result model of error response
     * @param <T> Generic model type
     */
    public <T> List<T> getResponseList(Class<T> clazz) {
        if (responseBody == null || responseBody.isEmpty()) {
            return null;
        }

        return JsonUtils.models(responseBody, clazz);
    }

    /**
     * Auto convert error response body to a map
     * @return Result map of error response
     */
    public Map<String, Object> getResponseMap() {
        if (responseBody == null || responseBody.isEmpty()) {
            return null;
        }

        return JsonUtils.map(responseBody);
    }

    public boolean isConnectionTimeout() {
        return ClientApiErrorCodes.CODE_API_TIMEOUT_CONNECTION == error;
    }

    public boolean isReadTimeout() {
        return ClientApiErrorCodes.CODE_API_TIMEOUT_READ == error;
    }

    public boolean isWriteTimeout() {
        return ClientApiErrorCodes.CODE_API_TIMEOUT_WRITE == error;
    }

    public boolean isUnknownHost() {
        return ClientApiErrorCodes.CODE_API_UNKNOWN_HOST == error;
    }

    public boolean isApiTimeout() {
        return isConnectionTimeout() || isReadTimeout() || isWriteTimeout();
    }

    public boolean isApiError() {
        return isApiTimeout() || isUnknownHost();
    }

    public enum ClientApiErrorCodes {

        CODE_API_ERROR("500", "Internal Server Error"),
        CODE_API_UNKNOWN_HOST("596", "Unknown host"),
        CODE_API_TIMEOUT_WRITE("597", "Write timeout"),
        CODE_API_TIMEOUT_READ("598", "Read timeout"),
        CODE_API_TIMEOUT_CONNECTION("599", "Connection timeout");

        final String code;
        final String message;

        ClientApiErrorCodes(String code, String message){
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
