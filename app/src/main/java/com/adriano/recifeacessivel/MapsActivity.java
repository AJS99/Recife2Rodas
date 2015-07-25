package com.adriano.recifeacessivel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener{

    private static final int REQUEST_CAMERA = 0;
    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private FloatingActionButton btNovoObstaculo;
    private ImageView foto;
    private FloatingActionButton tirarFoto;
    private Uri photoUri;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    LatLng latLng;
    String[] obstaculos = new String[]{"Buracos","Estacionamento Indevido","Árvores","Comércio Ambulante","Lixos Dispostos Irregularmente","Outros"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btNovoObstaculo = (FloatingActionButton) findViewById(R.id.btNovoObstaculo);
        btNovoObstaculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(MapsActivity.this);
                View form = getLayoutInflater().inflate(R.layout.fragment_form, null, false);
                final Spinner obstaculosView = (Spinner) form.findViewById(R.id.obstaculo);
                final EditText descricaoView = (EditText) form.findViewById(R.id.descricao);
                foto = (ImageView) form.findViewById(R.id.foto);
                tirarFoto = (FloatingActionButton) form.findViewById(R.id.btTirarFoto);

                tirarFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tirarFoto();
                    }
                });

                obstaculosView.setAdapter(new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_spinner_dropdown_item, obstaculos));
                descricaoView.setHint("Digite a Descrição");

                dialogBuilder
                        .withTitle("Obstáculo Encontrado")
                        .withMessage(null)
                        .withButton2Text("Salvar")
                        .withButton1Text("Cancelar")
                        .setCustomView(form, MapsActivity.this)
                        .withDialogColor(getResources().getColor(R.color.primary))
                        .setButton2Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ParseFile file = new ParseFile(getBitmapByte(photoUri.getPath()));

                                Obstaculo obstaculo = new Obstaculo();
                                obstaculo.setCategoria(obstaculosView.getSelectedItem().toString());
                                obstaculo.setDescricao(descricaoView.getText().toString());
                                obstaculo.setLatitude(latLng.latitude);
                                obstaculo.setLongitude(latLng.longitude);
                                obstaculo.setFotoURI(file);
                                obstaculo.saveInBackground();

                                App.listaObstaculos.add(obstaculo);

                                addMarker(obstaculo);
                                dialogBuilder.dismiss();
                            }
                        })
                        .setButton1Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogBuilder.dismiss();
                            }
                        })
                        .show();
            }
        });

        getLayoutInflater().inflate(R.layout.fragment_form, null, false).findViewById(R.id.btTirarFoto);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        map.moveCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            photoUri = data.getData();
            tirarFoto.setVisibility(View.GONE);
            foto.setVisibility(View.VISIBLE);
            Picasso.with(this).load(photoUri).into(foto);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpMapIfNeeded() {
        SupportMapFragment frag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        frag.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                map.setMyLocationEnabled(true);

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, MapsActivity.this);

                for (Obstaculo o : App.listaObstaculos) {
                    addMarker(o, o.getLatitude(), o.getLongitude());
                }
            }
        });
    }

    private void addMarker(Obstaculo obstaculo, double latitude, double longitude){
        int drawableId = 0;
        switch (obstaculo.getCategoria()){
            case "Buracos":
                drawableId =(int) R.drawable.ic_action_buraco;
                break;
            case "Estacionamento Indevido":
                drawableId =(int) R.drawable.ic_maps_directions_car;
                break;
            case "Árvores":
                drawableId =(int) R.drawable.ic_image_nature;
                break;
            case "Comércio Ambulante":
                drawableId =(int) R.drawable.ic_action_shopping_cart;
                break;
            case "Lixos Dispostos Irregularmente":
                drawableId =(int) R.drawable.ic_lixeira;
                break;
            case "Outros":
                drawableId =(int) R.drawable.ic_outros;
                break;
        }

        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), drawableId)))
                .position(new LatLng(latitude, longitude)));

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(MapsActivity.this);
                Obstaculo obstaculo = getObstaculo(marker.getPosition().latitude, marker.getPosition().longitude);
                View form = getLayoutInflater().inflate(R.layout.detalhe, null, false);
                TextView descricaoDetalhe = (TextView) form.findViewById(R.id.descricao);
                ImageView photoDetalhe = (ImageView) form.findViewById(R.id.foto);

                if(obstaculo != null){

                    descricaoDetalhe.setText(obstaculo.getDescricao());
                    if(obstaculo.getFotoURI() != null){
                        Picasso.with(MapsActivity.this).load(Uri.parse(obstaculo.getFotoURI().getUrl())).into(photoDetalhe);
                        photoDetalhe.setVisibility(View.VISIBLE);
                    }


                    dialogBuilder
                            .withTitle(obstaculo.getCategoria())
                            .withMessage(null)
                            .setCustomView(form, MapsActivity.this)
                            .withButton1Text("O.K")
                            .withDialogColor(getResources().getColor(R.color.primary))
                            .setButton1Click(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogBuilder.dismiss();
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(MapsActivity.this, R.string.msgNenhumObstaculoEncontrado, Toast.LENGTH_SHORT);
                }


                return false;
            }
        });
    }

    private void addMarker(Obstaculo obstaculo) {
        addMarker(obstaculo, latLng.latitude, latLng.longitude);
    }

    private Obstaculo getObstaculo(double latitude, double longitude){

        for(Obstaculo obstaculo: App.listaObstaculos) {
            if (obstaculo.getLatitude() == latitude && obstaculo.getLongitude() == longitude) {
                return obstaculo;
            }
        }
        return null;
    }

    private void tirarFoto(){
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    private byte[] getBitmapByte(String uri){
        File imgFile = new  File(uri);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return byteArray;
        }
        return null;
    }

}
