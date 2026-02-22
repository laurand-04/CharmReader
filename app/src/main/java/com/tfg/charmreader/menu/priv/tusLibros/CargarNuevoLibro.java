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

import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ImgBB;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;
import com.tfg.charmreader.objetosBD.CCLibrosDeUsuario;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
        if (requestCode == 42 && resultCode == RESULT_OK && data != null) {
            Uri epubUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(epubUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) { Log.e("PERMISOS", "No se pudo persistir", e); }
            procesarEpub(epubUri);
        } else finish();
    }

    private void procesarEpub(Uri epubUri) {
        try {
            InputStream is = getContentResolver().openInputStream(epubUri);
            if (is == null) return;

            Book book = new EpubReader().readEpub(is);
            String title = book.getTitle();

            // Autores
            StringBuilder authors = new StringBuilder();
            List<Author> authorList = book.getMetadata().getAuthors();
            for (int i = 0; i < authorList.size(); i++) {
                authors.append(authorList.get(i).getFirstname()).append(" ").append(authorList.get(i).getLastname());
                if (i < authorList.size() - 1) authors.append(", ");
            }

            String isbn = book.getMetadata().getIdentifiers().isEmpty() ? "" : book.getMetadata().getIdentifiers().get(0).getValue();
            int numChapters = book.getSpine().size();
            byte[] coverData = (book.getCoverImage() != null) ? book.getCoverImage().getData() : null;

            // Copia local
            String rutaLocal = copiarEpubAlmacenamientoInterno(epubUri);

            // Subida portada y guardar libro
            subirPortadaAImgBB(coverData, urlPortada -> {
                Libro nuevoLibro = new Libro(isbn, title, authors.toString(), numChapters);
                nuevoLibro.setUrl(urlPortada);
                guardarEnApi(nuevoLibro, rutaLocal);
            });

        } catch (Exception e) {
            Log.e("EPUB_ERROR", "Error al leer EPUB", e);
            Toast.makeText(this, "Error al leer EPUB", Toast.LENGTH_SHORT).show();
        }
    }

    private String copiarEpubAlmacenamientoInterno(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            String fileName = "libro_" + System.currentTimeMillis() + ".epub";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
            out.close();
            in.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            Log.e("COPIA_INTERNA", "Error al copiar", e);
            return uri.toString();
        }
    }

    private void guardarEnApi(Libro libro, String rutaLocal) {
        new Thread(() -> {
            try {
                Response<Libro> respGuardar = apiLibro.añadirLibro(libro).execute();
                if (respGuardar.isSuccessful() && respGuardar.body() != null) {
                    guardarProgreso(respGuardar.body().getId(), rutaLocal);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void guardarProgreso(int idLibro, String rutaLocal) {
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);
        LibrosDeUsuario ldu = new LibrosDeUsuario(new CCLibrosDeUsuario(idUsuario, idLibro), 0, rutaLocal);

        try {
            Response<LibrosDeUsuario> resp = apiLibrosDeUsuario.guardarProgreso(ldu).execute();
            runOnUiThread(() -> {
                if (resp.isSuccessful()) {
                    // 🔥 MODIFICACIÓN: Ya no abrimos el visor.
                    // Solo notificamos éxito y cerramos la Activity.
                    // Al cerrar, el fragmento recibirá el RESULT_OK y recargará la lista.
                    Toast.makeText(this, "Libro añadido a tu biblioteca", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error al vincular el libro", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void subirPortadaAImgBB(byte[] data, ImgBBCallback callback) {
        if (data == null) { callback.onUrlReady(""); return; }
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), data);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "portada.jpg", requestFile);

        new Retrofit.Builder()
                .baseUrl("https://api.imgbb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(I_ImgBB.class)
                .uploadImage(IMG_BB_KEY, body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                JSONObject json = new JSONObject(response.body().string());
                                callback.onUrlReady(json.getJSONObject("data").getString("url"));
                            } else callback.onUrlReady("");
                        } catch (Exception e) { callback.onUrlReady(""); }
                    }
                    @Override public void onFailure(Call<ResponseBody> call, Throwable t) { callback.onUrlReady(""); }
                });
    }

    interface ImgBBCallback { void onUrlReady(String url); }
}