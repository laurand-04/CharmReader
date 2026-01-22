package com.tfg.charmreader.menu.tusLibros;

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
import com.google.gson.Gson;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.CCLibrosDeUsuario;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.io.InputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import retrofit2.Response;

public class CargarNuevoLibro extends AppCompatActivity {

    private I_ApiLibro apiLibro = API.getInstancia().create(I_ApiLibro.class);
    private I_ApiLibrosDeUsuario apiLibrosDeUsuario = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
    private I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Abrir selector de EPUB
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

            getContentResolver().takePersistableUriPermission(
                    epubUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            guardarMetadatos(epubUri);
        }
    }

    private void guardarMetadatos(Uri epubUri) {
        try {
            InputStream is = getContentResolver().openInputStream(epubUri);
            if (is == null) {
                Toast.makeText(this, "No se pudo abrir el archivo EPUB", Toast.LENGTH_LONG).show();
                return;
            }

            Book book = new EpubReader().readEpub(is);

            // Extraer información
            String title = book.getTitle();
            StringBuilder authors = new StringBuilder();
            book.getMetadata().getAuthors().forEach(author -> {
                if (authors.length() > 0) authors.append(", ");
                authors.append(author.getFirstname()).append(" ").append(author.getLastname());
            });

            String isbn = "";
            if (!book.getMetadata().getIdentifiers().isEmpty()) {
                isbn = book.getMetadata().getIdentifiers().get(0).getValue();
            }

            int numChapters = book.getTableOfContents().getTocReferences().size();

            Libro libro = new Libro(isbn, title, authors.toString(), numChapters);

            // ⚡ Ejecutar en un hilo separado
            new Thread(() -> {
                try {
                    Response<Libro> respGuardar = apiLibro.añadirLibro(libro).execute();

                    if (!respGuardar.isSuccessful()) {
                        Log.e("API_ERROR", "Código: " + respGuardar.code() + " - " + respGuardar.message());
                        if (respGuardar.errorBody() != null)
                            Log.e("API_ERROR", "Cuerpo: " + respGuardar.errorBody().string());
                    }

                    if (respGuardar.isSuccessful()) {
                        Libro libroGuardado = respGuardar.body(); // Libro con ID asignado por la base de datos
                        if (libroGuardado != null) {
                            guardarProgreso(libroGuardado.getId(), epubUri);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Libro guardado correctamente", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent); // indica que todo fue OK
                                finish();
                            });
                        }
                    }
                    else {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Error al guardar libro", Toast.LENGTH_SHORT).show()
                        );
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer EPUB: " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
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

