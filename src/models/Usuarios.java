
package models;

public class Usuarios {
    public int usuarioId;
    public String nombreUsuario;
    public String pass;
    public String respuestaPreguntaConfianza;
    public int statusConexion;

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

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getRespuestaPreguntaConfianza() {
        return respuestaPreguntaConfianza;
    }

    public void setRespuestaPreguntaConfianza(String respuestaPreguntaConfianza) {
        this.respuestaPreguntaConfianza = respuestaPreguntaConfianza;
    }

    public int getStatusConexion() {
        return statusConexion;
    }

    public void setStatusConexion(int statusConexion) {
        this.statusConexion = statusConexion;
    }
    
}

