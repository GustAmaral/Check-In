package com.example.check_in;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        // Recebe latitude e longitude da MainActivity
        latitude  = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        dbHelper = new DatabaseHelper(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ── Mapa pronto ────────────────────────────────────────────────────────────
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Centraliza na posição atual do usuário
        LatLng posicaoAtual = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 15));

        carregarMarcadores();
    }

    // ── Marcadores de todos os check-ins ──────────────────────────────────────
    private void carregarMarcadores() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT c.Local, cat.nome, c.qtdVisitas, c.latitude, c.longitude " +
                        "FROM Checkin c " +
                        "INNER JOIN Categoria cat ON c.cat = cat.idCategoria", null);

        while (cursor.moveToNext()) {
            String local      = cursor.getString(0);
            String categoria  = cursor.getString(1);
            int    visitas    = cursor.getInt(2);
            double lat        = Double.parseDouble(cursor.getString(3));
            double lng        = Double.parseDouble(cursor.getString(4));

            LatLng posicao = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .title(local)
                    .snippet("Categoria: " + categoria + " Visitas: " + visitas));
        }
        cursor.close();
    }

    // ── Menu ───────────────────────────────────────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuVoltar) {
            finish();
            return true;

        } else if (id == R.id.menuGestao) {
            startActivity(new Intent(this, GestaoActivity.class));
            return true;

        } else if (id == R.id.menuRelatorio) {
            startActivity(new Intent(this, RelatorioActivity.class));
            return true;

        } else if (id == R.id.menuMapaNormal) {
            if (mMap != null) mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;

        } else if (id == R.id.menuMapaHibrido) {
            if (mMap != null) mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
