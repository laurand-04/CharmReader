package com.tfg.charmreader.menu.priv.tusLibros;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ImgBB;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;
import com.tfg.charmreader.objetosBD.CCLibrosDeUsuario;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CargarNuevoLibro extends AppCompatActivity {

    private final I_ApiLibro apiLibro = API.getInstancia().create(I_ApiLibro.class);
    private final I_ApiLibrosDeUsuario apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    private final String IMG_BB_KEY = "474a16c3fe5579608f57dfa163e81875";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_nuevo_libro);
        openEpubPicker();
    }

    private void openEpubPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/epub+zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 42);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 42) {
            if (resultCode == RESULT_OK && data != null) {
                Uri epubUri = data.getData();
                getContentResolver().takePersistableUriPermission(epubUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                procesarEpub(epubUri);
            } else {
                finish();
            }
        }
    }

    private void procesarEpub(Uri epubUri) {
        try {
            InputStream is = getContentResolver().openInputStream(epubUri);
            if (is == null) return;

            Book book = new EpubReader().readEpub(is);

            // 1. Extraer Metadatos
            String title = book.getTitle();
            StringBuilder authors = new StringBuilder();
            List<Author> authorList = book.getMetadata().getAuthors();
            for (int i = 0; i < authorList.size(); i++) {
                Author a = authorList.get(i);
                authors.append(a.getFirstname()).append(" ").append(a.getLastname());
                if (i < authorList.size() - 1) authors.append(", ");
            }

            String isbn = book.getMetadata().getIdentifiers().isEmpty() ? "" : book.getMetadata().getIdentifiers().get(0).getValue();
            int numChapters = book.getTableOfContents().getTocReferences().size();
            byte[] coverData = (book.getCoverImage() != null) ? book.getCoverImage().getData() : null;

            subirPortadaAImgBB(coverData, (urlPortada) -> {
                Libro nuevoLibro = new Libro(isbn, title, authors.toString(), numChapters);
                nuevoLibro.setUrl(urlPortada);
                guardarEnApi(nuevoLibro, epubUri);
            });

        } catch (Exception e) {
            Log.e("EPUB_ERROR", "Error al leer EPUB", e);
            Toast.makeText(this, "Error al leer EPUB", Toast.LENGTH_SHORT).show();
        }
    }

    interface ImgBBCallback {
        void onUrlReady(String url);
    }

    private void subirPortadaAImgBB(byte[] data, ImgBBCallback callback) {
        if (data == null) {
            callback.onUrlReady("");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), data);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "portada.jpg", requestFile);

        I_ImgBB apiImgBB = new Retrofit.Builder()
                .baseUrl("https://api.imgbb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(I_ImgBB.class);

        apiImgBB.uploadImage(IMG_BB_KEY, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String urlDirecta = jsonObject.getJSONObject("data").getString("url");
                        callback.onUrlReady(urlDirecta);
                    } else {
                        callback.onUrlReady("");
                    }
                } catch (Exception e) {
                    callback.onUrlReady("");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onUrlReady("");
            }
        });
    }

    private void guardarEnApi(Libro libro, Uri epubUri) {
        new Thread(() -> {
            try {
                Response<Libro> respGuardar = apiLibro.añadirLibro(libro).execute();
                if (respGuardar.isSuccessful() && respGuardar.body() != null) {
                    // Llamamos a guardar progreso con el ID del libro recién creado
                    guardarProgreso(respGuardar.body().getId(), epubUri);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al guardar metadatos del libro", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("API_LIBRO", "Error", e);
            }
        }).start();
    }

    private void guardarProgreso(int idLibro, Uri epubUri) {
        // 🔥 CAMBIO CLAVE: Obtenemos el ID del usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Log.e("PROGRESO", "ID de usuario no encontrado localmente");
            runOnUiThread(() -> Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show());
            return;
        }

        int capituloInicial = 0;
        String rutaEpub = epubUri.toString();
        LibrosDeUsuario librosDeUsuario = new LibrosDeUsuario(new CCLibrosDeUsuario(idUsuario, idLibro), capituloInicial, rutaEpub);

        try {
            Response<LibrosDeUsuario> respProgreso = apiLibrosDeUsuario.guardarProgreso(librosDeUsuario).execute();

            runOnUiThread(() -> {
                if (respProgreso.isSuccessful()) {
                    Toast.makeText(this, "¡Libro añadido con éxito!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error al vincular libro con usuario", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("PROGRESO_ERROR", "Error", e);
        }
    }
}