package com.tfg.charmreader.viewmodel.autentication;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;

public class RecuperacionViewModel extends ViewModel {
    private AuthRepository repository;
    private final MutableLiveData<RecuperacionState> state = new MutableLiveData<>();

    public RecuperacionViewModel() {
        this.repository = null;
    }

    public void setContext(Context context) {
        this.repository = AuthRepository.getInstance(context.getApplicationContext());
    }


    public LiveData<RecuperacionState> getState() { return state; }

    public void enviarCorreoRecuperacion(String email) {
        state.setValue(new RecuperacionState(true, false, null));

        repository.resetearPassword(email, new AuthRepository.RepositoryCallback<Void>() {
            @Override
            public void onComplete(Void result) {
                state.postValue(new RecuperacionState(false, true, null));
            }

            @Override
            public void onError(String message) {
                state.postValue(new RecuperacionState(false, false, message));
            }
        });
    }

    public static class RecuperacionState {
        public final boolean loading, success;
        public final String error;
        public RecuperacionState(boolean l, boolean s, String e) {
            loading = l; success = s; error = e;
        }
    }
}