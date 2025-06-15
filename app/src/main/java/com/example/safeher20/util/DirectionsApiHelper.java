package com.example.safeher20.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class DirectionsApiHelper {

    public static String getDirections(String origin, String destination, String apiKey) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                + origin + "&destination=" + destination + "&key=" + apiKey;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        return response.body().string();
    }
}