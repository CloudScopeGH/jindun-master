package com.android.volley.toolbox.multipart;

/**
 * A representation of a MultiPart parameter
 */
public class MultiPartParam {

    private String CONTENTTYPE = "text/plain;";

    public String contentType;
    public String value;

    /**
     * Initialize a multipart request param with the value and content type
     *
     * @param contentType The content type of the param
     * @param value       The value of the param
     */
    public MultiPartParam(String contentType, String value) {
        this.contentType = contentType;
        this.value = value;
    }

    public MultiPartParam(String value) {
        this.contentType = CONTENTTYPE;
        this.value = value;
    }
}