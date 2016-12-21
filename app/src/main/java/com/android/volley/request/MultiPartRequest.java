package com.android.volley.request;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.multipart.MultiPartParam;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A request for making a Multi Part request
 *
 * @param <T> Response expected
 */
public abstract class MultiPartRequest<T> extends Request<T> {

    private static final String PROTOCOL_CHARSET = "utf-8";
    private Listener<T> mListener;
    private Map<String, MultiPartParam> mMultipartParams = null;
    private Map<String, File> mFileUploads = null;
    public static final int TIMEOUT_MS = 30000;

    public MultiPartRequest(int method, String url, Listener<T> listener, ErrorListener errorlistener) {

        super(method, url, Priority.NORMAL, errorlistener, new DefaultRetryPolicy(TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mListener = listener;
        mMultipartParams = new HashMap<String, MultiPartParam>();
        mFileUploads = new HashMap<String, File>();

    }

    /**
     * Add a parameter to be sent in the multipart request
     *
     * @param name        The name of the paramter
     * @param contentType The content type of the paramter
     * @param value       the value of the paramter
     * @return The Multipart request for chaining calls
     */
    public MultiPartRequest<T> addMultipartParam(String name, String contentType, String value) {
        mMultipartParams.put(name, new MultiPartParam(contentType, value));
        return this;
    }

    /**
     * Add a file to be uploaded in the multipart request
     *
     * @param name The name of the file key
     * @param file The path to the file. This file MUST exist.
     * @return The Multipart request for chaining method calls
     */
    public MultiPartRequest<T> addFile(String name, File file) {

        mFileUploads.put(name, file);
        return this;
    }

    @Override
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    @Override
    protected void deliverResponse(Response<T> response) {
        if (null != mListener) {
            mListener.onResponse(response.getResult());
        }
    }

    /**
     * Get all the multipart params for this request
     *
     * @return A map of all the multipart params NOT including the file uploads
     */
    public Map<String, MultiPartParam> getMultipartParams() {
        return mMultipartParams;
    }

    /**
     * Get all the files to be uploaded for this request
     *
     * @return A map of all the files to be uploaded for this request
     */
    public Map<String, File> getFilesToUpload() {
        return mFileUploads;
    }

    /**
     * Get the protocol charset
     */
    public String getProtocolCharset() {
        return PROTOCOL_CHARSET;
    }
}
