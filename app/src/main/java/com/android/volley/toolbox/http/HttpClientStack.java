/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox.http;

import android.net.http.AndroidHttpClient;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.error.AuthFailureError;
import com.android.volley.misc.NetUtils;
import com.android.volley.toolbox.multipart.MultiPartParam;
import com.android.volley.toolbox.ssl.JindunSSLSocketFactory;
import com.cloudspace.jindun.UCAPIApp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An HttpStack that performs request over an {@link HttpClient}.
 */
public class HttpClientStack extends AbstractHttpStack {
    protected final HttpClient mClient;

    private final static String HEADER_CONTENT_TYPE = "Content-Type";

    public HttpClientStack() {
        mClient = AndroidHttpClient.newInstance(NetUtils.getUserAgent(UCAPIApp.getInstance()));
        setSSLSocketFactory(mClient, getSSLSocketFactory());
    }

    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                httpRequest.setHeader(key, headers.get(key));
            }
        }
    }

    @SuppressWarnings("unused")
    private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(postParams.size());
        if (postParams != null) {
            for (String key : postParams.keySet()) {
                result.add(new BasicNameValuePair(key, postParams.get(key)));
            }
        }
        return result;
    }

    @Override
    public HttpResponse performRequest(Request<?> request,
                                       Map<String, String> additionalHeaders) throws IOException,
            AuthFailureError {
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        addHeaders(httpRequest, request.getHeaders());
        addHeaders(httpRequest, additionalHeaders);
        onPrepareRequest(httpRequest);
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();
        // TODO: Reevaluate this connection timeout based on more wide-scale
        // data collection and possibly different for wifi vs. 3G.
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        return mClient.execute(httpRequest);
    }

    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     */
    @SuppressWarnings("deprecation")
    /* protected */ HttpUriRequest createHttpRequest(Request<?> request,
                                                     Map<String, String> additionalHeaders) throws AuthFailureError, IOException {

        String url = request.getUrl();
        if (mUrlRewriter != null) {
            url = mUrlRewriter.rewriteUrl(url);
            if (url == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
        }
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST: {
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getBody();
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(url);
//                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                    HttpEntity entity;
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    return new HttpGet(url);
                }
            }
            case Method.GET:
                return new HttpGet(url);
            case Method.DELETE:
                return new HttpDelete(url);
            case Method.POST: {
                HttpPost postRequest = new HttpPost(url);
//                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            case Method.PUT: {
                HttpPut putRequest = new HttpPut(url);
//                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            case Method.HEAD:
                return new HttpHead(url);
            case Method.OPTIONS:
                return new HttpOptions(url);
            case Method.TRACE:
                return new HttpTrace(url);
            case Method.PATCH: {
                HttpPatch patchRequest = new HttpPatch(url);
//                patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(patchRequest, request);
                return patchRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(
            HttpEntityEnclosingRequestBase httpRequest, Request<?> request)
            throws IOException, AuthFailureError {

        if (request.containsFile()) {
              /* MultipartEntity multipartEntity = new MultipartEntity();
            final Map<String, MultiPartParam> multipartParams = request.getMultiPartParams();
			for (String key : multipartParams.keySet()) {
				multipartEntity.addPart(new StringPart(key, multipartParams.get(key).value));
			}
			
			final Map<String, File> filesToUpload = request.getFilesToUpload();
			if(filesToUpload!=null){
			for (String key : filesToUpload.keySet()) {
				File file = filesToUpload.get(key) ;

				if (file==null || !file.exists()) {
					throw new IOException(String.format("File not found: %s",file.getAbsolutePath()));
				}

				if (file.isDirectory()) {
					throw new IOException(String.format("File is a directory: %s", file.getAbsolutePath()));
				}

				multipartEntity.addPart(new FilePart(key, file, null, null));
			}
			}
			httpRequest.setEntity(multipartEntity);
         */
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            final Map<String, MultiPartParam> multipartParams = request.getMultiPartParams();
            for (String key : multipartParams.keySet()) {
                multipartEntity.addPart(key, new StringBody(multipartParams.get(key).value, "text/plain", Charset.forName("UTF-8")));
            }

            final Map<String, File> filesToUpload = request.getFilesToUpload();
            if (filesToUpload != null) {
                for (String key : filesToUpload.keySet()) {
                    File file = filesToUpload.get(key);

                    if (file == null || !file.exists()) {
                        throw new IOException(String.format("File not found: %s", file.getAbsolutePath()));
                    }

                    if (file.isDirectory()) {
                        throw new IOException(String.format("File is a directory: %s", file.getAbsolutePath()));
                    }
                    multipartEntity.addPart(key, new FileBody(file));
                }
            }
            httpRequest.setEntity(multipartEntity);

        } else {
            byte[] body = request.getBody();
            if (body != null) {
                HttpEntity entity = new ByteArrayEntity(body);
                httpRequest.setEntity(entity);
            }
        }
    }

    /**
     * Called before the request is executed using the underlying HttpClient.
     * <p/>
     * <p>
     * Overwrite in subclasses to augment the request.
     * </p>
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }

    /**
     * The HttpPatch class does not exist in the Android framework, so this has
     * been defined here.
     */
    public static final class HttpPatch extends HttpEntityEnclosingRequestBase {

        public final static String METHOD_NAME = "PATCH";

        public HttpPatch() {
            super();
        }

        public HttpPatch(final URI uri) {
            super();
            setURI(uri);
        }

        /**
         * @throws IllegalArgumentException if the uri is invalid.
         */
        public HttpPatch(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

    }


    public static org.apache.http.conn.ssl.SSLSocketFactory getSSLSocketFactory() {
        SSLSocketFactory sf = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new JindunSSLSocketFactory(trustStore);
//          sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sf;
    }

    public void setSSLSocketFactory(HttpClient mClient, SSLSocketFactory mSslSocketFactory) {
        if (mSslSocketFactory != null && mClient != null) {
            ClientConnectionManager manager = mClient.getConnectionManager();
            SchemeRegistry schemeRegistry = manager.getSchemeRegistry();
            schemeRegistry.unregister("https");
            Scheme scheme = new Scheme("https", mSslSocketFactory, 443);
            schemeRegistry.register(scheme);
        }
    }

}
