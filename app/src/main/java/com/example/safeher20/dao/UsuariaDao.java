package com.example.safeher20.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.safeher20.model.Usuaria;

import java.util.List;

@Dao
public interface UsuariaDao {
    @Insert
    void insertar(Usuaria usuaria);

    @Query("SELECT * FROM usuarias WHERE email = :email AND contrasena = :contrasena")
    Usuaria login(String email, String contrasena);

    @Query("SELECT * FROM usuarias WHERE dni = :dni LIMIT 1")
    Usuaria buscarPorDni(String dni);

    @Query("SELECT * FROM usuarias WHERE email = :email LIMIT 1")
    Usuaria buscarPorEmail(String email);

    @Query("SELECT * FROM usuarias")
    List<Usuaria> getTodas();
    @Query("SELECT * FROM usuarias ORDER BY id DESC LIMIT 1")
    Usuaria obtenerUltimaUsuaria();



}
