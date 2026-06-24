package com.example.check_in;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteLocal;
    private Spinner spinnerCategoria;
    private TextView textLatitude, textLongitude;
    private Button btnCheckin;

    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private double latitude = 0, longitude = 0;
    private boolean posicaoObtida = false;

    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autoCompleteLocal = findViewById(R.id.autoCompleteLocal);
        spinnerCategoria   = findViewById(R.id.spinnerCategoria);
        textLatitude       = findViewById(R.id.textLatitude);
        textLongitude      = findViewById(R.id.textLongitude);
        btnCheckin         = findViewById(R.id.btnCheckin);

        dbHelper = new DatabaseHelper(this);

        carregarCategorias();
        carregarLocaisAutoComplete();
        iniciarLocalizacao();

        btnCheckin.setOnClickListener(v -> realizarCheckin());
    }

    // ── Spinner de categorias ──────────────────────────────────────────────────
    private void carregarCategorias() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nome FROM Categoria ORDER BY idCategoria ASC", null);

        List<String> categorias = new ArrayList<>();
        while (cursor.moveToNext()) {
            categorias.add(cursor.getString(0));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    // ── AutoComplete com locais já visitados ───────────────────────────────────
    private void carregarLocaisAutoComplete() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT Local FROM Checkin ORDER BY Local ASC", null);

        List<String> locais = new ArrayList<>();
        while (cursor.moveToNext()) {
            locais.add(cursor.getString(0));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, locais);
        autoCompleteLocal.setAdapter(adapter);
    }

    // ── Localização ────────────────────────────────────────────────────────────
    private void iniciarLocalizacao() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    latitude       = loc.getLatitude();
                    longitude      = loc.getLongitude();
                    posicaoObtida  = true;
                    textLatitude.setText("Latitude:  " + latitude);
                    textLongitude.setText("Longitude: " + longitude);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            solicitarAtualizacoesLocalizacao();
        }
    }

    private void solicitarAtualizacoesLocalizacao() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000).build();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            solicitarAtualizacoesLocalizacao();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            solicitarAtualizacoesLocalizacao();
        }
    }

    // ── Lógica do Check-in ─────────────────────────────────────────────────────
    private void realizarCheckin() {
        String nomeLocal = autoCompleteLocal.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem() != null
                ? spinnerCategoria.getSelectedItem().toString() : "";

        if (nomeLocal.isEmpty()) {
            Toast.makeText(this, "Informe o nome do local.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categoria.isEmpty()) {
            Toast.makeText(this, "Selecione uma categoria.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!posicaoObtida) {
            Toast.makeText(this, "Aguarde a obtenção da posição GPS.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Verifica se o local já existe
        Cursor cursor = db.rawQuery(
                "SELECT Local FROM Checkin WHERE Local = ?",
                new String[]{nomeLocal});
        boolean jaExiste = cursor.moveToFirst();
        cursor.close();

        if (jaExiste) {
            // UPDATE — incrementa visitas, ignora categoria e posição
            db.execSQL("UPDATE Checkin SET qtdVisitas = qtdVisitas + 1 WHERE Local = ?",
                    new String[]{nomeLocal});
            Toast.makeText(this, "Check-in atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            // INSERT — busca o id da categoria selecionada
            Cursor catCursor = db.rawQuery(
                    "SELECT idCategoria FROM Categoria WHERE nome = ?",
                    new String[]{categoria});
            int idCat = 1;
            if (catCursor.moveToFirst()) {
                idCat = catCursor.getInt(0);
            }
            catCursor.close();

            db.execSQL("INSERT INTO Checkin (Local, qtdVisitas, cat, latitude, longitude) " +
                            "VALUES (?, 1, ?, ?, ?)",
                    new Object[]{nomeLocal, idCat,
                            String.valueOf(latitude), String.valueOf(longitude)});
            Toast.makeText(this, "Check-in realizado!", Toast.LENGTH_SHORT).show();
        }

        // Fecha e reabre a tela para sensação de atualização
        finish();
        startActivity(getIntent());
    }

    // ── Menu ───────────────────────────────────────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuMapa) {
            if (!posicaoObtida) {
                Toast.makeText(this,
                        "Aguarde a obtenção da posição GPS para abrir o mapa.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, MapaActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
            return true;

        } else if (id == R.id.menuGestao) {
            startActivity(new Intent(this, GestaoActivity.class));
            return true;

        } else if (id == R.id.menuRelatorio) {
            startActivity(new Intent(this, RelatorioActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}