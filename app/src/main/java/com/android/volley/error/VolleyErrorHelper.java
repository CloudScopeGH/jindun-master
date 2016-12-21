package com.android.volley.error;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.cloudspace.jindun.R;
import com.cloudspace.jindun.network.NetworkMonitor;
import com.cloudspace.jindun.network.NetworkState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class VolleyErrorHelper {
    /**
     * Returns appropriate message which is to be displayed to the user against
     * the specified error object.
     *
     * @param error
     * @param context
     * @return
     */
    public static String getAllMessage(Context context, VolleyError error) {
        if (error instanceof ActionError) {
            return error.getMessage();
        } else if (error instanceof TimeoutError) {
            return context.getResources().getString(R.string.error_timeout);
        } else if (isServerProblem(error)) {
            return handleServerError(context, error);
        } else if (isNetworkProblem(error)) {
            if (error.networkResponse != null && error.networkResponse.statusCode == 502 && NetworkState.isWapMode(NetworkMonitor.getCurrState())) {
                return context.getResources().getString(R.string.no_internet_wap);
            }
            return context.getResources().getString(R.string.no_internet);
        }
        return context.getResources().getString(R.string.generic_error);
    }

    public static String getErrorActionMessage(VolleyError error) {
        if (error instanceof ActionError) {
            return error.getMessage();
        }
        return "";
    }

//	public static String getOtherErrorMessage(VolleyError error) {
//		if (error instanceof ActionError) {
//			return error.getMessage();
//		}
//		return "";
//	}

    /**
     * Determines whether the error is related to network
     *
     * @param error
     * @return
     */
    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }

    /**
     * Determines whether the error is related to server
     *
     * @param error
     * @return
     */
    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }

    /**
     * Handles the server error, tries to determine whether to show a stock
     * message or to show a message retrieved from the server.
     *
     * @param err
     * @param context
     * @return
     */
    private static String handleServerError(Context context, Object err) {
        VolleyError error = (VolleyError) err;

        NetworkResponse response = error.networkResponse;

        if (response != null) {
            switch (response.statusCode) {
                case 404:
                case 422:
                case 401:
                    try {
                        // server might return error like this { "error":
                        // "Some error occured" }
                        // Use "Gson" to parse the result
                        HashMap<String, String> result = new Gson().fromJson(new String(response.data),
                                new TypeToken<Map<String, String>>() {
                                }.getType());

                        if (result != null && result.containsKey("error")) {
                            return result.get("error");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // invalid request
                    return error.getMessage();

                default:
                    return context.getResources().getString(R.string.error_timeout);
            }
        }
        return context.getResources().getString(R.string.generic_error);
    }
}