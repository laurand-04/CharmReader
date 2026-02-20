package com.tfg.charmreader.autentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.interfacesAPI.I_ImgBB;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Usuario;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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

public class Perfil extends AppCompatActivity {

    private ImageView btnVolver, ivFotoPerfil, btnEditarNombre;
    private TextView tvNombreUsuario, tvEmailUsuario;
    private MaterialButton btnCerrarSesion, btnCambiarPassword;
    private FloatingActionButton btnCambiarFoto;

    private Usuario usuarioLocal;
    private final String IMG_BB_KEY = "474a16c3fe5579608f57dfa163e81875";
    private final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

    // Lanzador para seleccionar imagen de la galería
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    procesarImagenSeleccionada(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_perfil);

        vincularVistas();
        cargarDatosUsuario();
        configurarListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void vincularVistas() {
        btnVolver = findViewById(R.id.btnVolverPerfil);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        tvEmailUsuario = findViewById(R.id.tvEmailUsuario);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCambiarPassword = findViewById(R.id.btnCambiarPassword);
        btnCambiarFoto = findViewById(R.id.btnAbrirEdicion); // El ID del FAB en tu XML
        btnEditarNombre = findViewById(R.id.btnEditarNombre); // Asegúrate de añadir este ID a tu ImageView del lápiz de nombre
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Lápiz de la Foto: Abre Galería
        btnCambiarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        // Lápiz del Nombre: Abre Diálogo
        if (btnEditarNombre != null) {
            btnEditarNombre.setOnClickListener(v -> mostrarDialogoNombre());
        }

        btnCambiarPassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                enviarCorreoRecuperacion(user.getEmail());
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de que deseas salir?")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("CERRAR SESIÓN", (dialog, which) -> realizarCerrarSesion())
                    .show();
        });
    }

    private void cargarDatosUsuario() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null && fbUser.getEmail() != null) {
            tvEmailUsuario.setText(fbUser.getEmail());

            apiUsuario.getIdUsuarioPorCorreo(fbUser.getEmail()).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        usuarioLocal = response.body();

                        // REGLA UNIFICADA: Nombre o Usuario #ID
                        if (usuarioLocal.getNombre() != null && !usuarioLocal.getNombre().isEmpty()) {
                            tvNombreUsuario.setText(usuarioLocal.getNombre());
                        } else {
                            tvNombreUsuario.setText("Usuario #" + usuarioLocal.getId());
                        }

                        if (usuarioLocal.getFoto() != null && !usuarioLocal.getFoto().isEmpty()) {
                            Glide.with(Perfil.this)
                                    .load(usuarioLocal.getFoto())
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .centerCrop()
                                    .into(ivFotoPerfil);
                        }
                    }
                }
                @Override public void onFailure(Call<Usuario> call, Throwable t) {
                    Toast.makeText(Perfil.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void actualizarInterfaz() {
        if (usuarioLocal.getNombre() != null && !usuarioLocal.getNombre().isEmpty()) {
            tvNombreUsuario.setText(usuarioLocal.getNombre());
        } else {
            tvNombreUsuario.setText("Usuario #" + usuarioLocal.getId());
        }

        if (usuarioLocal.getFoto() != null && !usuarioLocal.getFoto().isEmpty()) {
            Glide.with(this)
                    .load(usuarioLocal.getFoto())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .centerCrop()
                    .into(ivFotoPerfil);
        }
    }

    private void mostrarDialogoNombre() {
        if (usuarioLocal == null) return;

        // 1. Crear el diálogo con un estilo transparente
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_editar_perfil); // El XML de arriba

        // 2. HACER EL FONDO TRANSPARENTE
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // 3. Vincular vistas del diálogo
        com.google.android.material.textfield.TextInputEditText et = dialog.findViewById(R.id.etNuevoNombreDialog);
        com.google.android.material.button.MaterialButton btn = dialog.findViewById(R.id.btnGuardarNombre);

        et.setText(usuarioLocal.getNombre());

        btn.setOnClickListener(v -> {
            String nuevoNom = et.getText().toString().trim();
            if (!nuevoNom.isEmpty()) {
                usuarioLocal.setNombre(nuevoNom);
                ejecutarActualizaciónEnAPI();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void procesarImagenSeleccionada(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] bytes = getBytes(is);
            subirAImgBB(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subirAImgBB(byte[] data) {
        if (data == null) return;
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), data);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "avatar.jpg", requestFile);

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
                        JSONObject json = new JSONObject(response.body().string());
                        String urlFinal = json.getJSONObject("data").getString("url");

                        usuarioLocal.setFoto(urlFinal);
                        ejecutarActualizaciónEnAPI();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void ejecutarActualizaciónEnAPI() {
        apiUsuario.guardarUsuario(usuarioLocal).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Perfil.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    actualizarInterfaz();
                }
            }
            @Override public void onFailure(Call<Usuario> call, Throwable t) {}
        });
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // ... (Métodos de cerrar sesión y recuperación de contraseña iguales)
    private void realizarCerrarSesion() {
        // 1. Cerramos sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. LIMPIEZA TOTAL de las SharedPreferences
        // Al hacer .clear() borramos: logeado, esAdmin, correoUsuario e idUsuario
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        // 3. Log de control para debug
        Log.d("SESSION_LOG", "Sesión limpiada y SharedPreferences borradas.");

        // 4. Redirigir al Login y LIMPIAR el historial de actividades
        // Es vital usar FLAG_ACTIVITY_CLEAR_TASK para que no se pueda volver atrás con el botón del sistema
        Intent intent = new Intent(Perfil.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        startActivity(intent);
        finish();
    }

    private void enviarCorreoRecuperacion(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show());
    }
}