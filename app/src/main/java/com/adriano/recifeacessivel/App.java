package com.adriano.recifeacessivel;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    public static List<Obstaculo> listaObstaculos;

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Obstaculo.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseUser.enableAutomaticUser();

        try {
            listaObstaculos = ParseQuery.getQuery(Obstaculo.class).find();
            ParseObject.pinAllInBackground(listaObstaculos);
        } catch (Exception e){ }
    }
}
