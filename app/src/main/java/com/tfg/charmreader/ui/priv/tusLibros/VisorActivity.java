package com.tfg.charmreader.ui.priv.tusLibros;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.GrupoLectura;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.databinding.ActivityVisorNBinding;
import com.tfg.charmreader.ui.priv.estanteria.ValoracionLibroActivity;
import com.tfg.charmreader.viewmodel.priv.tuslibros.VisorViewModel;

public class VisorActivity extends AppCompatActivity {

    private ActivityVisorNBinding binding;
    private VisorViewModel viewModel;
    private boolean scrollAplicado = false;
    private boolean grupo = false;

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
            //Privada
            int idL = getIntent().getIntExtra("idL", -1);
            int idU = AuthRepository.getInstance(getApplicationContext()).getIdUsuario();
            LibrosDeUsuario ldu = (LibrosDeUsuario) getIntent().getSerializableExtra("OBJETO_LIBRO_USUARIO");
            //Publica
            BookEn libro = (BookEn) getIntent().getSerializableExtra("Libro");
            this.grupo = getIntent().getBooleanExtra("grupoB", false);
            GrupoLectura grupoLectura = (GrupoLectura) getIntent().getSerializableExtra("grupoO");

            //String url = getIntent().getStringExtra("URL_LIBRO");
            //viewModel.inicializarVisor(idU, idL, Uri.parse(url));
            if(ldu != null) {
                Log.d("Leer", "Cargando Libro en privado");
                viewModel.verificarYAbrirLibro(ldu);
            }
            else if (libro != null){
                Log.d("Leer", "Cargando Libro ");
                viewModel.verificarYAbrirLibro(libro, grupoLectura.getIdGrupo());
            }
            else {
                Log.e("onCreate de VisorActivity", "El libro de usuario es nulo");
                finish();
            }
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
                actualizarTextosProgreso();
            }
        });

        binding.webViewContent.setOnScrollChangeListener((v, x, y, ox, oy) -> {
            actualizarTextosProgreso();
            if (viewModel.getCurrentChapterIndex() == viewModel.getTotalChapters() - 1) {
                if (obtenerPorcentajeScroll() > 0.98f && this.grupo == false) viewModel.marcarFinalizado();
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
                actualizarTextosProgreso();
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

    private void actualizarTextosProgreso() {
        // 1. Calcular porcentaje del capítulo (Scroll)
        float porcentajeCap = obtenerPorcentajeScroll() * 100;
        binding.tvProgresoCapitulo.setText(String.format("Capítulo: %.0f%%", porcentajeCap));

        // 2. Calcular porcentaje del libro (Capítulos)
        int total = viewModel.getTotalChapters();
        if (total > 0) {
            // Calculamos en qué capítulo estamos respecto al total
            // Sumamos 1 al index porque es 0-based
            float progresoGral = ((float) (viewModel.getCurrentChapterIndex() + 1) / total) * 100;
            binding.tvProgresoLibro.setText(String.format("Libro: %.0f%%", progresoGral));
        }
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
        if(this.grupo == false)
            viewModel.guardarEstadoActual(obtenerPorcentajeScroll());
    }
}