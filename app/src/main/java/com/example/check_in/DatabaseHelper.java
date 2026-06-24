package com.example.check_in;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "checkin.db";
    private static final int VERSAO = 1;

    public DatabaseHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Categoria (" +
                "idCategoria INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL)");

        db.execSQL("CREATE TABLE Checkin (" +
                "Local TEXT PRIMARY KEY, " +
                "qtdVisitas INTEGER NOT NULL, " +
                "cat INTEGER NOT NULL, " +
                "latitude TEXT NOT NULL, " +
                "longitude TEXT NOT NULL, " +
                "FOREIGN KEY (cat) REFERENCES Categoria(idCategoria))");

        // Inserir categorias iniciais
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Restaurante')");
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Bar')");
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Cinema')");
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Universidade')");
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Estádio')");
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Parque')");
        db.execSQL("INSERT INTO Categoria (nome) VALUES ('Outros')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Checkin");
        db.execSQL("DROP TABLE IF EXISTS Categoria");
        onCreate(db);
    }
}
