package com.android.volley.toolbox;


public interface ResponseParser<T> {
    public Object parse(T t);
}
