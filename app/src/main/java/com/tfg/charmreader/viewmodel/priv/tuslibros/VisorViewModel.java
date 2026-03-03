package com.tfg.charmreader.viewmodel.priv.tuslibros;

import android.app.Application;
import android.net.Uri;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.repository.priv.LibroRepository;

import java.io.IOException;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisorViewModel extends AndroidViewModel {
    private final LibroRepository repository = new LibroRepository();

    private final MutableLiveData<Book> epubBook = new MutableLiveData<>();
    private final MutableLiveData<List<Resource>> chapters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<LibrosDeUsuario> libroUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> chapterHtml = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>(false);

    private int currentChapter = 0;

    public VisorViewModel(@NonNull Application application) { super(application); }

    // Getters LiveData
    public LiveData<String> getChapterHtml() { return chapterHtml; }
    public LiveData<LibrosDeUsuario> getLibroUsuario() { return libroUsuario; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsFinished() { return isFinished; }
    public int getCurrentChapterIndex() { return currentChapter; }
    public int getTotalChapters() { return chapters.getValue() != null ? chapters.getValue().size() : 0; }

    public void cargarDatos(int idU, int idL, Uri epubUri) {
        // Cargar progreso API
        repository.obtenerProgreso(idU, idL, new Callback<LibrosDeUsuario>() {
            @Override
            public void onResponse(Call<LibrosDeUsuario> call, Response<LibrosDeUsuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LibrosDeUsuario ldu = response.body();
                    if (ldu.getFechaInicio() == null) ldu.setFechaInicio(new Date());
                    libroUsuario.postValue(ldu);
                    currentChapter = ldu.getCapitulo();
                }
                leerEpub(epubUri);
            }
            @Override public void onFailure(Call<LibrosDeUsuario> call, Throwable t) { leerEpub(epubUri); }
        });
    }

    private void leerEpub(Uri uri) {
        new Thread(() -> {
            try {
                InputStream is = uri.getScheme().startsWith("http") ?
                        new URL(uri.toString()).openStream() :
                        getApplication().getContentResolver().openInputStream(uri);

                Book book = new EpubReader().readEpub(is);
                List<Resource> tempChapters = new ArrayList<>();
                for (SpineReference sr : book.getSpine().getSpineReferences()) tempChapters.add(sr.getResource());

                epubBook.postValue(book);
                chapters.postValue(tempChapters);
                renderizarCapitulo(currentChapter);
            } catch (Exception e) { isLoading.postValue(false); }
        }).start();
    }

    public void navegarCapitulo(int direccion) {
        int nuevoIndex = currentChapter + direccion;
        if (chapters.getValue() != null && nuevoIndex >= 0 && nuevoIndex < chapters.getValue().size()) {
            currentChapter = nuevoIndex;
            renderizarCapitulo(currentChapter);
        } else if (nuevoIndex >= chapters.getValue().size()) {
            marcarFinalizado();
        }
    }

    private void renderizarCapitulo(int index) {
        isLoading.postValue(true);
        new Thread(() -> {
            try {
                Resource res = chapters.getValue().get(index);
                String rawHtml = new String(res.getData(), "UTF-8");
                String processedHtml = procesarImagenesHtml(rawHtml);

                String styledHtml = "<html><head><style>" +
                        "body{font-family:sans-serif; font-size:18px; line-height:1.6; padding:20px; color:#333;}" +
                        "img{max-width:100% !important; height:auto !important; display:block; margin: 20px auto;}" +
                        "</style></head><body>" + processedHtml + "</body></html>";

                chapterHtml.postValue(styledHtml);
                isLoading.postValue(false);
            } catch (Exception e) { isLoading.postValue(false); }
        }).start();
    }

    private String procesarImagenesHtml(String html) throws IOException {
        Book book = epubBook.getValue();
        if (book == null) return html;
        Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String src = m.group(1).replace("../", "");
            Resource imgRes = book.getResources().getByHref(src);
            if (imgRes != null) {
                String base64 = Base64.encodeToString(imgRes.getData(), Base64.NO_WRAP);
                String dataUri = "data:" + imgRes.getMediaType() + ";base64," + base64;
                m.appendReplacement(sb, m.group(0).replace(m.group(1), dataUri));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public void marcarFinalizado() {
        LibrosDeUsuario ldu = libroUsuario.getValue();
        if (ldu != null && ldu.getFechaFin() == null) {
            ldu.setFechaFin(new Date());
            ldu.setCapitulo(getTotalChapters() - 1);
            ldu.setScroll(1f);
            repository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
                @Override public void onResponse(Call<LibrosDeUsuario> c, Response<LibrosDeUsuario> r) { isFinished.postValue(true); }
                @Override public void onFailure(Call<LibrosDeUsuario> c, Throwable t) { isFinished.postValue(true); }
            });
        } else { isFinished.postValue(true); }
    }

    public void guardarEstadoActual(float scroll) {
        LibrosDeUsuario ldu = libroUsuario.getValue();
        if (ldu != null && ldu.getFechaFin() == null) {
            ldu.setCapitulo(currentChapter);
            ldu.setScroll(scroll);
            repository.actualizarProgreso(ldu, new Callback<LibrosDeUsuario>() {
                @Override public void onResponse(Call<LibrosDeUsuario> c, Response<LibrosDeUsuario> r) {}
                @Override public void onFailure(Call<LibrosDeUsuario> c, Throwable t) {}
            });
        }
    }
}