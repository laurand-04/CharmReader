package com.tfg.charmreader.autentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiLibro;
import com.tfg.charmreader.interfacesAPI.I_ApiLibrosDeUsuario;
import com.tfg.charmreader.interfacesAPI.I_ApiUsuario;
import com.tfg.charmreader.interfacesAPI.I_ImgBB;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Libro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;
import com.tfg.charmreader.objetosBD.Usuario;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private MaterialButton btnCerrarSesion, btnCambiarPassword, btnExportarPDF;
    private FloatingActionButton btnCambiarFoto;

    private Usuario usuarioLocal;
    private final String IMG_BB_KEY = "474a16c3fe5579608f57dfa163e81875";
    private final I_ApiUsuario apiUsuario = API.getInstancia().create(I_ApiUsuario.class);

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
        btnCambiarFoto = findViewById(R.id.btnAbrirEdicion);
        btnEditarNombre = findViewById(R.id.btnEditarNombre);
        btnExportarPDF = findViewById(R.id.btnExportarPDF);
    }

    private void configurarListeners() {
        btnVolver.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnCambiarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });
        if (btnEditarNombre != null) {
            btnEditarNombre.setOnClickListener(v -> mostrarDialogoNombre());
        }
        btnCambiarPassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                enviarCorreoRecuperacion(user.getEmail());
            }
        });

        btnExportarPDF.setOnClickListener(v -> mostrarOpcionesExportar());

        btnCerrarSesion.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de que deseas salir?")
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("CERRAR SESIÓN", (dialog, which) -> realizarCerrarSesion())
                    .show();
        });
    }

    // --- LÓGICA DE EXPORTACIÓN PDF ---

    private void mostrarOpcionesExportar() {
        String[] opciones = {"Todo mi historial", "Seleccionar rango de fechas"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exportar informe de lectura")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        obtenerDatosParaPDF(null, null);
                    } else {
                        abrirCalendarioRango();
                    }
                })
                .show();
    }

    private void abrirCalendarioRango() {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Selecciona el periodo")
                        .build();

        dateRangePicker.show(getSupportFragmentManager(), "RANGE_PICKER");
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            obtenerDatosParaPDF(new Date(selection.first), new Date(selection.second));
        });
    }

    private void obtenerDatosParaPDF(Date inicio, Date fin) {
        if (usuarioLocal == null) return;
        Toast.makeText(this, "Preparando informe...", Toast.LENGTH_SHORT).show();

        I_ApiLibrosDeUsuario apiLdu = API.getInstancia().create(I_ApiLibrosDeUsuario.class);
        I_ApiLibro apiLibro = API.getInstancia().create(I_ApiLibro.class);

        // Paso 1: Obtener relaciones del usuario
        apiLdu.obtenerLibrosDeUsuario(usuarioLocal.getId()).enqueue(new Callback<List<LibrosDeUsuario>>() {
            @Override
            public void onResponse(Call<List<LibrosDeUsuario>> call, Response<List<LibrosDeUsuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LibrosDeUsuario> relaciones = response.body();
                    List<Integer> idsLibros = new ArrayList<>();

                    // Extraer solo libros finalizados
                    for (LibrosDeUsuario ldu : relaciones) {
                        if (ldu.getFechaFin() != null) idsLibros.add(ldu.getId().getIdL());
                    }

                    if (idsLibros.isEmpty()) {
                        Toast.makeText(Perfil.this, "No tienes libros finalizados para exportar", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Paso 2: Obtener detalles de los libros por sus IDs (Usando tu endpoint /varios)
                    apiLibro.obtenerLibrosPorIds(idsLibros).enqueue(new Callback<List<Libro>>() {
                        @Override
                        public void onResponse(Call<List<Libro>> call, Response<List<Libro>> responseLibros) {
                            if (responseLibros.isSuccessful() && responseLibros.body() != null) {
                                crearDocumentoPDF(relaciones, responseLibros.body(), inicio, fin);
                            }
                        }
                        @Override public void onFailure(Call<List<Libro>> call, Throwable t) {
                            Toast.makeText(Perfil.this, "Error obteniendo títulos", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override public void onFailure(Call<List<LibrosDeUsuario>> call, Throwable t) {
                Toast.makeText(Perfil.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearDocumentoPDF(List<LibrosDeUsuario> relaciones, List<Libro> librosDetalle, Date inicio, Date fin) {
        Map<Integer, Libro> libroMap = new HashMap<>();
        for (Libro l : librosDetalle) libroMap.put(l.getId(), l);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // --- 1. LOGO VECTORIAL (ALTA RESOLUCIÓN) ---
        try {
            // Convertimos el XML a Bitmap con un tamaño grande para que no pixele
            Bitmap bitmap = getBitmapFromVectorDrawable(this, R.mipmap.ic_launcher);

            if (bitmap != null) {
                int logoWidth = 100;
                int logoHeight = 100;
                Bitmap scaledLogo = Bitmap.createScaledBitmap(bitmap, logoWidth, logoHeight, true);
                float centerX = (595 - logoWidth) / 2f;
                canvas.drawBitmap(scaledLogo, centerX, 50, paint);
            }
        } catch (Exception e) {
            Log.e("PDF_LOGO", "Error con vector: " + e.getMessage());
        }

        // --- 2. NOMBRE DE LA APP CENTRADO ---
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        paint.setTextSize(28);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("CharmReader", 595 / 2f, 180, paint);

        // --- 3. INFORMACIÓN DEL USUARIO ---
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL));
        paint.setTextSize(12);
        paint.setColor(Color.DKGRAY);
        String nombreUsuario = (usuarioLocal != null && usuarioLocal.getNombre() != null) ? usuarioLocal.getNombre() : "Usuario";
        canvas.drawText("Lector: " + nombreUsuario, 595 / 2f, 210, paint);

        String periodo = (inicio == null) ? "Historial Completo" : sdf.format(inicio) + " - " + sdf.format(fin);
        canvas.drawText(periodo, 595 / 2f, 225, paint);

        paint.setColor(Color.LTGRAY);
        canvas.drawLine(100, 245, 495, 245, paint);

        // --- 4. LISTADO DE LIBROS ---
        int y = 280;
        paint.setTextAlign(Paint.Align.LEFT);

        for (LibrosDeUsuario ldu : relaciones) {
            if (ldu.getFechaFin() != null) {
                if (inicio != null && fin != null) {
                    if (ldu.getFechaFin().before(inicio) || ldu.getFechaFin().after(fin)) continue;
                }

                Libro detalle = libroMap.get(ldu.getId().getIdL());
                String titulo = (detalle != null) ? detalle.getNombre() : "Libro Desconocido";

                if (y > 780) break;

                paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
                paint.setTextSize(14);
                paint.setColor(Color.BLACK);
                canvas.drawText("📖 " + titulo, 70, y, paint);

                String valStr;
                try {
                    double valor = ldu.getValoracion();
                    valStr = (valor > 0) ? String.format(Locale.getDefault(), "%.1f/5 ⭐", valor) : "Sin valorar";
                } catch (Exception e) { valStr = "Sin valorar"; }

                paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL));
                paint.setTextSize(11);
                paint.setColor(Color.GRAY);
                canvas.drawText("Finalizado: " + sdf.format(ldu.getFechaFin()) + "  |  Nota: " + valStr, 90, y + 20, paint);

                String desc = (ldu.getDescripcion() != null && !ldu.getDescripcion().isEmpty()) ? ldu.getDescripcion() : "Sin comentario.";
                if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
                canvas.drawText("Reseña: " + desc, 90, y + 38, paint);

                y += 75;
            }
        }

        document.finishPage(page);

        File file = new File(getExternalFilesDir(null), "MisLecturas_CharmReader.pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            compartirArchivo(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            // Creamos un bitmap con un tamaño base alto (500x500) para asegurar nitidez absoluta
            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    private void compartirArchivo(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir informe de lectura"));
    }

    // --- FIN LÓGICA PDF ---

    private void cargarDatosUsuario() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null && fbUser.getEmail() != null) {
            tvEmailUsuario.setText(fbUser.getEmail());

            apiUsuario.getIdUsuarioPorCorreo(fbUser.getEmail()).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        usuarioLocal = response.body();
                        actualizarInterfaz();
                    }
                }
                @Override public void onFailure(Call<Usuario> call, Throwable t) {}
            });
        }
    }

    private void actualizarInterfaz() {
        if (usuarioLocal == null) return;
        tvNombreUsuario.setText((usuarioLocal.getNombre() != null && !usuarioLocal.getNombre().isEmpty()) ?
                usuarioLocal.getNombre() : "Usuario #" + usuarioLocal.getId());

        if (usuarioLocal.getFoto() != null && !usuarioLocal.getFoto().isEmpty()) {
            Glide.with(this).load(usuarioLocal.getFoto()).placeholder(R.drawable.ic_person).centerCrop().into(ivFotoPerfil);
        }
    }

    private void mostrarDialogoNombre() {
        if (usuarioLocal == null) return;
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_editar_perfil);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        com.google.android.material.textfield.TextInputEditText et = dialog.findViewById(R.id.etNuevoNombreDialog);
        com.google.android.material.button.MaterialButton btn = dialog.findViewById(R.id.btnGuardarNombre);
        ImageView btnCerrar = dialog.findViewById(R.id.btnCerrarDialog);
        et.setText(usuarioLocal.getNombre());
        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btn.setOnClickListener(v -> {
            String nuevoNom = et.getText().toString().trim();
            if (!nuevoNom.isEmpty()) {
                usuarioLocal.setNombre(nuevoNom);
                ejecutarActualizaciónEnAPI();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void procesarImagenSeleccionada(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
            subirAImgBB(byteBuffer.toByteArray());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void subirAImgBB(byte[] data) {
        if (data == null) return;
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), data);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "avatar.jpg", requestFile);
        new Retrofit.Builder().baseUrl("https://api.imgbb.com/").addConverterFactory(GsonConverterFactory.create()).build()
                .create(I_ImgBB.class).uploadImage(IMG_BB_KEY, body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                usuarioLocal.setFoto(new JSONObject(response.body().string()).getJSONObject("data").getString("url"));
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

    private void realizarCerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(Perfil.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void enviarCorreoRecuperacion(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show());
    }
}