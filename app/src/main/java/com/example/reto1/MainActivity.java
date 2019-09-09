package com.example.reto1;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private Marker miUbicacion;
    private ArrayList<Marker> places;
    private ImageView plusImg;
    private String titleAux;
    private Marker markerAux;
    private LatLng latLngAux;
    private TextView infoTv;
    private boolean addMarkerEnable;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },11);
        places = new ArrayList<>();
        plusImg = findViewById(R.id.plus_img);
        infoTv = findViewById(R.id.info_tv);
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        addMarkerEnable = false;
        plusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoTv.setText("Selecciona el lugar");
                addMarkerEnable=true;
                map.setOnMapClickListener(MainActivity.this);
            }
        });
        LatLng icesi = new LatLng(3.341552, -76.529784);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(icesi, 15));
        miUbicacion = map.addMarker(new MarkerOptions().position(icesi).icon(BitmapDescriptorFactory.fromResource(R.drawable.ubication)).title("Mi ubicacion"));
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        miUbicacion.setPosition(pos);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
        infoTv.setText(getInfoMiUbicacion());
    }

    public String getInfoMiUbicacion() {
        String info = "";
        Marker min = null;
        if(addMarkerEnable) {
            return "Selecciona un lugar";
        }
        for (int i = 0; i < places.size(); i++) {
            min = places.get(i);
            if(getDistance(min)<getDistance(places.get(i))) {
                min = places.get(i);
            }
        }
        if(min!=null) {
            if(getDistance(min)<20) {
                info = "Estas en "+min.getTitle();
            }
            else {
                info = "El lugar mas cercano es "+min.getTitle();
            }
        } else {
            info = "Agrega tus lugares favoritos con el boton +";
        }
        return info;
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
    public void onMapClick(LatLng latLng) {
        latLngAux = latLng;
        map.setOnMapClickListener(null);
        addMarkerEnable=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Que lugar es?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                titleAux = input.getText().toString();
                markerAux = map.addMarker(new MarkerOptions().position(latLngAux).title(titleAux));
                places.add(markerAux);
                infoTv.setText(getInfoMiUbicacion());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle().equals("Mi ubicacion")) {
            try {
                List<Address> address  = geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude,1);
                marker.setSnippet(address.get(0).getAddressLine(0));
            } catch (IOException e) {

            }

        } else {
            DecimalFormat formato1 = new DecimalFormat("#.00");
            marker.setSnippet("Estas a "+formato1.format(getDistance(marker))+" metros");
        }
        return false;
    }

    public double getDistance (Marker marker) {
        double distance = Math.sqrt( Math.pow(marker.getPosition().latitude-miUbicacion.getPosition().latitude,2) + Math.pow(marker.getPosition().longitude-miUbicacion.getPosition().longitude,2) );
        distance = distance * 111.12 * 1000.0;
        return distance;
    }


}
