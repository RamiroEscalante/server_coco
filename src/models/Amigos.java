
package models;

public class Amigos {
    public int amigosId;
    public int usuarioDuenoId;
    public int usuarioId;
    public String nombreUsuario;
    public String nombreUsuarioDueno;
    public int conexion;

    public int getConexion() {
        return conexion;
    }

    public void setConexion(int conexion) {
        this.conexion = conexion;
    }

    public int getAmigosId() {
        return amigosId;
    }

    public void setAmigosId(int amigosId) {
        this.amigosId = amigosId;
    }

    public int getUsuarioDuenoId() {
        return usuarioDuenoId;
    }

    public void setUsuarioDuenoId(int usuarioDuenoId) {
        this.usuarioDuenoId = usuarioDuenoId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreUsuarioDueno() {
        return nombreUsuarioDueno;
    }

    public void setNombreUsuarioDueno(String nombreUsuarioDueno) {
        this.nombreUsuarioDueno = nombreUsuarioDueno;
    }
    
}
