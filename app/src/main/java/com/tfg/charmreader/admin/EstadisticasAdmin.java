package com.tfg.charmreader.admin;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiAdmin;
import com.tfg.charmreader.objetosBD.API;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasAdmin extends AppCompatActivity {

    private TextView tvEnCurso, tvUsuariosTotal, tvGrupoTop, tvMedia;
    private ProgressBar progressEnCurso, progressUsuarios;
    private ImageView btnBack;
    private I_ApiAdmin api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Barra de estado blanca
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_estadisticas_admin);

        vincularVistas();

        api = API.getInstancia().create(I_ApiAdmin.class);

        cargarEstadisticas();
    }

    private void vincularVistas() {
        tvEnCurso = findViewById(R.id.tvLabelEnCurso);
        tvUsuariosTotal = findViewById(R.id.tvLabelUsuariosTotal);
        tvGrupoTop = findViewById(R.id.tvNombreGrupoTop);
        tvMedia = findViewById(R.id.tvMediaUsuarios);
        progressEnCurso = findViewById(R.id.progressEnCurso);
        progressUsuarios = findViewById(R.id.progressUsuarios);
        btnBack = findViewById(R.id.btnBackStats);

        btnBack.setOnClickListener(v -> finish());
    }

    private void cargarEstadisticas() {
        // 1. Total de Usuarios
        api.getTotalUsuarios().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long total = response.body();
                    tvUsuariosTotal.setText("Usuarios totales: " + total);
                    // Usamos un máximo de 500 para el ejemplo visual de la barra
                    progressUsuarios.setMax(500);
                    progressUsuarios.setProgress((int) total);
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {}
        });

        // 2. Lecturas Activas
        api.getLecturasActivas().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long activas = response.body();
                    tvEnCurso.setText("En curso: " + activas);
                    progressEnCurso.setMax(100);
                    progressEnCurso.setProgress((int) activas);
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(EstadisticasAdmin.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Grupo más popular
        api.getNombreGrupoTop().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvGrupoTop.setText(response.body());
                } else {
                    tvGrupoTop.setText("Sin grupos registrados");
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                tvGrupoTop.setText("Error al cargar");
            }
        });

        // 4. Densidad (Media)
        api.getDensidad().enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvMedia.setText(String.format(Locale.getDefault(), "%.1f", response.body()));
                }
            }
            @Override
            public void onFailure(Call<Double> call, Throwable t) {}
        });
    }
}