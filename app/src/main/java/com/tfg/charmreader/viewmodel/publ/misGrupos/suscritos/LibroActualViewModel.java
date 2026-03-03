package com.tfg.charmreader.viewmodel.publ.misGrupos.suscritos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.BookEn;

public class LibroActualViewModel extends ViewModel {

    private final MutableLiveData<BookEn> libro = new MutableLiveData<>();
    private final MutableLiveData<String> coverUrl = new MutableLiveData<>();

    public LiveData<BookEn> getLibro() { return libro; }
    public LiveData<String> getCoverUrl() { return coverUrl; }

    public void setLibro(BookEn book) {
        libro.setValue(book);
        if (book != null && book.getCoverId() != null && !book.getCoverId().isEmpty() && !book.getCoverId().equals("null")) {
            // Lógica de negocio: construir URL de alta calidad (-L)
            coverUrl.setValue("https://covers.openlibrary.org/b/id/" + book.getCoverId() + "-L.jpg");
        } else {
            coverUrl.setValue(null);
        }
    }
}