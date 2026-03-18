
package com.tfg.charmreader.data.network.API;

import com.tfg.charmreader.data.model.Valoracion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GeminiService {

    public interface GeminiCallback {
        void onSuccess(String respuesta);
        void onError(Exception e);
    }

    public void generarTexto(String prompt, GeminiCallback callback) {
        // Pega tu clave real aquí
        String apiKey = "AIzaSyB_cXWIyGfR9Sli0drroA0Hkf5sYB_zSk4";

        new Thread(() -> {
            try {
                // 1. Configuramos la conexión directa a la API de Gemini
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // 2. Construimos el cuerpo de la petición (JSON)
                JSONObject part = new JSONObject().put("text", prompt);
                JSONArray parts = new JSONArray().put(part);
                JSONObject content = new JSONObject().put("parts", parts);
                JSONArray contents = new JSONArray().put(content);
                JSONObject requestBody = new JSONObject().put("contents", contents);

                // 3. Enviamos la petición
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // 4. Verificamos si la respuesta fue exitosa (código 200)
                int responseCode = conn.getResponseCode();
                InputStream inputStream;
                if (responseCode >= 200 && responseCode <= 299) {
                    inputStream = conn.getInputStream();
                } else {
                    // Si hay error (ej. clave incorrecta), leemos el error
                    inputStream = conn.getErrorStream();
                }

                // 5. Leemos el texto devuelto por el servidor
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder responseString = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseString.append(responseLine.trim());
                }

                // 6. Procesamos el resultado
                if (responseCode >= 200 && responseCode <= 299) {
                    // Navegamos por el JSON de respuesta para sacar solo el texto de Gemini
                    JSONObject jsonResponse = new JSONObject(responseString.toString());
                    String textoRespuesta = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    if (callback != null) {
                        callback.onSuccess(textoRespuesta);
                    }
                } else {
                    // Si el código no es 200, devolvemos el error exacto de la API
                    if (callback != null) {
                        callback.onError(new Exception("Error " + responseCode + ": " + responseString.toString()));
                    }
                }

            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }

    public String generarPrompt(List<Valoracion> valoraciones) {
        if (valoraciones == null || valoraciones.isEmpty()) {
            return "No hay valoraciones disponibles para este libro.";
        }

        StringBuilder sb = new StringBuilder();
        double sumaCalificaciones = 0;
        int contadoresConTexto = 0;

        sb.append("Actúa como un experto crítico literario. ");
        sb.append("A continuación te presento una lista de reseñas de usuarios sobre un libro. ");
        sb.append("Tu tarea es escribir un ÚNICO párrafo (máximo 4 líneas) que resuma el sentimiento general, ");
        sb.append("mencionando los puntos fuertes y débiles más repetidos por los lectores.\n\n");
        sb.append("Reseñas de los usuarios:\n");

        for (Valoracion v : valoraciones) {
            sumaCalificaciones += v.getCalificacion();
            if (v.getDescripcion() != null && !v.getDescripcion().trim().isEmpty()) {
                contadoresConTexto++;
                sb.append("- ").append(v.getDescripcion()).append("\n");
            }
        }

        if (contadoresConTexto == 0) {
            double media = sumaCalificaciones / valoraciones.size();
            sb.append("\nNota media: ").append(String.format("%.1f", media));
            sb.append(". Los usuarios no han dejado comentarios escritos, resume qué significa esta puntuación.");
        }

        sb.append("\nEscribe el resumen ahora de forma cercana y profesional:");
        return sb.toString();
    }
}