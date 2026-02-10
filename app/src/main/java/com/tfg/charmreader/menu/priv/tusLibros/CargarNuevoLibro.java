package com.tfg.charmreader.menu.priv.tusLibros;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.interfacesAPI.I_ImgBB;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;
import com.tfg.charmreader.objetosBD.CCLibrosDeUsuario;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.UUID;

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

    private I_ApiLibro apiLibro = API.getInstancia().create(I_ApiLibro.class);
    private I_ApiLibrosDeUsuario apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    private String IMG_BB_KEY = "474a16c3fe5579608f57dfa163e81875";

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
                // El usuario seleccionó un libro
                Uri epubUri = data.getData();
                getContentResolver().takePersistableUriPermission(epubUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                procesarEpub(epubUri);
            } else {
                // El usuario canceló o dio hacia atrás en el selector
                // Cerramos esta Activity para que no se quede la pantalla en blanco
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
            book.getMetadata().getAuthors().forEach(author -> {
                if (authors.length() > 0) authors.append(", ");
                authors.append(author.getFirstname()).append(" ").append(author.getLastname());
            });

            String isbn = book.getMetadata().getIdentifiers().isEmpty() ? "" : book.getMetadata().getIdentifiers().get(0).getValue();
            int numChapters = book.getTableOfContents().getTocReferences().size();

            byte[] coverData = (book.getCoverImage() != null) ? book.getCoverImage().getData() : null;

            subirPortadaAImgBB(coverData, (urlPortada) -> {
                Libro nuevoLibro = new Libro(isbn, title, authors.toString(), numChapters);
                nuevoLibro.setUrl(urlPortada);

                // LOG DE CONTROL: Mira esto en el Logcat
                Log.d("API_PREPARACION", "Enviando libro a mi API con URL: " + urlPortada);

                guardarEnApi(nuevoLibro, epubUri);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer EPUB", Toast.LENGTH_SHORT).show();
        }
    }

    // Interfaz funcional para el callback de la URL
    interface FirebaseCallback {
        void onUrlReady(String url);
    }

    private void subirPortadaAImgBB(byte[] data, FirebaseCallback callback) {
        if (data == null) {
            callback.onUrlReady("");
            return;
        }

        // 1. Convertir bytes a MultipartBody.Part
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), data);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "portada.jpg", requestFile);

        // 2. Configurar Retrofit para ImgBB (puedes crear una instancia rápida)
        I_ImgBB apiImgBB = new Retrofit.Builder()
                .baseUrl("https://api.imgbb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(I_ImgBB.class);

        // 3. Hacer la llamada
        apiImgBB.uploadImage(IMG_BB_KEY, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        // ImgBB devuelve un JSON anidado. Extraemos la URL:
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String urlDirecta = jsonObject.getJSONObject("data").getString("url");

                        Log.d("ImgBB", "URL obtenida: " + urlDirecta);
                        callback.onUrlReady(urlDirecta);
                    } else {
                        Log.e("ImgBB", "Error en respuesta: " + response.code());
                        callback.onUrlReady("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onUrlReady("");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ImgBB", "Fallo total: " + t.getMessage());
                callback.onUrlReady("");
            }
        });
    }

    private void guardarEnApi(Libro libro, Uri epubUri) {
        new Thread(() -> {
            try {
                Response<Libro> respGuardar = apiLibro.añadirLibro(libro).execute();
                if (respGuardar.isSuccessful() && respGuardar.body() != null) {
                    guardarProgreso(respGuardar.body().getId(), epubUri);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Libro y portada guardados", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error en API Libro", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void guardarProgreso(int idLibro, Uri epubUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("PROGRESO", "Usuario no logueado, no se puede guardar progreso");
            return;
        }

        String correo = user.getEmail();
        int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();

        if (idUsuario == -1) {
            Log.e("PROGRESO", "No se pudo obtener ID de usuario, progreso no guardado");
            return;
        }

        int capituloInicial = 0;
        String rutaEpub = epubUri.toString();

        LibrosDeUsuario librosDeUsuario = new LibrosDeUsuario(new CCLibrosDeUsuario(idUsuario, idLibro), capituloInicial, rutaEpub);
        Log.e("PROGRESO", "Id del usuario: " + librosDeUsuario.getId().getIdU() + ", Id del libro: " + librosDeUsuario.getId().getIdL());

        new Thread(() -> {
            try {
                Log.d("PROGRESO", "Enviando a API: " + librosDeUsuario);
                Gson gson = new Gson();
                Log.d("DEBUG_JSON", gson.toJson(librosDeUsuario));
                Response<LibrosDeUsuario> respProgreso = apiLibrosDeUsuario.guardarProgreso(librosDeUsuario).execute();
                if (!respProgreso.isSuccessful()) {
                    Log.e("API_ERROR_PROGRESO", "Código: " + respProgreso.code());
                    if (respProgreso.errorBody() != null) {
                        Log.e("API_ERROR_PROGRESO", respProgreso.errorBody().string());
                    }
                } else {
                    Log.d("PROGRESO", "Progreso guardado correctamente");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}