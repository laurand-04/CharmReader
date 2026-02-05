package com.tfg.charmreader.admin;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tfg.charmreader.R;
import com.tfg.charmreader.interfacesAPI.I_ApiAdmin;
import com.tfg.charmreader.objetosBD.API;

import java.util.Locale;

// IMPORTANTE: Estos deben ser de retrofit2 obligatoriamente
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstadisticasAdmin extends AppCompatActivity {

    private TextView tvEnCurso, tvUsuariosTotal, tvGrupoTop, tvMedia;
    private ProgressBar progressEnCurso;
    private I_ApiAdmin api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_admin);

        // Enlazar vistas (Asegúrate de que los IDs coincidan con tu XML)
        tvEnCurso = findViewById(R.id.tvLabelEnCurso);
        tvUsuariosTotal = findViewById(R.id.tvLabelFinalizadas); // Reutilizamos este TextView para Usuarios Totales
        tvGrupoTop = findViewById(R.id.tvNombreGrupoTop);
        tvMedia = findViewById(R.id.tvMediaUsuarios);
        progressEnCurso = findViewById(R.id.progressEnCurso);

        api = API.getInstancia().create(I_ApiAdmin.class);

        cargarEstadisticas();
    }

    private void cargarEstadisticas() {

        // 1. Total de Usuarios
        api.getTotalUsuarios().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvUsuariosTotal.setText("Usuarios registrados: " + response.body());
                }
            }
            @Override
            public void onFailure(Call<Long> call, Throwable t) {}
        });

        // 2. Lecturas Activas (Barra de progreso)
        api.getLecturasActivas().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long activas = response.body();
                    tvEnCurso.setText("Lecturas actuales: " + activas);
                    // Como no tenemos un máximo real, usamos 100 o lo que consideres
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
                    tvGrupoTop.setText("Sin datos");
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // Esto imprimirá el error real en el Logcat de Android Studio
                android.util.Log.e("API_ERROR", "Fallo en Grupo Top: " + t.getMessage());
                tvGrupoTop.setText("Error: " + t.getMessage());
            }
        });

        // 4. Densidad (Media de usuarios por grupo)
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