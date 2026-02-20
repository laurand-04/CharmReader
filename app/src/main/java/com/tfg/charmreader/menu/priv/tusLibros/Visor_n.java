package com.tfg.charmreader.menu.priv.tusLibros;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import retrofit2.Response;

public class Visor_n extends AppCompatActivity {

    private WebView webViewContent;
    private ProgressBar pbVisor;
    private List<Resource> chapters = new ArrayList<>();
    private int currentChapter = 0;
    private float scrollGuardado = 0f;
    private GestureDetector gestureDetector;
    private boolean apiCargada = false;
    private boolean epubCargado = false;
    private int idUsuario; // 🔥 Variable local para no consultar SharedPreferences constantemente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visor_n);

        // 1. Obtener ID de usuario localmente al iniciar
        SharedPreferences prefs = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Vincular vistas
        webViewContent = findViewById(R.id.webViewContent);
        pbVisor = findViewById(R.id.pbVisor);

        webViewContent.getSettings().setJavaScriptEnabled(true);
        webViewContent.setAlpha(0f);

        webViewContent.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (scrollGuardado > 0f) {
                    aplicarScroll();
                } else {
                    mostrarContenidoFinal();
                }
            }
        });

        configurarGestos();

        String urlLibro = getIntent().getStringExtra("URL_LIBRO");
        if (urlLibro != null) {
            Uri epubUri = Uri.parse(urlLibro);
            cargarProgresoDesdeAPI();
            readEpub(epubUri);
        } else {
            Toast.makeText(this, "No se proporcionó EPUB", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configurarGestos() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float x = e.getX();
                if (x > webViewContent.getWidth() / 2) nextChapter();
                else prevChapter();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) nextChapter();
                    else prevChapter();
                    return true;
                }
                return false;
            }
        });
        webViewContent.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarSeguimiento();
    }

    private void nextChapter() {
        if (currentChapter < chapters.size() - 1) {
            guardarSeguimiento();
            currentChapter++;
            scrollGuardado = 0f;
            showChapter(currentChapter);
        }
    }

    private void prevChapter() {
        if (currentChapter > 0) {
            guardarSeguimiento();
            currentChapter--;
            scrollGuardado = 0f;
            showChapter(currentChapter);
        }
    }

    private void readEpub(Uri epubUri) {
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(epubUri);
                nl.siegmann.epublib.domain.Book book = new EpubReader().readEpub(is);
                List<Resource> tempChapters = new ArrayList<>();
                for (TOCReference tocRef : book.getTableOfContents().getTocReferences()) {
                    if (tocRef.getResource() != null) tempChapters.add(tocRef.getResource());
                }

                runOnUiThread(() -> {
                    this.chapters = tempChapters;
                    this.epubCargado = true;
                    verificarYMostrarCapitulo();
                });
            } catch (Exception e) {
                Log.e("EPUB", "Error al leer EPUB", e);
            }
        }).start();
    }

    private void cargarProgresoDesdeAPI() {
        new Thread(() -> {
            try {
                int idLibro = getIntent().getIntExtra("idL", -1);
                // 🔥 Ya no usamos Utilidades.obtenerIdUsuarioDesdeAPI(), usamos la variable local
                Response<LibrosDeUsuario> response = Utilidades.apiLibrosDeUsuario
                        .getLibrodeUsuario(idUsuario, idLibro).execute();

                if (response.isSuccessful() && response.body() != null) {
                    currentChapter = response.body().getCapitulo();
                    scrollGuardado = response.body().getScroll();
                    runOnUiThread(() -> {
                        this.apiCargada = true;
                        verificarYMostrarCapitulo();
                    });
                }
            } catch (Exception e) {
                Log.e("API", "Error cargando progreso", e);
            }
        }).start();
    }

    private void verificarYMostrarCapitulo() {
        if (epubCargado && apiCargada) {
            showChapter(currentChapter);
        }
    }

    private void showChapter(int index) {
        if (index < 0 || index >= chapters.size()) return;
        try {
            pbVisor.setVisibility(View.VISIBLE);
            webViewContent.setAlpha(0f);

            Resource res = chapters.get(index);
            String chapterHtml = new String(res.getData());
            String html = "<html><head><style>body { font-family: sans-serif; font-size:18px; line-height:1.6; padding:16px; margin:0; }</style></head><body>"
                    + chapterHtml + "</body></html>";
            webViewContent.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            Log.e("VISOR", "Error mostrando capítulo", e);
        }
    }

    private void guardarSeguimiento() {
        final float porcentaje = obtenerPorcentajeScroll();
        final int capitulo = currentChapter;
        final int idLibro = getIntent().getIntExtra("idL", -1);

        new Thread(() -> {
            try {
                // 🔥 Usamos el idUsuario que cargamos en el onCreate
                if (idUsuario == -1 || idLibro == -1) return;

                Response<LibrosDeUsuario> response = Utilidades.apiLibrosDeUsuario
                        .getLibrodeUsuario(idUsuario, idLibro).execute();

                if (response.isSuccessful() && response.body() != null) {
                    LibrosDeUsuario progreso = response.body();
                    progreso.setCapitulo(capitulo);
                    progreso.setScroll(porcentaje);
                    Utilidades.apiLibrosDeUsuario.guardarProgreso(progreso).execute();
                    Log.d("PROGRESO", "Guardado Cap: " + capitulo + " Scroll: " + porcentaje);
                }
            } catch (Exception e) {
                Log.e("PROGRESO", "Error al guardar", e);
            }
        }).start();
    }

    private float obtenerPorcentajeScroll() {
        int scrollY = webViewContent.getScrollY();
        float contentHeight = webViewContent.getContentHeight() * webViewContent.getScale();
        int viewHeight = webViewContent.getHeight();
        float maxScroll = contentHeight - viewHeight;

        if (maxScroll <= 0) return 0f;
        return Math.max(0f, Math.min(scrollY / maxScroll, 1f));
    }

    private void aplicarScroll() {
        webViewContent.postDelayed(new Runnable() {
            int intentos = 0;
            @Override
            public void run() {
                float contentHeight = webViewContent.getContentHeight() * webViewContent.getScale();
                if (contentHeight > webViewContent.getHeight() || intentos > 10) {
                    int maxScroll = (int) (contentHeight - webViewContent.getHeight());
                    int targetY = (int) (maxScroll * scrollGuardado);

                    webViewContent.scrollTo(0, targetY);
                    scrollGuardado = 0f;
                    mostrarContenidoFinal();
                } else {
                    intentos++;
                    webViewContent.postDelayed(this, 200);
                }
            }
        }, 300);
    }

    private void mostrarContenidoFinal() {
        pbVisor.setVisibility(View.GONE);
        webViewContent.animate().alpha(1f).setDuration(400).start();
    }
}