
package Controllers;

import models.IndividualChatModel;
import models.Messages;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import bd.BD;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import de modelados
import models.Amigos;
import models.GroupMessages;
import models.Usuarios;

public class Queries {
    
    public String BuscarCredenciales(String usuarioId, String psw) throws SQLException {
        PreparedStatement pstmt;
        ResultSet rs = null;
        BD bd = new BD();
        try {
            String sql = "SELECT * FROM usuarios WHERE NombreUsuario = ? AND Pass = ?";
            pstmt = bd.getCon().prepareStatement(sql);
            pstmt.setString(1, usuarioId);
            pstmt.setString(2, psw);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Credenciales válidas");
            } else {
                System.out.println("Credenciales no válidas");
                bd.closeConnection();
                return "0";
            }
        } catch (SQLException e) {
        }
        int usuarioID = rs.getInt("UsuarioId");
        String userID = "0";
        userID = String.valueOf(usuarioID);
        bd.closeConnection();
        return userID;
    }
    public String selectNameByUserId(int usuarioId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        String nombre = null;
        ResultSet r;
        try
        {
            //sql = "SELECT NombreUsuario FROM usuarios WHERE UsuarioId = " + usuarioId; 
            sql = bd.getCon().prepareStatement("SELECT NombreUsuario FROM usuarios WHERE UsuarioId=?");
            sql.setInt(1,usuarioId);
            r = sql.executeQuery();
            if(r.next())
            {
                nombre = r.getString("NombreUsuario");
            }
            return nombre;
        } catch (SQLException ex) {
            
        }
        return null;
    }
    public int EncontrarUsuarios(String nombreUsuario) {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet res;
        int userId = -1;
        try {
            sql = bd.getCon().prepareStatement("SELECT UsuarioId FROM usuarios WHERE NombreUsuario = ?");
            sql.setString(1, nombreUsuario);
            res = sql.executeQuery();

            if (res.next()) { // Verificar si hay al menos una fila en el conjunto de resultados
                userId = res.getInt("UsuarioId");
                System.out.println(userId + " encontrado.");
            } else {
                System.out.println("No se encontró ningún usuario con el nombre: " + nombreUsuario);
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return userId;
    }
    
    public int GetChatId(int chatterId1, int chatterId2)
    {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet res;
        int chatId = -1;
        try {
            sql = bd.getCon().prepareStatement("SELECT ConversacionId FROM chats WHERE (Usuario1Id = ? AND Usuario2Id= ?) OR (Usuario1Id = ? AND Usuario2Id= ?)");
            sql.setInt(1, chatterId1);
            sql.setInt(2, chatterId2);
            sql.setInt(3, chatterId2);
            sql.setInt(4, chatterId1);
            res = sql.executeQuery();
            
            if (res.next()) {
                chatId = res.getInt("ConversacionId");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            bd.closeConnection();
        }
        bd.closeConnection();
        return chatId;
    }
    
    public boolean CreateChat(int chatterId1, int chatterId2)
    {
         BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("INSERT INTO chats (Usuario1Id, Usuario2Id) VALUES (?, ?)");
            sql.setInt(1, chatterId1);
            sql.setInt(2, chatterId2);
            int comprobar = sql.executeUpdate();
            
            bd.closeConnection();
           
            return comprobar > 0;
        } catch (SQLException ex) {
            
            return false; 
        } finally
        {
            bd.closeConnection();
        }
    }
    
    public boolean SendMessageToBD(String Message, int chatId, int userId)
    {
        //System.out.println("ALAV");
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("INSERT INTO mensajes (contenido, fecha, chat_id, UsuarioId) VALUES (?, (SELECT CURRENT_TIMESTAMP), ?, ?)");
            sql.setString(1, Message);
            sql.setInt(2, chatId);
            sql.setInt(3, userId);
            System.out.println("Antes " + sql);
            int comprobar = sql.executeUpdate();
            System.out.println(sql);
            bd.closeConnection();
           
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false; 
        } finally
        {
            bd.closeConnection();
        }
    }
    
    public boolean SendMessageGroupToBD(String Message, int groupId, int userId)
    {
        //System.out.println("ALAV");
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("INSERT INTO mensajes_grupales (contenido_mensaje, fecha_hora, UsuarioId, GrupoId) VALUES (?, (SELECT CURRENT_TIMESTAMP), ?, ?)");
            sql.setString(1, Message);
            sql.setInt(2, userId);
            sql.setInt(3, groupId);
            //System.out.println("Antes " + sql);
            int comprobar = sql.executeUpdate();
            System.out.println(sql);
            bd.closeConnection();
           
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false; 
        } finally
        {
            bd.closeConnection();
        }
    }
    
