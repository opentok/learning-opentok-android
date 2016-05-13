package com.tokbox.android.demo.learningopentok;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class WebServiceCoordinator {

    private static final String CHAT_SERVER_URL = BuildConfig.CHAT_SERVER_URL;
    private static final String SESSION_INFO_ENDPOINT = CHAT_SERVER_URL + "/session";
    private static final String ARCHIVE_START_ENDPOINT = CHAT_SERVER_URL + "/start/:sessionId";
    private static final String ARCHIVE_STOP_ENDPOINT = CHAT_SERVER_URL + "/stop/:archiveId";
    private static final String ARCHIVE_PLAY_ENDPOINT = CHAT_SERVER_URL + "/view/:archiveId";

    private static final String LOG_TAG = WebServiceCoordinator.class.getSimpleName();

    private static RequestQueue reqQueue;

    private final Context context;
    private Listener delegate;

    public WebServiceCoordinator(Context context, Listener delegate) {
        this.context = context;
        this.delegate = delegate;
        this.reqQueue = Volley.newRequestQueue(context);
    }

    public void fetchSessionConnectionData() {
        this.reqQueue.add(new JsonObjectRequest(Request.Method.GET, SESSION_INFO_ENDPOINT, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String apiKey = response.getString("apiKey");
                    String sessionId = response.getString("sessionId");
                    String token = response.getString("token");

                    Log.i(LOG_TAG, apiKey);
                    Log.i(LOG_TAG, sessionId);
                    Log.i(LOG_TAG, token);

                    delegate.onSessionConnectionDataReady(apiKey, sessionId, token);

                } catch (JSONException e) {
                    delegate.onWebServiceCoordinatorError(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                delegate.onWebServiceCoordinatorError(error);
            }
        }));
    }

    public void startArchive(String sessionId) {
        String requestUrl = ARCHIVE_START_ENDPOINT.replace(":sessionId", sessionId);
        this.reqQueue.add(new JsonObjectRequest(Request.Method.POST, requestUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    Log.i(LOG_TAG, "archive started");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                delegate.onWebServiceCoordinatorError(error);
            }
        }));
    }

    public void stopArchive(String archiveId) {
        String requestUrl = ARCHIVE_STOP_ENDPOINT.replace(":archiveId", archiveId);
        this.reqQueue.add(new JsonObjectRequest(Request.Method.POST, requestUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(LOG_TAG, "archive stopped");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                delegate.onWebServiceCoordinatorError(error);
            }
        }));
    }

    public Uri archivePlaybackUri(String archiveId) {
        return Uri.parse(ARCHIVE_PLAY_ENDPOINT.replace(":archiveId", archiveId));
    }

    public static interface Listener {
        void onSessionConnectionDataReady(String apiKey, String sessionId, String token);
        void onWebServiceCoordinatorError(Exception error);
    }
}

