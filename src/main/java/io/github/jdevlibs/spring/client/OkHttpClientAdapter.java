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
package io.github.jdevlibs.spring.client;

import io.github.jdevlibs.spring.client.request.*;
import io.github.jdevlibs.spring.exception.ClientApiException;
import io.github.jdevlibs.spring.utils.JsonUtils;
import io.github.jdevlibs.utils.MimeTypes;
import io.github.jdevlibs.utils.Validators;
import okhttp3.*;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author supot.jdev
 * @version 1.0
 */
public abstract class OkHttpClientAdapter implements InitializingBean {
    private static final String TIMEOUT_CONN    = "java.net.SocketTimeoutException: Connect timed out";
    private static final String TIMEOUT_READ    = "java.net.SocketTimeoutException: Read timed out";
    private static final String TIMEOUT_WRITE   = "java.net.SocketTimeoutException: timeout";
    private static final String CONTENT_TYPE_JSON   = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE_FORM   = "application/x-www-form-urlencoded";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT       = "Accept";

    private static final MediaType TYPE_JSON = MediaType.parse(CONTENT_TYPE_JSON);
    
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private OkHttpClient httpClient;

    /* ++++++++++++++++++++++++++ Initial and Validate +++++++++++++++++++++++ */
    @Override
    public final void afterPropertiesSet() throws IllegalArgumentException {
        validate();
    }

    protected abstract void autowiredHttpClient(OkHttpClient httpClient);

    public final void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public OkHttpClient newHttpClient() {
        return httpClient.newBuilder().build();
    }

    public OkHttpClient newHttpClient(Duration connectTimeout) {
        return newHttpClient(connectTimeout, null, null);
    }

    public OkHttpClient newHttpClient(Duration connectTimeout, Duration readTimeout) {
        return newHttpClient(connectTimeout, readTimeout, null);
    }

    public OkHttpClient newHttpClient(Duration connectTimeout, Duration readTimeout, Duration writeTimeout) {
        OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
        if (connectTimeout != null) {
            clientBuilder.connectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            clientBuilder.readTimeout(readTimeout);
        }
        if (writeTimeout != null) {
            clientBuilder.writeTimeout(writeTimeout);
        }
        return clientBuilder.build();
    }

    private void validate() {
        if (this.httpClient == null) {
            throw new IllegalArgumentException("OkHttpClient bean is required");
        }
    }

    /**
     *  Call service API with POST by json body
     * @param url Service API URL
     */
    public void post(String url) {
        post(url, new JsonRequest<>(), null);
    }

    /**
     *  Call service API with POST by json body
     * @param url Service API URL
     * @param jsonRequest The request json model
     */
    public void post(String url, JsonRequest<?> jsonRequest) {
        post(url, jsonRequest, null);
    }

