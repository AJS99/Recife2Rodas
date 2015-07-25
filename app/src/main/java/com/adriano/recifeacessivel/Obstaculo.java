package com.adriano.recifeacessivel;

import android.graphics.Path;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Obstaculo")
public class Obstaculo extends ParseObject {
    public String getCategoria() {
        return getString("categoria");
    }

    public void setCategoria(String categoria) {
        put("categoria", categoria);
    }

    public String getDescricao() {
        return getString("descricao");
    }

    public void setDescricao(String descricao) {
        put("descricao", descricao);
    }

    public ParseFile getFotoURI() {
        return getParseFile("fotoURI");
}

    public void setFotoURI(ParseFile fotoURI) {
        put("fotoURI", fotoURI);
    }

    public double getLatitude() {
        return getDouble("latitude");
    }

    public void setLatitude(double latitude) {
        put("latitude", latitude);
    }

    public double getLongitude() {
        return getDouble("longitude");
    }

    public void setLongitude(double longitude) {
        put("longitude", longitude);
    }
}
