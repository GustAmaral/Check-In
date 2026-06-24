package com.example.check_in;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GestaoActivity extends AppCompatActivity {

    private LinearLayout layoutConteudo;
    private LinearLayout layoutDeletar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestao);

        layoutConteudo = findViewById(R.id.layoutConteudo);
        layoutDeletar  = findViewById(R.id.layoutDeletar);
        dbHelper       = new DatabaseHelper(this);

        carregarLista();
    }

    // ── Carrega lista de check-ins ─────────────────────────────────────────────
    private void carregarLista() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT Local FROM Checkin ORDER BY Local ASC", null);

        while (cursor.moveToNext()) {
            final String nomeLocal = cursor.getString(0);

            // TextView com o nome do local
            TextView tv = new TextView(this);
            tv.setText(nomeLocal);
            tv.setTextSize(16f);
            tv.setPadding(0, 12, 0, 12);
            layoutConteudo.addView(tv);

            // ImageButton para deletar
            ImageButton btn = new ImageButton(this);
            btn.setImageResource(android.R.drawable.ic_delete);
            btn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btn.setTag(nomeLocal);

            // Remove o tamanho mínimo padrão para alinhar com o TextView
            btn.setMinimumHeight(0);
            btn.setPadding(8, 12, 8, 12);

            // Força a mesma altura que o TextView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btn.setLayoutParams(params);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String local = (String) v.getTag();
                    confirmarExclusao(local);
                }
            });
            layoutDeletar.addView(btn);
        }
        cursor.close();
    }

    // ── Confirmação de exclusão ────────────────────────────────────────────────
    private void confirmarExclusao(final String nomeLocal) {
        new AlertDialog.Builder(this)
                .setTitle("Exclusão")
                .setMessage("Tem certeza que deseja excluir " + nomeLocal + "?")
                .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletarLocal(nomeLocal);
                    }
                })
                .setNegativeButton("NÃO", null)
                .show();
    }

    // ── Deleta o registro e recarrega a tela ──────────────────────────────────
    private void deletarLocal(String nomeLocal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM Checkin WHERE Local = ?", new String[]{nomeLocal});

        // Fecha e reabre a tela para sensação de atualização
        finish();
        startActivity(getIntent());
    }

    // ── Menu ───────────────────────────────────────────────────────────────────
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gestao, menu);
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
