package com.tfg.charmreader.data.repository.publ;

import com.tfg.charmreader.data.model.Votacion;
import com.tfg.charmreader.data.network.API.API;
import com.tfg.charmreader.data.network.interfacesAPI.I_ApiVotacion;

import java.util.Map;

import retrofit2.Callback;

public class VotacionRepository {
    private final I_ApiVotacion apiVotacion = API.getInstancia().create(I_ApiVotacion.class);

    public void alternarVoto(Votacion votacion, Callback<Map<String, String>> cb) {
        apiVotacion.alternarVoto(votacion).enqueue(cb);
    }


}
