package com.tfg.charmreader.menu.priv.tusLibros;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.menu.priv.estanteria.ValoracionLibro;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

public class Visor_n extends AppCompatActivity {

    private WebView webViewContent;
    private ProgressBar pbVisor;
    private List<Resource> chapters = new ArrayList<>();
    private Book epubBook;
    private int currentChapter = 0;
    private float scrollGuardado = 0f;
    private boolean apiCargada = false;
    private boolean epubCargado = false;
    private LibrosDeUsuario libroUsuarioActual;
    private boolean dialogoMostrado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.tfg.charmreader.R.layout.activity_visor_n);

        libroUsuarioActual = (LibrosDeUsuario) getIntent().getSerializableExtra("OBJETO_LIBRO_USUARIO");
        String urlLibro = getIntent().getStringExtra("URL_LIBRO");

        webViewContent = findViewById(com.tfg.charmreader.R.id.webViewContent);
        pbVisor = findViewById(com.tfg.charmreader.R.id.pbVisor);

        webViewContent.getSettings().setJavaScriptEnabled(true);
        webViewContent.getSettings().setAllowFileAccess(true);
        webViewContent.getSettings().setDomStorageEnabled(true);
        webViewContent.setAlpha(0f);

        webViewContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                aplicarScroll();
                mostrarContenidoFinal();
            }
        });

        webViewContent.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (epubCargado && currentChapter == chapters.size() - 1) {
                if (obtenerPorcentajeScroll() > 0.98f) marcarComoFinalizado();
            }
        });

        configurarGestos();

        if (urlLibro != null) {
            cargarProgresoDesdeAPI();
            readEpub(Uri.parse(urlLibro));
        }
    }

    private void readEpub(Uri epubUri) {
        new Thread(() -> {
            try {
                InputStream is;
                if (epubUri.getScheme() != null && epubUri.getScheme().startsWith("http")) {
                    is = new URL(epubUri.toString()).openStream();
                } else {
                    is = getContentResolver().openInputStream(epubUri);
                }
                this.epubBook = new EpubReader().readEpub(is);
                List<Resource> tempChapters = new ArrayList<>();
                for (SpineReference spineRef : epubBook.getSpine().getSpineReferences()) {
                    tempChapters.add(spineRef.getResource());
                }
                runOnUiThread(() -> {
                    this.chapters = tempChapters;
                    this.epubCargado = true;
                    verificarYMostrar();
                });
            } catch (Exception e) { Log.e("VISOR", "Error EPUB", e); }
        }).start();
    }

    private void showChapter(int index) {
        if (chapters.isEmpty() || index < 0 || index >= chapters.size()) return;

        new Thread(() -> {
            try {
                Resource res = chapters.get(index);
                String content = new String(res.getData(), "UTF-8");

                // 🔥 PROCESADOR DE IMÁGENES: Reemplaza src=".." por src="data:image/..;base64,.."
                content = procesarImagenesHtml(content);

                String finalHtml = "<html><head><style>" +
                        "body{font-family:sans-serif; font-size:18px; line-height:1.6; padding:20px; color:#333; background-color:white;}" +
                        "img{max-width:100% !important; height:auto !important; display:block; margin: 20px auto;}" +
                        "</style></head><body>" + content + "</body></html>";

                runOnUiThread(() -> {
                    pbVisor.setVisibility(View.VISIBLE);
                    webViewContent.setAlpha(0f);
                    // Cargamos directamente sin BaseURL porque las imágenes ya están inyectadas
                    webViewContent.loadDataWithBaseURL(null, finalHtml, "text/html", "UTF-8", null);
                });
            } catch (Exception e) { Log.e("VISOR", "Error procesando capítulo", e); }
        }).start();
    }

    private String procesarImagenesHtml(String html) {
        if (epubBook == null) return html;

        // Buscamos todas las etiquetas img
        Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
        Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String src = matcher.group(1);
            // Intentamos encontrar el recurso en el EPUB (limpiando rutas relativas tipo ../)
            String cleanPath = src.replace("../", "");
            Resource imageRes = epubBook.getResources().getByHref(cleanPath);

            // Si no lo encuentra, probamos buscando solo por nombre de archivo
            if (imageRes == null) {
                String fileName = new java.io.File(src).getName();
                for (Resource r : epubBook.getResources().getAll()) {
                    if (r.getHref().endsWith(fileName)) {
                        imageRes = r;
                        break;
                    }
                }
            }

            if (imageRes != null) {
                try {
                    String base64Img = Base64.encodeToString(imageRes.getData(), Base64.NO_WRAP);
                    String mimeType = imageRes.getMediaType().toString();
                    String dataUri = "data:" + mimeType + ";base64," + base64Img;
                    matcher.appendReplacement(sb, matcher.group(0).replace(src, dataUri));
                } catch (Exception e) { matcher.appendReplacement(sb, matcher.group(0)); }
            } else {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // --- MÉTODOS DE APOYO (SIN CAMBIOS) ---

    private void cargarProgresoDesdeAPI() {
        new Thread(() -> {
            try {
                int idU = getSharedPreferences("sesion_usuario", MODE_PRIVATE).getInt("idUsuario", -1);
                int idL = getIntent().getIntExtra("idL", -1);
                retrofit2.Response<LibrosDeUsuario> res = Utilidades.apiLibrosDeUsuario.getLibrodeUsuario(idU, idL).execute();
                if (res.isSuccessful() && res.body() != null) {
                    libroUsuarioActual = res.body();
                    currentChapter = libroUsuarioActual.getCapitulo();
                    scrollGuardado = libroUsuarioActual.getScroll();
                    if (libroUsuarioActual.getFechaInicio() == null) {
                        libroUsuarioActual.setFechaInicio(new Date());
                        guardarProgresoAPI();
                    }
                }
                runOnUiThread(() -> { this.apiCargada = true; verificarYMostrar(); });
            } catch (Exception e) { runOnUiThread(() -> { this.apiCargada = true; verificarYMostrar(); }); }
        }).start();
    }

    private void verificarYMostrar() {
        if (epubCargado && apiCargada) {
            if (currentChapter >= chapters.size()) currentChapter = chapters.size() - 1;
            showChapter(currentChapter);
        }
    }

    private void configurarGestos() {
        GestureDetector gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (e.getX() > webViewContent.getWidth() / 2) {
                    if (currentChapter < chapters.size() - 1) {
                        currentChapter++; scrollGuardado = 0f; showChapter(currentChapter);
                    } else marcarComoFinalizado();
                } else {
                    if (currentChapter > 0) {
                        currentChapter--; scrollGuardado = 0f; showChapter(currentChapter);
                    }
                }
                return true;
            }
        });
        webViewContent.setOnTouchListener((v, event) -> gd.onTouchEvent(event));
    }

    private void marcarComoFinalizado() {
        if (libroUsuarioActual != null && libroUsuarioActual.getFechaFin() == null) {
            libroUsuarioActual.setFechaFin(new Date());
            libroUsuarioActual.setCapitulo(chapters.size() - 1);
            libroUsuarioActual.setScroll(1f);
            guardarProgresoAPI();
        }
        if (!dialogoMostrado) mostrarAvisoFinalizacion();
    }

    private void mostrarAvisoFinalizacion() {
        dialogoMostrado = true;
        runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("¡Lectura completada!")
                    .setMessage("Has llegado al final. ¿Quieres valorar el libro?")
                    .setCancelable(false)
                    .setPositiveButton("VALORAR", (d, w) -> {
                        Intent i = new Intent(this, ValoracionLibro.class);
                        i.putExtra("OBJETO_LIBRO_USUARIO", libroUsuarioActual);
                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton("CERRAR", (d, w) -> finish())
                    .show();
        });
    }

    private void guardarProgresoAPI() {
        new Thread(() -> {
            try { Utilidades.apiLibrosDeUsuario.guardarProgreso(libroUsuarioActual).execute(); }
            catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private float obtenerPorcentajeScroll() {
        float d = getResources().getDisplayMetrics().density;
        float h = webViewContent.getContentHeight() * d - webViewContent.getHeight();
        return h <= 0 ? 1f : Math.min(1f, webViewContent.getScrollY() / h);
    }

    private void aplicarScroll() {
        if (scrollGuardado > 0) {
            webViewContent.postDelayed(() -> {
                float d = getResources().getDisplayMetrics().density;
                int y = (int) ((webViewContent.getContentHeight() * d - webViewContent.getHeight()) * scrollGuardado);
                webViewContent.scrollTo(0, Math.max(0, y));
            }, 400);
        }
    }

    private void mostrarContenidoFinal() {
        pbVisor.setVisibility(View.GONE);
        webViewContent.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (libroUsuarioActual != null && !chapters.isEmpty()) {
            final int cap = currentChapter;
            final float scr = obtenerPorcentajeScroll();
            new Thread(() -> {
                try {
                    if (libroUsuarioActual.getFechaFin() == null) {
                        libroUsuarioActual.setCapitulo(cap);
                        libroUsuarioActual.setScroll(scr);
                    }
                    Utilidades.apiLibrosDeUsuario.guardarProgreso(libroUsuarioActual).execute();
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
    }
}