// QUERIES DE AMIGOS -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public ArrayList<Amigos> selectMisAmigosUsuarios(int usuarioId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        ArrayList<Amigos> amigos = new ArrayList<>();
        ResultSet r;
        try
        {
            sql = bd.getCon().prepareStatement("SELECT u.NombreUsuario, a.UsuarioId, a.amigosId, u.StatusConexion FROM usuarios u INNER JOIN listaamigos a ON u.UsuarioId=a.UsuarioId WHERE a.UsuarioDuenoId=?");
            
            sql.setInt(1,usuarioId);
            
            r = sql.executeQuery();
            while(r.next())
            {
                Amigos x = new Amigos();
                x.nombreUsuario = r.getString("NombreUsuario");
                x.usuarioId = r.getInt("UsuarioId");
                x.amigosId = r.getInt("amigosId");
                x.conexion = r.getInt("StatusConexion");
                amigos.add(x);
            }
            
            amigos = selectMisAmigosDuenos(amigos, usuarioId);
            
            bd.closeConnection();
            return amigos;
        } catch (SQLException ex) {
            Logger.getLogger(Queries.class.getName()).log(Level.SEVERE, null, ex);
        }
        bd.closeConnection();
        return null;
    }
    public ArrayList<Amigos> selectMisAmigosDuenos(ArrayList<Amigos> amigos, int usuarioId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet r;
        try
        {
            sql = bd.getCon().prepareStatement("SELECT u.NombreUsuario, a.UsuarioDuenoId, a.amigosId, u.StatusConexion FROM usuarios u INNER JOIN listaamigos a ON u.UsuarioId=a.UsuarioDuenoId WHERE a.UsuarioId=?");
            
            sql.setInt(1,usuarioId);
            r = sql.executeQuery();
            while(r.next())
            {
                Amigos x = new Amigos();
                x.nombreUsuario = r.getString("NombreUsuario");
                x.usuarioId = r.getInt("UsuarioDuenoId");
                x.amigosId = r.getInt("amigosId");
                x.conexion = r.getInt("StatusConexion");
                amigos.add(x);
            }
            
            bd.closeConnection();
            return amigos;
        } catch (SQLException ex) {
            Logger.getLogger(Queries.class.getName()).log(Level.SEVERE, null, ex);
        }
        bd.closeConnection();
        return null;       
    }
    
    public boolean deleteAmistadByAmigosId(int amigosId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        boolean res = false;
        try {
            sql = bd.getCon().prepareStatement("DELETE FROM listaamigos WHERE amigosId = ?");
            sql.setInt(1, amigosId); // Suponiendo que userId es el ID del usuario actual
            int comprobar = sql.executeUpdate();
            if (comprobar > 0) {
                System.out.println("Amistad eliminada: " + amigosId);
                res = true;                
            } else {
                System.out.println("No se pudo eliminar la amistad ("+amigosId+")");
            }
        } catch (SQLException ex) {
            System.out.println("Error al eliminar la amistad ("+amigosId+")" + ex.getMessage());
        } finally {
            bd.closeConnection();
        }
        bd.closeConnection();
        return res;
    }
    public boolean insertarUsuario( String Nombre, String pass, String res) throws SQLException {
        PreparedStatement pstmt = null;
        
        BD bd = new BD();
        try {
            String sql = "INSERT INTO usuarios (NombreUsuario, Pass, RespuestaPreguntaConfianza,StatusConexion)VALUES (?,?,?,?)";
            pstmt = bd.getCon().prepareStatement(sql);
            pstmt.setString(1, Nombre);
            pstmt.setString(2, pass);
            pstmt.setString(3, res);
            pstmt.setInt(4,0 );
           
            int comprobar = pstmt.executeUpdate();
            bd.closeConnection();
            return comprobar > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }finally{
            if(pstmt != null){
                try{
                pstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            bd.closeConnection();
        }
        
        
        
    }
    public String VerContraseña(String usuarioId) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BD bd = new BD();
        try {
            String sql = "SELECT Pass FROM usuarios WHERE UsuarioId = ?";
            pstmt = bd.getCon().prepareStatement(sql);
            pstmt.setString(1, usuarioId);
            System.out.println(usuarioId);
            rs = pstmt.executeQuery();
            
            if(rs.next()){
            String Password = rs.getString("pass");

            System.out.println(Password);
            bd.closeConnection();
        return Password;
        }
        } catch (SQLException e) {
            
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        bd.closeConnection();
        return "";
        
        
    }
    public String BuscarPregunta(String usuarioId, String psw) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BD bd = new BD();
        try {
            String sql = "SELECT * FROM usuarios WHERE NombreUsuario = ? AND RespuestaPreguntaConfianza = ?";
            pstmt = bd.getCon().prepareStatement(sql);
            pstmt.setString(1, usuarioId);
            pstmt.setString(2, psw);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Credenciales válidas");
            } else {
                System.out.println("Credenciales no válidas");
                bd.closeConnection();
                return "0";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int usuarioID = rs.getInt("UsuarioId");
        String userID = "0";
        userID = String.valueOf(usuarioID);
        bd.closeConnection();
        System.out.println(userID);
        return userID;
    }
 // FIN DE QUERIES DE AMIGOS ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

 // QUERIES PARA GRUPOS -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public String obtenerSolicitudesGrupos(String usuarioRecibeIdStr)
    {
        Integer usuarioRecibeId = Integer.valueOf(usuarioRecibeIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        String resultado="";
        try {
            sql = bd.getCon().prepareStatement("SELECT ig.InvitacionId, g.Nombre, g.UsuarioDuenoId FROM invitacionesgrupos ig INNER JOIN grupos g ON ig.GrupoId = g.GrupoId WHERE ig.UsuarioRecibeId = ? AND Status = ?");
            sql.setInt(1, usuarioRecibeId);
            sql.setInt(2, 1);
            var rs = sql.executeQuery();
            while (rs.next()) {
                var invitacionId = rs.getInt("InvitacionId");
                var nombreGrupo = rs.getString("Nombre");
                var usuarioDuenoId = rs.getInt("UsuarioDuenoId");
                resultado += invitacionId + ":" + nombreGrupo + ":" + usuarioDuenoId + ";";
            }
            bd.closeConnection();
            return resultado;
        } catch (SQLException ex) {
            System.out.println("Error caso 46: " + ex.getMessage());
        } 
        bd.closeConnection();
        return null;
    }
    
    public boolean enviarSolicitudAmigos(int UsuarioEnviaId, int UsuarioRecibeId) {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("INSERT INTO invitacionesamigos (UsuarioEnviaId, UsuarioRecibeId) VALUES (?, ?)");
            sql.setInt(1, UsuarioEnviaId);
            sql.setInt(2, UsuarioRecibeId);
            int comprobar = sql.executeUpdate();
            System.out.println(comprobar + " res de solicitud de amistad");
            bd.closeConnection();
            
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println("Error caso 52: " + ex.getMessage());
            bd.closeConnection();
            return false; 
        }
    }
    
    public int InsertarGrupo(int UsuarioDuenoId, String Nombre) {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("INSERT INTO grupos (UsuarioDuenoId, Nombre) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            sql.setInt(1, UsuarioDuenoId);
            sql.setString(2, Nombre);
            int affectedRows = sql.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el grupo.");
            }

            try (var generatedKeys = sql.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el GrupoId generado.");
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error caso 53: " + ex.getMessage());
            return -1; // Devuelve un valor que indique error
        }
    }
    
    public boolean enviarSolicitudGrupos(int GrupoId,int UsuarioRecibeId, int Status) {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("INSERT INTO invitacionesgrupos (GrupoId, UsuarioRecibeId, Status) VALUES (?, ?, ?)");
            sql.setInt(1, GrupoId);
            sql.setInt(2, UsuarioRecibeId);
            sql.setInt(3, Status);
            int comprobar = sql.executeUpdate();
            
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println("Error caso 54: " + ex.getMessage());
            
            return false; 
        }
    }
    
    public String obtenerSolicitudesAmigos(int UsuarioRecibeId) {
        BD bd = new BD();
        PreparedStatement sql;
        String resultado="";
        try {
            sql = bd.getCon().prepareStatement("SELECT UsuarioEnviaId, InvitacionId FROM invitacionesamigos WHERE UsuarioRecibeId = ?");
            sql.setInt(1, UsuarioRecibeId);
            var rs = sql.executeQuery();
            while (rs.next()) {
                int usuarioEnviaId = rs.getInt("UsuarioEnviaId");
                int invitacionId = rs.getInt("InvitacionId");
                resultado += usuarioEnviaId + ":" + invitacionId + ";";
            }
            return resultado;
        } catch (SQLException ex) {
            System.out.println("Error caso 55: " + ex.getMessage());
            
            return null;
        }
    }
    
    public boolean AceptarSolicitudAmigos(int InvitacionId) {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            String query = "INSERT INTO listaamigos (UsuarioDuenoId, UsuarioId) " +
                            "SELECT ia.UsuarioEnviaId, ia.UsuarioRecibeId " +
                            "FROM invitacionesamigos ia " +
                            "WHERE ia.InvitacionId = ?";
            sql = bd.getCon().prepareStatement(query);
            sql.setInt(1, InvitacionId);
            int comprobar = sql.executeUpdate();
            
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println("Error caso 57: " + ex.getMessage());
            
            return false;
        }
    }
    
    public boolean EliminarSolicitudAmigos(int InvitacionId) {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("DELETE FROM invitacionesamigos WHERE InvitacionId = ?");
            sql.setInt(1, InvitacionId);
            int comprobar = sql.executeUpdate();
            
            //closeConnection();
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println("Error caso 58: " + ex.getMessage());
            
            return false;
        }
    }
    
    public boolean actualizarEstadoSolicitudGrupo(int invitacionId, int Status) {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            String query = "UPDATE invitacionesgrupos SET Status = ? WHERE InvitacionId = ?";
            sql = bd.getCon().prepareStatement(query);
            sql.setInt(1, Status);
            sql.setInt(2, invitacionId);
            int comprobar = sql.executeUpdate();
            
            return comprobar > 0;
        } catch (SQLException ex) {
            System.out.println("Error caso 59: " + ex.getMessage());
            
            return false;
        }
    }
    
    public String buscarNombrePorId(String usuarioIdStr)
    {
        Integer usuarioId = Integer.valueOf(usuarioIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        String nombre = null;
        ResultSet r;
        try
        {
            sql = bd.getCon().prepareStatement("SELECT NombreUsuario FROM usuarios WHERE UsuarioId=?");
            
            sql.setInt(1,usuarioId);
            r = sql.executeQuery();
            while(r.next())
            {
                nombre = r.getString("NombreUsuario");
            }
            
            bd.closeConnection();
            return nombre;
        } catch (SQLException ex) {
            System.out.println("Error caso 76: " + ex.getMessage());
        }
        bd.closeConnection();
        return null;
    }
    
    public boolean actualizarEstadoSolicitudGrupo(String invitacionIdStr, String statusStr) {////////////////
        Integer invitacionId = Integer.valueOf(invitacionIdStr);
        Integer status = Integer.valueOf(statusStr);
        BD bd = new BD();
        PreparedStatement sql;
        try {
            String query = "UPDATE invitacionesgrupos SET Status = ? WHERE InvitacionId = ?";
            sql = bd.getCon().prepareStatement(query);
            sql.setInt(1, status);
            sql.setInt(2, invitacionId);
            int comprobar = sql.executeUpdate();
            System.out.println("comprobar: " + comprobar);
            return comprobar > 0;
        } catch (SQLException ex) {
            return false;
        } finally {
            bd.closeConnection();
        }
    }
    
    public String buscarMisGrupos(String usuarioIdStr) {
        Integer usuarioId = Integer.valueOf(usuarioIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        String resultado="";
        String nombre;
        int grupoId;
        ResultSet r;
        try
        {
            sql = bd.getCon().prepareStatement("SELECT g.nombre, g.grupoId from grupos g join invitacionesgrupos ig on g.grupoId=ig.grupoId where g.usuarioDuenoId=? OR (ig.status=2 AND ig.usuarioRecibeId=?) group by g.grupoId");
            
            sql.setInt(1, usuarioId);
            sql.setInt(2, usuarioId);
            r = sql.executeQuery();
            while(r.next())
            {
                nombre = r.getString("nombre");
                grupoId= r.getInt("grupoId");
                resultado = resultado + nombre + ":" + grupoId + ";";
            }
            System.out.println("resultado: " + resultado);
            bd.closeConnection();
            return resultado;
        } catch (SQLException ex) {
            System.out.println("Error caso 77: " + ex.getMessage());
        }
        bd.closeConnection();
        return null;
    }
 // FIN DE QUERIES GRUPOS -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  
 // QUERIES CONECTADOS Y DESCONECTADOS ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public ArrayList<Usuarios> usuariosPorConexion(int conexion, int usuarioId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        ArrayList<Usuarios> usuarios;
        ResultSet r;
        try
        {
            sql = bd.getCon().prepareStatement("SELECT NombreUsuario, UsuarioId FROM usuarios WHERE StatusConexion=? AND UsuarioId!=?");
            
            sql.setInt(1,conexion);
            sql.setInt(2,usuarioId);
            usuarios = new ArrayList<>();
            r = sql.executeQuery();
            while(r.next())
            {
                Usuarios x = new Usuarios();
                x.nombreUsuario = r.getString("NombreUsuario");
                x.usuarioId = r.getInt("UsuarioId");
                usuarios.add(x);
            }
            bd.closeConnection();
            return usuarios;
        } catch (SQLException ex) {
            Logger.getLogger(Queries.class.getName()).log(Level.SEVERE, null, ex);
        }
        bd.closeConnection();
        return null;
    }
    public boolean ChangeStatus(int UserId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            System.out.println("Entro a cambiar status");
            sql = bd.getCon().prepareStatement("UPDATE usuarios SET StatusConexion = ? WHERE UsuarioId = ?");
            sql.setInt(1, 1);          
            sql.setInt(2, UserId);
            System.out.println(sql);
            int comprobar = sql.executeUpdate();
            System.out.println("Despues de query: " + comprobar);
            bd.closeConnection();
           
            return comprobar > 0;
        } catch (SQLException ex) {
            bd.closeConnection();
            return false; 
        }
    }
    public boolean cerrarSesion(int UserId){
        BD bd = new BD();
        PreparedStatement sql;
        try{
            sql = bd.getCon().prepareStatement("UPDATE usuarios SET StatusConexion = ? WHERE UsuarioId = ?"); 
            sql.setInt(1, 0);
            sql.setInt(2, UserId);

            int comprobar = sql.executeUpdate();
            bd.closeConnection();
            return comprobar > 0;
        } catch (SQLException ex) {
            bd.closeConnection();
            return false;
        }
    }
  
 // FIN DE QUERIES CONECTADOS DESCONECTADOS -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    public List<Messages> GetMessages(int chatId)
    {
        List<Messages> mensajesChat = new ArrayList<>();
        Messages mensaje;
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet res;
        try {
            sql = bd.getCon().prepareStatement("SELECT contenido, usuarioId FROM mensajes WHERE chat_Id = ?");
            sql.setInt(1, chatId);
            res = sql.executeQuery();
            
            while (res.next()) {
                mensaje = new Messages();
                mensaje.setMessageContent(res.getString("contenido"));
                mensaje.setUserId(res.getInt("UsuarioId"));
                mensajesChat.add(mensaje);
            }
        } catch (SQLException ex) {
            
        } finally {
            bd.closeConnection();
        }
        return mensajesChat;
    }
    
    public List<GroupMessages> GetGroupMessages(int groupId)
    {
        List<GroupMessages> mensajesGrupo = new ArrayList<>();
        GroupMessages mensaje;
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet res;
        try {
            sql = bd.getCon().prepareStatement("SELECT contenido_mensaje, UsuarioId FROM mensajes_grupales WHERE GrupoId = ?");
            sql.setInt(1, groupId);
            res = sql.executeQuery();
            System.out.println(sql);
            while (res.next()) {
                mensaje = new GroupMessages();
                mensaje.setMessageContent(res.getString("contenido_mensaje"));
                mensaje.setUserId(res.getInt("UsuarioId"));
                //System.out.println(mensaje);
                mensajesGrupo.add(mensaje);
            }
        } catch (SQLException ex) {
            
        } finally {
            bd.closeConnection();
        }
        return mensajesGrupo;
    }
    
    public List<IndividualChatModel> SearchChats(int chatterId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet res;
        
        List<IndividualChatModel> chatsEncontrados = new ArrayList<>();
        IndividualChatModel chat;
        
        try {
            sql = bd.getCon().prepareStatement("SELECT ConversacionId, Usuario1Id, Usuario2Id FROM chats WHERE (Usuario1Id = ?) OR (Usuario2Id= ?)");
            sql.setInt(1, chatterId);
            sql.setInt(2, chatterId);
            res = sql.executeQuery();
            
            while (res.next()) {
                chat = new IndividualChatModel();
                chat.setChatId(res.getInt("ConversacionId"));
                chat.setChatterId1(res.getInt("Usuario1Id"));
                chat.setChatterId2(res.getInt("Usuario2Id"));
                chatsEncontrados.add(chat);
            }
        } catch (SQLException ex) {
            
        } finally {
            bd.closeConnection();
        }
        return chatsEncontrados;
    }
    
    public boolean DeleteMessagesFromChat(int chatId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        try {
            sql = bd.getCon().prepareStatement("DELETE FROM mensajes WHERE chat_id = ?");
            sql.setInt(1, chatId);
            int comprobar = sql.executeUpdate();
            
            bd.closeConnection();
           
            return comprobar > 0;
        } catch (SQLException ex) {
            bd.closeConnection();
            return false; 
        }
    }
    
    public boolean SearchFriends(int usuarioId1, int usuarioId2)
    {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet res;
        
        try
        {
            sql = bd.getCon().prepareStatement("SELECT amigosId FROM listaamigos WHERE (UsuarioDuenoId = ? AND UsuarioId = ?) OR (UsuarioDuenoId = ? AND UsuarioId = ?)");
            sql.setInt(1, usuarioId1);
            sql.setInt(2, usuarioId2);
            sql.setInt(3, usuarioId2);
            sql.setInt(4, usuarioId1);
            System.out.println("Prequery");
            res = sql.executeQuery();
            
            if(res.next())
            {
                System.out.println("Son amigos");
                bd.closeConnection();
                return true;
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally
        {
            bd.closeConnection();
        }
        System.out.println("No son amigos");
        return false;
    }
    
    public int obtenerGrupoIdInvitaciones(String invitacionIdStr) {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet r;
        Integer invitacionId = Integer.valueOf(invitacionIdStr);
        int grupoId = -1;
        try {
            sql = bd.getCon().prepareStatement("Select grupoId from invitacionesgrupos where invitacionId=?");
            sql.setInt(1,invitacionId);
            r = sql.executeQuery();
            while(r.next())
            {
                grupoId = r.getInt("grupoId");
            }
            bd.closeConnection();
            return grupoId;
        } catch (SQLException ex) {
            System.out.println("Error 79: " + ex.getMessage());
        } finally {
            bd.closeConnection();
        }
        return grupoId;
    }
    
    public int obtenerCantParticipantes(String grupoIdStr) {
        Integer grupoId = Integer.valueOf(grupoIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet r;
        int cantidadInvitaciones = -1;
        try {
            sql = bd.getCon().prepareStatement("SELECT count(*) as cant from invitacionesgrupos where grupoId=? AND status<=2");
            sql.setInt(1,grupoId);
            r = sql.executeQuery();
            while(r.next())
            {
                cantidadInvitaciones = r.getInt("cant");
            }
            bd.closeConnection();
            return cantidadInvitaciones;
        } catch (SQLException ex) {
            System.out.println("Error caso 80: " + ex.getMessage());
        } finally {
            bd.closeConnection();
        }
        return cantidadInvitaciones;
    }
    
    public int deleteMensajesGrupos(String grupoIdStr) {
        Integer grupoId = Integer.valueOf(grupoIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        int comprobar = -1;
        try {
            sql = bd.getCon().prepareStatement("DELETE FROM mensajes_grupales where grupoId=?");
            sql.setInt(1, grupoId);
            comprobar = sql.executeUpdate();
            bd.closeConnection();
            return comprobar ;
        } catch (SQLException ex) {
            System.out.println("Error 81: " + ex.getMessage());
        } finally
        {
            bd.closeConnection();
        } 
        return comprobar;
    }
    
    public int deleteInvitacionesGrupos(String grupoIdStr) {
        Integer grupoId = Integer.valueOf(grupoIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        int comprobar = 0;
        try {
            sql = bd.getCon().prepareStatement("DELETE FROM invitacionesGrupos where grupoId=?");
            sql.setInt(1, grupoId);
            comprobar = sql.executeUpdate();
            
            bd.closeConnection();
            return comprobar;
        } catch (SQLException ex) {
            
        } finally
        {
            bd.closeConnection();
        }
        return comprobar;
    }
    
    public int deleteGrupo(String grupoIdStr) {
        Integer grupoId = Integer.valueOf(grupoIdStr);
        BD bd = new BD();
        PreparedStatement sql;
        int comprobar = 0;
        try {
            sql = bd.getCon().prepareStatement("DELETE FROM grupos where grupoId=?");
            sql.setInt(1, grupoId);
            comprobar = sql.executeUpdate();
            
            bd.closeConnection();
            return comprobar;
        } catch (SQLException ex) {
            System.out.println("Error 83: " + ex.getMessage());
        } finally
        {
            bd.closeConnection();
        }
        return comprobar;
    }
    
    public boolean selectDuenoId(int grupoId, int usuarioId) {
        BD bd = new BD();
        PreparedStatement sql;
        ResultSet r;
        int usuarioDuenoId = 0;
        try {
            sql = bd.getCon().prepareStatement("Select usuarioDuenoId from grupos where grupoId = ?");
            sql.setInt(1, grupoId);
            r = sql.executeQuery();
            while(r.next())
            {
                usuarioDuenoId = r.getInt("usuarioDuenoId");
            }
            
            bd.closeConnection();
            return usuarioId==usuarioDuenoId;
        } catch (SQLException ex) {
            System.out.println("Error caso 84: " + ex.getMessage());
        } finally
        {
            bd.closeConnection();
        }
        return usuarioId==usuarioDuenoId;
    }
    
    public String MiembrosGrupo(int grupoId, int status) {
        BD bd = new BD();
        PreparedStatement sql = null;
        ResultSet r = null;
        StringBuilder resultado = new StringBuilder();
        try {
            sql = bd.getCon().prepareStatement("SELECT DISTINCT u.NombreUsuario, u.UsuarioId FROM usuarios u " +
                    "LEFT JOIN invitacionesgrupos i ON i.usuarioRecibeId = u.usuarioId AND i.status = 2 " +
                    "LEFT JOIN grupos g ON g.grupoId = i.grupoId OR g.usuarioDuenoId = u.usuarioId " +
                    "WHERE g.grupoId = ? AND u.statusConexion = ?");
            sql.setInt(1, grupoId);
            sql.setInt(2, status);
            r = sql.executeQuery();
            
            while (r.next()) {
                String nombre = r.getString("NombreUsuario");
                int usuarioId = r.getInt("UsuarioId");
                resultado.append(nombre).append(":").append(usuarioId).append(";");
            }
        } catch (SQLException ex) {
            System.out.println("Error caso 85: " + ex.getMessage());
        } finally {
            bd.closeConnection();
        }
        
        return resultado.toString();
    }
    
    public boolean deleteUsuarioRecibeId(int grupoId, int usuarioId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        int comprobar = 0;
        try {
            System.out.println("grupo: " + grupoId + " " + usuarioId);
            sql = bd.getCon().prepareStatement("DELETE FROM invitacionesGrupos where grupoId=? and usuarioRecibeId=?");
            sql.setInt(1, grupoId);
            sql.setInt(2, usuarioId);
            comprobar = sql.executeUpdate();
            
            bd.closeConnection();
            return comprobar>0;
        } catch (SQLException ex) {
            System.out.println("Error caso 86: " + ex.getMessage());
        } finally
        {
            bd.closeConnection();
        }
        return comprobar>0;
    }
    
    public String selectNombreGrupo(int grupoId)
    {
        BD bd = new BD();
        PreparedStatement sql;
        String nombreGrupo=null;
        ResultSet r;
        try
        {
            sql = bd.getCon().prepareStatement("SELECT nombre FROM grupos WHERE grupoId=?");
            sql.setInt(1,grupoId);
            r = sql.executeQuery();
            while(r.next())
            {
                nombreGrupo = r.getString("nombre");
            }
            
            bd.closeConnection();
            return nombreGrupo;
        } catch (SQLException ex) {
            System.out.println("Error caso 87: " + ex.getMessage());
        }finally
        {
            bd.closeConnection();
        }
        return null;
    }
    
    public String obtenerUsuariosIdGrupos(int grupoId)
    {
        BD bd = new BD();
        PreparedStatement sql = null;
        ResultSet r = null;
        StringBuilder resultado = new StringBuilder();
        try {
            sql = bd.getCon().prepareStatement("SELECT DISTINCT u.UsuarioId FROM usuarios u " +
                    "LEFT JOIN invitacionesgrupos i ON i.usuarioRecibeId = u.usuarioId AND i.status <= 2 " +
                    "LEFT JOIN grupos g ON g.grupoId = i.grupoId OR g.usuarioDuenoId = u.usuarioId " +
                    "WHERE g.grupoId = ?");
            sql.setInt(1, grupoId);
            r = sql.executeQuery();
            
            while (r.next()) {
                int usuarioId = r.getInt("UsuarioId");
                resultado.append(usuarioId).append(":");
                //System.out.println("resultado: " + resultado);
            }
        } catch (SQLException ex) {
            System.out.println("Error caso 88: " + ex.getMessage());
        } finally {
            try {
                if (r != null) r.close();
                if (sql != null) sql.close();
            } catch (SQLException ex) {
                System.out.println("Error cerrando recursos: " + ex.getMessage());
            }
            bd.closeConnection();
        }
        
        return resultado.toString();
    }
}
