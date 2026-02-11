package com.tfg.charmreader.menu.publ.explorar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiGrupoLectura;
import com.tfg.charmreader.interfacesAPI.I_ApiMiembro;
import com.tfg.charmreader.interfacesAPI.I_ImgBB;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.GrupoLectura;
import com.tfg.charmreader.objetosBD.Miembro;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NuevoGrupo extends AppCompatActivity {

    private EditText etNombreGrupo, etUbicacionGrupo, etDescripcionGrupo;
    private AutoCompleteTextView autoCompleteFrecuencia;
    private MaterialButton btnGuardarGrupo;
    private ImageView btnBack, ivPreview;
    private MaterialCardView cardImagen;

    private Uri uriImagenSeleccionada;
    // Tu API KEY integrada
    private final String IMG_BB_KEY = "474a16c3fe5579608f57dfa163e81875";

    private final I_ApiGrupoLectura apiGrupo = API.getInstancia().create(I_ApiGrupoLectura.class);
    private final I_ApiMiembro apiMiembro = API.getInstancia().create(I_ApiMiembro.class);

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    uriImagenSeleccionada = result.getData().getData();
                    ivPreview.setImageURI(uriImagenSeleccionada);
                    ivPreview.setImageTintList(null);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_grupo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        vincularVistas();
        configurarFrecuencia();

        btnBack.setOnClickListener(v -> finish());
        cardImagen.setOnClickListener(v -> abrirGaleria());
        btnGuardarGrupo.setOnClickListener(v -> validarYSubir());
    }

    private void vincularVistas() {
        etNombreGrupo = findViewById(R.id.etNombreGrupo);
        etUbicacionGrupo = findViewById(R.id.etUbicacionGrupo);
        etDescripcionGrupo = findViewById(R.id.etDescripcionGrupo);
        autoCompleteFrecuencia = findViewById(R.id.autoCompleteFrecuencia);
        btnGuardarGrupo = findViewById(R.id.btnGuardarGrupo);
        btnBack = findViewById(R.id.btnBackNuevoGrupo);
        ivPreview = findViewById(R.id.ivPreviewGrupo);
        cardImagen = findViewById(R.id.cardSeleccionarImagen);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void configurarFrecuencia() {
        String[] opciones = {"Semanal", "Quincenal", "Mensual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, opciones);
        autoCompleteFrecuencia.setAdapter(adapter);
        autoCompleteFrecuencia.setText(opciones[0], false);
    }

    private void validarYSubir() {
        String nombre = etNombreGrupo.getText().toString().trim();
        String ubicacion = etUbicacionGrupo.getText().toString().trim();

        if (nombre.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(this, "Completa nombre y ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardarGrupo.setEnabled(false);
        btnGuardarGrupo.setText("SUBIENDO...");

        if (uriImagenSeleccionada != null) {
            subirImagenAImgBB(uriImagenSeleccionada);
        } else {
            // URL por defecto si el usuario no elige foto
            procederACrearGrupo("https://i.ibb.co/vzZ6vKz/default-group.png");
        }
    }

    private void subirImagenAImgBB(Uri uri) {
        try {
            // Crear archivo temporal desde la URI
            File file = new File(getCacheDir(), "upload_image.jpg");
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
            fos.close();
            is.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            // Retrofit específico para ImgBB
            Retrofit retrofitImg = new Retrofit.Builder()
                    .baseUrl("https://api.imgbb.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            I_ImgBB apiImg = retrofitImg.create(I_ImgBB.class);
            apiImg.uploadImage(IMG_BB_KEY, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject json = new JSONObject(response.body().string());
                            String urlFinal = json.getJSONObject("data").getString("url");
                            procederACrearGrupo(urlFinal);
                        } else {
                            restaurarBoton();
                            Toast.makeText(NuevoGrupo.this, "Error en el servidor de imágenes", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        restaurarBoton();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    restaurarBoton();
                    Toast.makeText(NuevoGrupo.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            restaurarBoton();
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void procederACrearGrupo(String urlImagen) {
        String nombre = etNombreGrupo.getText().toString().trim();
        String ubicacion = etUbicacionGrupo.getText().toString().trim();
        String desc = etDescripcionGrupo.getText().toString().trim();
        String frecuenciaTxt = autoCompleteFrecuencia.getText().toString();
        GrupoLectura.Frecuencia frecuenciaEnum = GrupoLectura.stringToFrecuencia(frecuenciaTxt);

        Utilidades.obtenerIdUsuarioDesdeAPI(idUsuario -> {
            if (idUsuario == -1) { restaurarBoton(); return; }

            GrupoLectura nuevo = new GrupoLectura(nombre, ubicacion, desc, frecuenciaEnum, urlImagen, idUsuario);
            apiGrupo.crearGrupo(nuevo).enqueue(new Callback<GrupoLectura>() {
                @Override
                public void onResponse(Call<GrupoLectura> call, Response<GrupoLectura> response) {
                    if (response.isSuccessful()) {
                        autoSuscribirCreador(response.body().getIdGrupo(), idUsuario);
                    } else { restaurarBoton(); }
                }

                @Override
                public void onFailure(Call<GrupoLectura> call, Throwable t) { restaurarBoton(); }
            });
        });
    }

    private void autoSuscribirCreador(int idGrupo, int idUsuario) {
        Miembro admin = new Miembro(idGrupo, idUsuario);
        apiMiembro.unirse(admin).enqueue(new Callback<Miembro>() {
            @Override
            public void onResponse(Call<Miembro> call, Response<Miembro> response) {
                Toast.makeText(NuevoGrupo.this, "¡Grupo creado con éxito!", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onFailure(Call<Miembro> call, Throwable t) { finish(); }
        });
    }

    private void restaurarBoton() {
        runOnUiThread(() -> {
            btnGuardarGrupo.setEnabled(true);
            btnGuardarGrupo.setText("CREAR GRUPO");
        });
    }
}