package com.example.safeher20.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarias")
public class Usuaria {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull private String dni;
    @NonNull private String nombre;
    @NonNull private String apellido;
    @NonNull private String genero;
    @NonNull private String pais;
    @NonNull private String ciudad;
    @NonNull private String direccion;
    private String portal;
    private String piso;
    private String puerta;
    @NonNull private String telefono;
    @NonNull private String codigoPostal;
    private String fotoDniPath;
    @NonNull private String email;
    @NonNull private String contrasena; // ⚠️ En producción, usar hash

    // Constructor vacío requerido por Room
    public Usuaria() {}

    // Constructor completo
    public Usuaria(@NonNull String dni, @NonNull String nombre, @NonNull String apellido,
                   @NonNull String genero, @NonNull String pais, @NonNull String ciudad,
                   @NonNull String direccion, String portal, String piso, String puerta,
                   @NonNull String telefono, @NonNull String codigoPostal, String fotoDniPath,
                   @NonNull String email, @NonNull String contrasena) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.genero = genero;
        this.pais = pais;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.portal = portal;
        this.piso = piso;
        this.puerta = puerta;
        this.telefono = telefono;
        this.codigoPostal = codigoPostal;
        this.fotoDniPath = fotoDniPath;
        this.email = email;
        this.contrasena = contrasena;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPortal() { return portal; }
    public void setPortal(String portal) { this.portal = portal; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getFotoDniPath() { return fotoDniPath; }
    public void setFotoDniPath(String fotoDniPath) { this.fotoDniPath = fotoDniPath; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    // Alias opcional para getPassword (si prefieres llamarlo así desde otras clases)
    public String getPassword() {
        return contrasena;
    }
}