    /**
     * Call service API with POST by json body
     * @param url Service API URL
     * @param request The request json model
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> Generic class
     */
    public <T> T post(String url, JsonRequest<?> request, Class<T> clazz) {
        try {
            byte[] contents = jsonAsByte(url, request, HttpMethod.POST);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.model(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     *  Call service API with PUT by json body
     * @param url Service API URL
     */
    public void put(String url) {
        put(url, new JsonRequest<>(), null);
    }

    /**
     *  Call service API with PUT by json body
     * @param url Service API URL
     * @param jsonRequest The request json model
     */
    public void put(String url, JsonRequest<?> jsonRequest) {
        put(url, jsonRequest, null);
    }

    /**
     * Call service API with POST by json body
     * @param url Service API URL
     * @param jsonRequest The request json model
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> Generic class
     */
    public <T> T put(String url, JsonRequest<?> jsonRequest, Class<T> clazz) {
        try {
            byte[] contents = jsonAsByte(url, jsonRequest, HttpMethod.PUT);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.model(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with POST by json body return result as collections.
     * @param url Service API URL
     * @param jsonRequest The request json model
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> The type of response class
     */
    public <T> List<T> postResultAsList(String url, JsonRequest<?> jsonRequest, Class<T> clazz) {
        try {
            byte[] contents = jsonAsByte(url, jsonRequest, HttpMethod.POST);
            if (Validators.isEmpty(contents) || clazz == null) {
                return Collections.emptyList();
            }
            return JsonUtils.models(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with PUT by json body return result as collections.
     * @param url Service API URL
     * @param jsonRequest The request json model
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> The type of response class
     */
    public <T> List<T> putResultAsList(String url, JsonRequest<?> jsonRequest, Class<T> clazz) {
        try {
            byte[] contents = jsonAsByte(url, jsonRequest, HttpMethod.PUT);
            if (Validators.isEmpty(contents) || clazz == null) {
                return Collections.emptyList();
            }
            return JsonUtils.models(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with POST by form
     * @param url Service API URL
     */
    public void postForm(String url) {
        postForm(url, null, null);
    }

    /**
     * Call service API with POST by form
     * @param url Service API URL
     * @param req The Form request object includes [fields, headers]
     */
    public void postForm(String url, FormRequest req) {
        postForm(url, req, null);
    }

    /**
     * Call service API with POST by form
     * @param url   Service API URL
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> T postForm(String url, Class<T> clazz) {
        return postForm(url, new FormRequest(), clazz);
    }

    /**
     * Call service API with POST by form
     * @param url   Service API URL
     * @param req   The Form request object includes [fields, headers]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> T postForm(String url, FormRequest req, Class<T> clazz) {
        try {
            byte[] contents = postFormAsByte(url, req);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.model(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with POST by a form return result as collections.
     * @param url   Service API URL
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> List<T> postFormResultAsList(String url, Class<T> clazz) {
        return postFormResultAsList(url, new FormRequest(), clazz);
    }

    /**
     * Call service API with POST by a form return result as collections.
     * @param url   Service API URL
     * @param req   The Form request object includes [fields, headers]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> List<T> postFormResultAsList(String url, FormRequest req, Class<T> clazz) {
        try {
            byte[] contents = postFormAsByte(url, req);
            if (Validators.isEmpty(contents) || clazz == null) {
                return Collections.emptyList();
            }
            return JsonUtils.models(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with POST by MultiPart (file upload)
     * @param url Service API URL
     * @param req The Form request object includes [fields, headers, files]
     */
    public void postMultiPart(String url, MultipartRequest req) {
        try {
            logInfo(url, req);
            if (Validators.isNullOne(url, req)) {
                throw new ClientApiException(ClientApiException.ClientApiErrorCodes.CODE_API_ERROR, "Invalid required parameter");
            }

            RequestBody body = multipartRequestBody(req);
            Request request = formMultipart(url, body, req);
            Call call = httpClient.newCall(request);
            try (Response resp = call.execute()) {
                if (!resp.isSuccessful()) {
                    throw throwException(resp);
                }
            }
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with POST by MultiPart (file upload)
     * @param url   Service API URL
     * @param multipartRequest   The Form request object includes [fields, headers, files]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> T postMultiPart(String url, MultipartRequest multipartRequest, Class<T> clazz) {
        try {
            logInfo(url, multipartRequest);
            if (Validators.isNullOne(url, multipartRequest, clazz)) {
                throw new ClientApiException(ClientApiException.ClientApiErrorCodes.CODE_API_ERROR, "Invalid required parameter");
            }

            RequestBody body = multipartRequestBody(multipartRequest);
            Request request = formMultipart(url, body, multipartRequest);
            Call call = httpClient.newCall(request);
            try (Response resp = call.execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    return JsonUtils.model(resp.body().bytes(), clazz);
                } else {
                    throw throwException(resp);
                }
            }
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with http (GET) method
     * @param url   Service API URL
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> T get(String url, Class<T> clazz) {
        return get(url, new GetRequest(), clazz);
    }

    /**
     * Call service API with http (GET) method
     * @param url   Service API URL
     * @param getRequest   The Form request object includes [Parameters, headers]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> T get(String url, GetRequest getRequest, Class<T> clazz) {
        try {
            byte[] contents = getAsByte(url, getRequest);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.model(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with http (GET) method
     * @param url   Service API URL
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> List<T> getResultAsList(String url, Class<T> clazz) {
        return getResultAsList(url, new GetRequest(), clazz);
    }

    /**
     * Call service API with http (GET) with method
     * @param url   Service API URL
     * @param getRequest   The Form request object includes [Parameters, headers]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> The type of response class
     */
    public <T> List<T> getResultAsList(String url, GetRequest getRequest, Class<T> clazz) {
        try {
            byte[] contents = getAsByte(url, getRequest);
            if (Validators.isEmpty(contents) || clazz == null) {
                return Collections.emptyList();
            }

            return JsonUtils.models(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     *  Call service API with [http:DELETE]
     * @param url Service API URL
     */
    public void delete(String url) {
        delete(url, new DeleteRequest(), null);
    }

    /**
     * Call service API with [http:DELETE]
     * @param url Service API URL
     * @param deleteRequest The request object includes [parameter, headers]
     */
    public void delete(String url, DeleteRequest deleteRequest) {
        delete(url, deleteRequest, null);
    }

    /**
     * Call service API with [http:DELETE]
     * @param url Service API URL
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> The type of response class
     */
    public <T> T delete(String url, Class<T> clazz) {
        return delete(url, new DeleteRequest(), clazz);
    }

    /**
     *  Call service API with [http:DELETE]
     * @param url Service API URL
     * @param deleteRequest The request object includes [parameter, headers]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> The type of response class
     */
    public <T> T delete(String url, DeleteRequest deleteRequest, Class<T> clazz) {
        try {
            byte[] contents = deleteAsByte(url, deleteRequest);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.model(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     *  Call service API with [http:DELETE]
     * @param url Service API URL
     * @param deleteRequest The request object includes [parameter, headers]
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T> The type of response class
     */
    public <T> List<T> deleteResultAsList(String url, DeleteRequest deleteRequest, Class<T> clazz) {
        try {
            byte[] contents = deleteAsByte(url, deleteRequest);
            if (Validators.isEmpty(contents) || clazz == null) {
                return Collections.emptyList();
            }

            return JsonUtils.models(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with [http:DELETE] by json body
     * @param url Service API URL
     * @param jsonRequest The request model
     */
    public void delete(String url, JsonRequest<?> jsonRequest) {
        delete(url, jsonRequest, null);
    }

    /**
     * Call service API with [http:DELETE] by json body
     * @param url   Service API URL
     * @param jsonRequest   The request model
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> T delete(String url, JsonRequest<?> jsonRequest, Class<T> clazz) {
        try {
            byte[] contents = jsonAsByte(url, jsonRequest, HttpMethod.DELETE);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.model(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    /**
     * Call service API with [http:DELETE] by json body result as a collection.
     * @param url   Service API URL
     * @param jsonRequest   The request model
     * @param clazz The response model class
     * @return The result of assign class
     * @param <T>   The type of response class
     */
    public <T> List<T> deleteResultAsList(String url, JsonRequest<?> jsonRequest, Class<T> clazz) {
        try {
            byte[] contents = jsonAsByte(url, jsonRequest, HttpMethod.DELETE);
            if (Validators.isEmpty(contents) || clazz == null) {
                return null;
            }

            return JsonUtils.models(contents, clazz);
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    private byte[] jsonAsByte(String url, JsonRequest<?> req, HttpMethod httpMethod) {
        try {
            String json = null;
            if (req != null && req.getModel() != null) {
                json = JsonUtils.json(req.getModel());
            }
            logInfo(url, json);

            Request.Builder builder = jsonRequest(url, req);
            Request request;
            if (HttpMethod.PUT == httpMethod) {
                request = builder.put(jsonRequestBody(json)).build();
            } else if (HttpMethod.PATCH == httpMethod) {
                request = builder.patch(jsonRequestBody(json)).build();
            } else if (HttpMethod.DELETE == httpMethod) {
                request = builder.delete(jsonRequestBody(json)).build();
            } else {
                request = builder.post(jsonRequestBody(json)).build();
            }

            Call call = httpClient.newCall(request);
            try (Response resp = call.execute()) {
                if (resp.isSuccessful()) {
                    return resp.body() != null ? resp.body().bytes() : new byte[]{};
                } else {
                    throw throwException(resp);
                }
            }
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    private byte[] postFormAsByte(String url, FormRequest req) {
        try {
            FormBody.Builder builder = new FormBody.Builder();
            if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getFields())) {
                req.getFields().forEach(builder::add);
            }

            logInfo(url, req);

            RequestBody body = builder.build();
            Request request = formRequest(url, body, req);
            Call call = httpClient.newCall(request);
            try (Response resp = call.execute()) {
                if (resp.isSuccessful()) {
                    return resp.body() != null ? resp.body().bytes() : new byte[]{};
                } else {
                    throw throwException(resp);
                }
            }
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    private byte[] getAsByte(String url, GetRequest req) {
        try {
            HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            //Query Parameters
            if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getParameters())) {
                req.getParameters().forEach(builder::addEncodedQueryParameter);
                url = builder.build().toString();
            }

            logInfo(url, req);

            Request request = getRequest(url, req);
            Call call = httpClient.newCall(request);
            try (Response resp = call.execute()) {
                if (resp.isSuccessful()) {
                    return resp.body() != null ? resp.body().bytes() : new byte[]{};
                } else {
                    throw throwException(resp);
                }
            }
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    private byte[] deleteAsByte(String url, DeleteRequest req) {
        try {
            HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            //Query Parameters
            if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getParameters())) {
                req.getParameters().forEach(builder::addEncodedQueryParameter);
                url = builder.build().toString();
            }

            logInfo(url, req);

            Request request = deleteRequest(url, req);
            Call call = httpClient.newCall(request);
            try (Response resp = call.execute()) {
                if (resp.isSuccessful()) {
                    return resp.body() != null ? resp.body().bytes() : new byte[]{};
                } else {
                    throw throwException(resp);
                }
            }
        } catch (ClientApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw throwException(ex);
        }
    }

    private RequestBody multipartRequestBody(MultipartRequest req) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //Form Field
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getFields())) {
            req.getFields().forEach(builder::addFormDataPart);
        }

        // Form file
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getFiles())) {
            req.getFiles().forEach((key, value) -> {
                RequestBody reqFile = createFile(value);
                if(reqFile != null) {
                    builder.addFormDataPart(key, value.getName(), reqFile);
                }
            });
        }
        return builder.build();
    }

    private Request formRequest(String url, RequestBody body, FormRequest req) {
        Request.Builder builder = new Request.Builder().url(url)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM)
                .header(HEADER_ACCEPT, CONTENT_TYPE_FORM);
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getHeaders())) {
            req.getHeaders().forEach(builder::addHeader);
        }
        return builder.post(body).build();
    }

    private Request formMultipart(String url, RequestBody body, MultipartRequest req) {
        Request.Builder builder = new Request.Builder().url(url);
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getHeaders())) {
            req.getHeaders().forEach(builder::addHeader);
        }
        return builder.post(body).build();
    }

    private Request.Builder jsonRequest(String url, JsonRequest<?> req) {
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .addHeader(HEADER_ACCEPT, CONTENT_TYPE_JSON);
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getHeaders())) {
            req.getHeaders().forEach(builder::addHeader);
        }
        return builder;
    }

    private RequestBody jsonRequestBody(String json) {
        return RequestBody.create(json, TYPE_JSON);
    }

    private RequestBody createFile(MultipartRequest.FilePart file) {
        if (Validators.isNull(file)) {
            return null;
        }

        String mimeType = MimeTypes.getMimeType(file.getName());
        if (file.isFileByPath()) {
            return RequestBody.create(new File(file.getPath()), MediaType.parse(mimeType));
        }

        if (file.isFileBinary()) {
            return RequestBody.create(file.getContents(), MediaType.parse(mimeType));
        }
        return null;
    }

    private Request getRequest(String url, GetRequest req) {
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getHeaders())) {
            Request.Builder builder = new Request.Builder().url(url);
            req.getHeaders().forEach(builder::addHeader);
            return builder.build();
        }
        return new Request.Builder().url(url).build();
    }

    private Request deleteRequest(String url, DeleteRequest req) {
        if (Validators.isNotNull(req) && Validators.isNotEmpty(req.getHeaders())) {
            Request.Builder builder = new Request.Builder().url(url);
            req.getHeaders().forEach(builder::addHeader);
            return builder.delete().build();
        }
        return new Request.Builder().url(url).delete().build();
    }

    private ClientApiException throwException(Response resp) {

        StringBuilder error = new StringBuilder();
        error.append("[isSuccessful:").append(resp.isSuccessful());
        error.append(", code:").append(resp.code());
        error.append(", message:").append(resp.message());
        error.append(", headers:").append(resp.headers());
        String body = null;
        if (resp.body() != null) {
            try {
                body = resp.body().string();
                body = body.replaceAll("\\r\\n", "")
                        .replaceAll("\\t", "");
            } catch (Exception ex) {
                logger.error("convert http body :", ex);
            }
        }
        error.append("]");
        return new ClientApiException(ClientApiException.ClientApiErrorCodes.CODE_API_ERROR, resp.code(), body, "Call service api error :" + error);
    }

    private void logInfo(String url, Object request) {
        logger.debug("Call API with URL: {}", url);
        if (request != null) {
            logger.debug("Call API Request : {}", request);
        }
    }

    private ClientApiException throwException(Exception ex) {
        if (ex instanceof ClientApiException) {
            throw (ClientApiException) ex;
        }

        if (ex instanceof SocketTimeoutException) {
            String err = ex.toString();
            if (err.contains(TIMEOUT_CONN)) {
                throw new ClientApiException(ex, ClientApiException.ClientApiErrorCodes.CODE_API_TIMEOUT_CONNECTION);
            } else if (err.contains(TIMEOUT_READ)) {
                throw new ClientApiException(ex, ClientApiException.ClientApiErrorCodes.CODE_API_TIMEOUT_READ);
            } else if (err.contains(TIMEOUT_WRITE)) {
                throw new ClientApiException(ex, ClientApiException.ClientApiErrorCodes.CODE_API_TIMEOUT_WRITE);
            } else {
                throw new ClientApiException(ex, ClientApiException.ClientApiErrorCodes.CODE_API_ERROR);
            }
        } else if (ex instanceof UnknownHostException || ex instanceof SSLHandshakeException) {
            throw new ClientApiException(ex, ClientApiException.ClientApiErrorCodes.CODE_API_UNKNOWN_HOST);
        }
        throw new ClientApiException(ex, ClientApiException.ClientApiErrorCodes.CODE_API_ERROR);
    }

    public enum HttpMethod {
        POST, PUT, PATCH, DELETE
    }
}
