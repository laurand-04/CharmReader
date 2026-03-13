package com.tfg.charmreader.viewmodel.publ.fragmentView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.model.BookEn;
import com.tfg.charmreader.data.model.CatalogoLectura;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_APICatalogo;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiBook;
import com.tfg.charmreader.data.repository.GrupoRepository;
import com.tfg.charmreader.data.repository.priv.proximamente.BookRepository;
import com.tfg.charmreader.data.repository.publ.InfoGrupoRepository;
import com.tfg.charmreader.viewmodel.publ.misGrupos.creados.ManejoGrupoViewModel;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaLibrosManejoFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<BookEn>> libros = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    private final GrupoRepository grupoRepository = new GrupoRepository();
    private final InfoGrupoRepository ingoGrupoRepository = new InfoGrupoRepository();
    private final BookRepository bookRepository = new BookRepository();

    public LiveData<List<BookEn>> getLibros() { return libros; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    public void cargarDatos(int idGrupo, int tipoLista) {
        isLoading.setValue(true);
        if(tipoLista == 0){ //Propuesta
            ingoGrupoRepository.obtenerLibroPropuestas(idGrupo, new Callback<List<BookEn>>() {
                @Override
                public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        libros.setValue(response.body());
                        isLoading.setValue(false);
                        //filtrarYCargarDetalles(response.body(), tipoLista);
                    } else {
                        libros.setValue(new ArrayList<>());
                        isLoading.setValue(false);
                    }
                }
                @Override
                public void onFailure(Call<List<BookEn>> call, Throwable t) {
                    libros.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                }
            });
        }
        else if (tipoLista == 1){ //Actual
            ingoGrupoRepository.obtenerLibroActual(idGrupo, new Callback<BookEn>() {
                @Override
                public void onResponse(Call<BookEn> call, Response<BookEn> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<BookEn> librosCargados = new ArrayList<>();
                        librosCargados.add(response.body());
                        libros.setValue(librosCargados);
                        isLoading.setValue(false);
                        //filtrarYCargarDetalles(response.body(), tipoLista);
                    } else {
                        libros.setValue(new ArrayList<>());
                        isLoading.setValue(false);
                    }
                }
                @Override
                public void onFailure(Call<BookEn> call, Throwable t) {
                    libros.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                }
            });
        }
        else{ //Historial
            ingoGrupoRepository.obtenerHistorial(idGrupo, new Callback<List<BookEn>>() {
                @Override
                public void onResponse(Call<List<BookEn>> call, Response<List<BookEn>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        libros.setValue(response.body());
                        isLoading.setValue(false);
                        //filtrarYCargarDetalles(response.body(), tipoLista);
                    } else {
                        libros.setValue(new ArrayList<>());
                        isLoading.setValue(false);
                    }
                }
                @Override
                public void onFailure(Call<List<BookEn>> call, Throwable t) {
                    libros.setValue(new ArrayList<>());
                    isLoading.setValue(false);
                }
            });
        }
    }

    private void filtrarYCargarDetalles(List<CatalogoLectura> catalogo, int tipoLista) {
        List<Integer> idsAFiltrar = new ArrayList<>();
        for (CatalogoLectura item : catalogo) {
            boolean cumple = false;
            if (tipoLista == 0 && item.getEstado() == CatalogoLectura.EstadoLectura.PROPUESTO) cumple = true;
            else if (tipoLista == 1 && item.getEstado() == CatalogoLectura.EstadoLectura.ACTUAL) cumple = true;
            else if (tipoLista == 2 && item.getEstado() == CatalogoLectura.EstadoLectura.FINALIZADO) cumple = true;
            if (cumple) idsAFiltrar.add(item.getIdBook());
        }

        if (idsAFiltrar.isEmpty()) {
            libros.setValue(new ArrayList<>());
            isLoading.setValue(false);
            return;
        }

        List<BookEn> librosCargados = new ArrayList<>();
        final int[] completados = {0};

        for (Integer idBook : idsAFiltrar) {
            bookRepository.obtenerBookPorId(idBook, new Callback<BookEn>() {
                @Override
                public void onResponse(Call<BookEn> call, Response<BookEn> r) {
                    completados[0]++;
                    if (r.isSuccessful() && r.body() != null) librosCargados.add(r.body());
                    if (completados[0] == idsAFiltrar.size()) {
                        libros.setValue(librosCargados);
                        isLoading.setValue(false);
                    }
                }
                @Override
                public void onFailure(Call<BookEn> call, Throwable t) {
                    completados[0]++;
                    if (completados[0] == idsAFiltrar.size()) {
                        libros.setValue(librosCargados);
                        isLoading.setValue(false);
                    }
                }
            });
        }
    }

    public void eliminarPropuesta(int idGrupo, int idLibro, ManejoGrupoViewModel sharedViewModel) {
        grupoRepository.eliminarPropuesta(idGrupo, idLibro, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    toastMessage.setValue("Propuesta eliminada");
                    sharedViewModel.consumeRefresh(); // Notifica al mediador global
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                toastMessage.setValue("Error al eliminar");
            }
        });
    }
}