package com.example.marni.orderapp.dataaccess;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.marni.orderapp.service.Config;
import com.example.marni.orderapp.service.VolleyRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivityRequests {
    private Context context;
    public final String tag = this.getClass().getSimpleName();

    private LoginActivityListener listener;

    /**
     * Constructor
     *
     * @param context a description
     * @param listener a description
     */
    public LoginActivityRequests(Context context, LoginActivityListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void handleLogin(String email, String password) {
        final String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        Log.i(tag, "handleLogin - body = " + body);

        try {
            final JSONObject jsonBody = new JSONObject(body);
            StringRequest jsObjRequest = new StringRequest
                    (Request.Method.POST, Config.URL_LOGIN,
                            new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    listener.onLoginSuccessful(response);
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            listener.onError(error.toString());
                        }
                    }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return jsonBody.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    1500, // SOCKET_TIMEOUT_MS,
                    2, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // Access the RequestQueue through your singleton class.
            VolleyRequestQueue.getInstance(context).addToRequestQueue(jsObjRequest);
        } catch (JSONException e) {
            Log.e("", "JSONException " + e.getLocalizedMessage());
        }
    }


    public interface LoginActivityListener {
        void onLoginSuccessful(String response);
        void onError(String message);
    }
}

