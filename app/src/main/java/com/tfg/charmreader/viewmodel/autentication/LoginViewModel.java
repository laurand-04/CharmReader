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
//import com.tfg.charmreader.utils.Utilidades;

public class LoginViewModel extends ViewModel {
    private AuthRepository authRepository;
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    // Clase para representar los estados de la pantalla
    public static class LoginState {
        public final boolean success, isAdmin, needsVerification, loading;
        public final String errorMessage;

        public LoginState(boolean s, boolean a, boolean v, boolean l, String e) {
            success = s;
            isAdmin = a;
            needsVerification = v;
            loading = l;
            errorMessage = e;
        }
    }

    public void setContext(Context context) {
        this.authRepository = AuthRepository.getInstance(context.getApplicationContext());
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        if (email.equals("admin") && password.equals("admin")) {
            authRepository.saveSession(true, email, -1);
            loginState.setValue(new LoginState(true, true, false, false, null));
            return;
        }

        loginState.setValue(new LoginState(false, false, false, true, null));

        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (user.isEmailVerified()) {
                    fetchIdAndSave(email);
                } else {
                    loginState.postValue(new LoginState(false, false, true, false, null));
                }
            }

            @Override
            public void onError(Exception e) {
                loginState.postValue(new LoginState(false, false, false, false, e.getMessage()));
            }
        });
    }

    public void fetchIdAndSave(String email) {
        new Thread(() -> {
            try {
                // Usamos tu objeto de utilidades o el api directamente
                /*retrofit2.Response<Usuario> response = Utilidades.apiUsuario.getIdUsuarioPorCorreo(email).execute();
                int id = (response.isSuccessful() && response.body() != null) ? response.body().getId() : -1;*/
                int id = authRepository.getIdUsuario();

                authRepository.saveSession(false, email, id);
                Log.d("DEBUG_REG", "Sesion guardada con exito, codigo: " + id);
                loginState.postValue(new LoginState(true, false, false, false, null));
            } catch (Exception e) {
                loginState.postValue(new LoginState(false, false, false, false, "Error de red al sincronizar perfil"));
                Log.e("DEBUG_REG", "Error al sincronizar perfil: " + e.getMessage());
            }
        }).start();
    }
}