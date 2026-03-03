package com.tfg.charmreader.ui.admin;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.tfg.charmreader.databinding.ActivityEstadisticasAdminBinding;
import com.tfg.charmreader.viewmodel.admin.EstadisticasViewModel;
import java.util.Locale;

public class EstadisticasAdminActivity extends AppCompatActivity {

    private ActivityEstadisticasAdminBinding binding;
    private EstadisticasViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEstadisticasAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);

        binding.btnBackStats.setOnClickListener(v -> finish());

        observeViewModel();
        viewModel.cargarEstadisticas();
    }

    private void observeViewModel() {

        viewModel.getIsLoading().observe(this, loading -> {
            if (loading) {
                binding.layoutLoading.setVisibility(View.VISIBLE);
                binding.layoutContenido.setAlpha(0.3f); // Efecto visual de deshabilitado
            } else {
                binding.layoutLoading.setVisibility(View.GONE);
                binding.layoutContenido.setAlpha(1.0f);
            }
        });

        viewModel.getStatsState().observe(this, state -> {


            // 1. Usuarios
            binding.tvLabelUsuariosTotal.setText("Usuarios totales: " + state.totalUsuarios);
            binding.progressUsuarios.setMax(500);
            binding.progressUsuarios.setProgress(state.totalUsuarios.intValue());

            // 2. En Curso
            binding.tvLabelEnCurso.setText("En curso: " + state.lecturasActivas);
            binding.progressEnCurso.setMax(100);
            binding.progressEnCurso.setProgress(state.lecturasActivas.intValue());

            // 3. Grupo Top
            binding.tvNombreGrupoTop.setText(state.grupoTop);

            // 4. Media (Densidad)
            binding.tvMediaUsuarios.setText(String.format(Locale.getDefault(), "%.1f", state.densidad));

            // 5. Tiempo Medio
            binding.tvTiempoMedio.setText(String.format(Locale.getDefault(), "%.0f días", state.tiempoMedio));

            // 6. Finalizados Mes
            binding.tvFinalizadosMes.setText(String.valueOf(state.finalizadosMes));
        });
    }

}