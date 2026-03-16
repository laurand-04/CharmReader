package com.tfg.charmreader.data.network.API;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlacesClientManager {

    // Usaremos Nominatim para OSM en español y solo España
    private static final String API_URL =
            "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&countrycodes=ES&limit=10&accept-language=es&q=";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void obtenerPredicciones(String query, OnPlacesPredictionsListener listener) {
        executor.execute(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                URL url = new URL(API_URL + encodedQuery);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "CharmReaderTFG");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                List<UbicacionSimple> resultados = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    JSONObject address = obj.getJSONObject("address");

                    String localidad = "";
                    String provincia = "";

                    if (address.has("village")) localidad = address.getString("village");
                    else if (address.has("town")) localidad = address.getString("town");
                    else if (address.has("city")) localidad = address.getString("city");
                    else if (address.has("municipality")) localidad = address.getString("municipality");

                    // Para provincia usamos 'county' si existe, sino 'state'
                    if (address.has("province")) provincia = address.getString("province");
                    else if (address.has("state")) provincia = address.getString("state");

                    // Formateamos nombre como: localidad (provincia)
                    String nombreFormateado = localidad;
                    if (!provincia.isEmpty()) {
                        nombreFormateado += " (" + provincia + ")";
                    }

                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");

                    resultados.add(new UbicacionSimple(nombreFormateado, lat, lon));
                }

                mainHandler.post(() -> listener.onSuccess(resultados));

            } catch (Exception e) {
                Log.e("PLACES_API", "Error obteniendo lugares", e);
                mainHandler.post(() -> listener.onError(e.getMessage()));
            }
        });
    }

    public static class UbicacionSimple {
        public String nombre;
        public double lat;
        public double lon;

        public UbicacionSimple(String nombre, double lat, double lon) {
            this.nombre = nombre;
            this.lat = lat;
            this.lon = lon;
        }
    }

    public interface OnPlacesPredictionsListener {
        void onSuccess(List<UbicacionSimple> sugerencias);
        void onError(String error);
    }
}