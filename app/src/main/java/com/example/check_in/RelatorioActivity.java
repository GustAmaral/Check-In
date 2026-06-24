package com.example.check_in;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RelatorioActivity extends AppCompatActivity {

    private LinearLayout layoutConteudo;
    private LinearLayout layoutVisitas;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);

        layoutConteudo = findViewById(R.id.layoutConteudo);
        layoutVisitas  = findViewById(R.id.layoutVisitas);
        dbHelper       = new DatabaseHelper(this);

        carregarRelatorio();
    }

    // ── Carrega lista ordenada por quantidade de visitas ──────────────────────
    private void carregarRelatorio() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT Local, qtdVisitas FROM Checkin " +
                        "ORDER BY qtdVisitas DESC", null);

        while (cursor.moveToNext()) {
            String nomeLocal = cursor.getString(0);
            int    visitas   = cursor.getInt(1);

            // TextView com o nome do local
            TextView tvLocal = new TextView(this);
            tvLocal.setText(nomeLocal);
            tvLocal.setTextSize(16f);
            tvLocal.setPadding(0, 12, 0, 12);
            layoutConteudo.addView(tvLocal);

            // TextView com a quantidade de visitas
            TextView tvVisitas = new TextView(this);
            tvVisitas.setText(String.valueOf(visitas));
            tvVisitas.setTextSize(16f);
            tvVisitas.setPadding(16, 12, 0, 12);
            layoutVisitas.addView(tvVisitas);
        }
        cursor.close();
    }

    // ── Menu ───────────────────────────────────────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_relatorio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuVoltar) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
