package com.tfg.charmreader.ui.priv.tusLibros;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityVisorNBinding;
import com.tfg.charmreader.ui.priv.estanteria.ValoracionLibroActivity;
import com.tfg.charmreader.viewmodel.priv.tuslibros.VisorViewModel;

public class VisorNActivity extends AppCompatActivity {

    private ActivityVisorNBinding binding;
    private VisorViewModel viewModel;
    private boolean scrollAplicado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisorNBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(VisorViewModel.class);

        configurarWebView();
        setupObservers();
        configurarGestos();

        if (savedInstanceState == null) {
            int idL = getIntent().getIntExtra("idL", -1);
            int idU = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();
            String url = getIntent().getStringExtra("URL_LIBRO");
            viewModel.cargarDatos(idU, idL, Uri.parse(url));
        }
    }

    private void configurarWebView() {
        binding.webViewContent.getSettings().setJavaScriptEnabled(true);
        binding.webViewContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!scrollAplicado) aplicarScrollGuardado();
                binding.pbVisor.setVisibility(View.GONE);
                binding.webViewContent.animate().alpha(1f).setDuration(300);
            }
        });

        binding.webViewContent.setOnScrollChangeListener((v, x, y, ox, oy) -> {
            if (viewModel.getCurrentChapterIndex() == viewModel.getTotalChapters() - 1) {
                if (obtenerPorcentajeScroll() > 0.98f) viewModel.marcarFinalizado();
            }
        });
    }

    private void setupObservers() {
        viewModel.getChapterHtml().observe(this, html -> {
            binding.webViewContent.setAlpha(0f);
            binding.webViewContent.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        });

        viewModel.getIsLoading().observe(this, loading ->
                binding.pbVisor.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getIsFinished().observe(this, finished -> {
            if (finished) mostrarDialogoFinal();
        });
    }

    private void configurarGestos() {
        GestureDetector gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (e.getX() > binding.webViewContent.getWidth() / 2) viewModel.navegarCapitulo(1);
                else viewModel.navegarCapitulo(-1);
                scrollAplicado = true; // Al cambiar capítulo no aplicamos scroll antiguo
                return true;
            }
        });
        binding.webViewContent.setOnTouchListener((v, event) -> gd.onTouchEvent(event));
    }

    private void aplicarScrollGuardado() {
        if (viewModel.getLibroUsuario().getValue() != null) {
            float scroll = viewModel.getLibroUsuario().getValue().getScroll();
            binding.webViewContent.postDelayed(() -> {
                int y = (int) ((binding.webViewContent.getContentHeight() * getResources().getDisplayMetrics().density - binding.webViewContent.getHeight()) * scroll);
                binding.webViewContent.scrollTo(0, Math.max(0, y));
                scrollAplicado = true;
            }, 400);
        }
    }

    private float obtenerPorcentajeScroll() {
        float h = binding.webViewContent.getContentHeight() * getResources().getDisplayMetrics().density - binding.webViewContent.getHeight();
        return h <= 0 ? 1f : Math.min(1f, binding.webViewContent.getScrollY() / h);
    }

    private void mostrarDialogoFinal() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Lectura completada!")
                .setMessage("¿Quieres valorar el libro?")
                .setCancelable(false)
                .setPositiveButton("VALORAR", (d, w) -> {
                    Intent i = new Intent(this, ValoracionLibroActivity.class);
                    i.putExtra("OBJETO_LIBRO_USUARIO", viewModel.getLibroUsuario().getValue());
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("CERRAR", (d, w) -> finish())
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.guardarEstadoActual(obtenerPorcentajeScroll());
    }
}