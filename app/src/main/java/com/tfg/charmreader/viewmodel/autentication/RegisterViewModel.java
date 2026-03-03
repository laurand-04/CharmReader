package com.tfg.charmreader.viewmodel.autentication;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.data.model.Usuario;
import com.tfg.charmreader.data.repository.autentication.AuthRepository;
import com.tfg.charmreader.data.repository.UserRepository;

public class RegisterViewModel extends ViewModel {
    private AuthRepository repository;
    private final MutableLiveData<RegisterState> state = new MutableLiveData<>();
    private FirebaseUser firebaseUser = null;

    public RegisterViewModel() {
        this.repository = null;
    }

    public void setContext(Context context) {
        this.repository = AuthRepository.getInstance(context.getApplicationContext());
    }


    public LiveData<RegisterState> getState() { return state; }

    public void register(String email, String password) {
        state.setValue(new RegisterState(true, false, null));

        repository.registrar(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                firebaseUser = user;
                user.sendEmailVerification();
                repository.guardarUsuarioEnSQL(email, new UserRepository.RepositoryCallback<Usuario>() {
                    @Override
                    public void onComplete(Usuario result) {
                        state.postValue(new RegisterState(false, true, null));
                    }
                    @Override
                    public void onError(String message) {
                        state.postValue(new RegisterState(false, false, message));
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                state.postValue(new RegisterState(false, false, e.getMessage()));
            }
        });
    }

    public boolean isVerified(){
        FirebaseAuth.getInstance().getCurrentUser().reload();
        return FirebaseAuth.getInstance().getCurrentUser().isEmailVerified();
    }

    public void enviarCorreoBienvenida(){
            repository.enviarCorreoBienvenida(firebaseUser.getEmail());
    }

    public static class RegisterState {
        public final boolean loading, success;
        public final String error;
        public RegisterState(boolean l, boolean s, String e) { loading = l; success = s; error = e; }
    }
}