package server;

import Controllers.Queries;
import models.IndividualChatModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import models.Messages;
import models.GroupMessages;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import models.Amigos;
import models.Usuarios;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {

    private static final int PORT = 1234;
    private static final HashMap<String, ClientHandler> clients = new HashMap<>();
    
    public static LogServer GUI = new LogServer();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado... Esperando clientes.");
            
            GUI.setVisible(true);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, clients);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {

        }
    }

    static class ClientHandler implements Runnable {

        private Socket socket;
        private HashMap<String, ClientHandler> clients;
        private DataOutputStream out;
        private BufferedReader in;
        private String clientId;

        public ClientHandler(Socket socket, HashMap<String, ClientHandler> clients) {
            this.socket = socket;
            this.clients = clients;
        }

        @Override
        public void run() {
            int UsuarioRecibeId = -1, Status = -1, InvitacionId = -1;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                Queries queries = new Queries();
                String query_credenciales = in.readLine();
                if (query_credenciales != null) {
                    String[] parts = query_credenciales.split(":");
                    int queryNumber = Integer.parseInt(parts[0]);
                    switch (queryNumber) {
                        case 1:
                            String usuario = parts[1];
                            String password = parts[2];
                            String usuarioId = queries.BuscarCredenciales(usuario, password);
                            GUI.SendToServerLog("Usuario " + usuarioId + " Intento iniciar sesion");
                            out.writeBytes(usuarioId);
                            break;
                        case 16:
                            String chatterId1_16 = parts[1];
                            System.out.println(chatterId1_16);
                            String chatterId2_16 = parts[2];
                            System.out.println(chatterId2_16);
                            int chatId_16 = queries.GetChatId(Integer.parseInt(chatterId1_16), Integer.parseInt(chatterId2_16));
                            GUI.SendToServerLog("Se obtuvo el id del chat " + chatId_16);
                            out.writeBytes(String.valueOf(chatId_16));
                            break;
                        case 17:
                            String chatterId1_17 = parts[1];
                            String chatterId2_17 = parts[2];
                            boolean registroExitoso = queries.CreateChat(Integer.parseInt(chatterId1_17), Integer.parseInt(chatterId2_17));
                            if (registroExitoso) {
                                GUI.SendToServerLog("Creacion del chat exitosa");
                                out.writeBytes("true");
                            } else {
                                out.writeBytes("false");
                            }
                            break;
                        case 18:
                            String chatterId_18 = parts[1];
                            List<IndividualChatModel> chatsFound = queries.SearchChats(Integer.parseInt(chatterId_18));
                            for (IndividualChatModel chat : chatsFound) {
                                int chatId_18 = chat.getChatId();
                                int chatterId1_18 = chat.getChatterId1();
                                int chatterId2_18 = chat.getChatterId2();
                                String messageToSend_18 = chatId_18 + ":" + chatterId1_18 + ":" + chatterId2_18;
                                out.writeBytes(messageToSend_18 + "\n");
                                out.flush();
                            }
                            break;
                        
                        case 31:
                            String message = parts[1];
                            String chatId_31 = parts[2];
                            String userId_31 = parts[3];
                            boolean mensajeSubido = queries.SendMessageToBD(message, Integer.parseInt(chatId_31), Integer.parseInt(userId_31));
                            GUI.SendToServerLog("El usuario " + userId_31 + "mando el mensaje '" + message + "' al chat " + chatId_31);
                            out.writeBytes(mensajeSubido ? "true\n" : "false\n");
                            out.flush();
                            break;
                        case 32:
                            String chatId_32 = parts[1];
                            List<Messages> messageList = queries.GetMessages(Integer.parseInt(chatId_32));
                            for (Messages messageIndividual : messageList) {
                                int userId_32 = messageIndividual.getUserId();
                                String messageContent_32 = messageIndividual.getMessageContent();

                                String messageToSend_32 = userId_32 + ":" + messageContent_32;
                                out.writeBytes(messageToSend_32 + "\n");
                                out.flush();
                            }
                            out.writeBytes("Mensajes Terminados\n");
                            out.flush();
                            break;
                        case 33:
                            String chatId_33 = parts[1];
                            boolean chatDeleted = queries.DeleteMessagesFromChat(Integer.parseInt(chatId_33));
                            out.writeBytes(chatDeleted ? "true\n" : "false\n");
                            out.flush();
                            break;
                        case 34:
                            String message_group = parts[1];
                            String groupId_34 = parts[2];
                            String userId_34 = parts[3];
                            boolean mensajeSubido_34 = queries.SendMessageGroupToBD(message_group, Integer.parseInt(groupId_34), Integer.parseInt(userId_34));
                            GUI.SendToServerLog("El usuario " + userId_34 + "mando el mensaje '" + message_group + "' al grupo " + groupId_34);
                            out.writeBytes(mensajeSubido_34 ? "true\n" : "false\n");
                            out.flush();
                            break;
                        case 35:
                            String groupId_35 = parts[1];
                            List<GroupMessages> groupMessageList = queries.GetGroupMessages(Integer.parseInt(groupId_35));
                            for (GroupMessages messageGrupal : groupMessageList) {
                                int userId_35 = messageGrupal.getUserId();
                                String messageContent_35 = messageGrupal.getMessageContent();
                                String messageToSend_35 = userId_35 + ":" + messageContent_35;
                                System.out.println(messageToSend_35);
                                out.writeBytes(messageToSend_35 + "\n");
                                out.flush();
                            }
                            out.writeBytes("Mensajes Terminados\n");
                            out.flush();
                            break;
                        case 2:
                            try {
                                int userIdQuery = Integer.parseInt(parts[1]);
                                System.out.println("userId del que buscamos amigos:  "+userIdQuery);
                                ArrayList<Amigos> amigos = queries.selectMisAmigosUsuarios(userIdQuery);
                                StringBuilder messageAmigos = new StringBuilder();
                                if (amigos.isEmpty()) {
                                    messageAmigos.append("0");
                                    GUI.SendToServerLog("buscando amigos del userID: "+userIdQuery+"... no se encontraron amigos, retornando 0!");
                                
                                } else {
                                    for (Amigos a : amigos) {
                                        System.out.println(a);
                                        messageAmigos.append(a.amigosId + ":" + a.usuarioId + ":" + a.nombreUsuario + ":" + a.conexion + "_");
                                    }
                                    GUI.SendToServerLog("buscando amigos del userID: "+userIdQuery+"... se encontraron amigos, retornando la lista!");
                                }
                                
                                out.writeBytes(messageAmigos.toString());

                            } catch (IOException | NumberFormatException e) {

                            }
                            break;
                        case 3:
                            try {
                                int amigosId = Integer.parseInt(parts[1]);
                                System.out.println("amigosId a eliminar: " + amigosId);

                                boolean result = queries.deleteAmistadByAmigosId(amigosId);
                                if (result == true) {
                                    GUI.SendToServerLog("eliminando la relacion amigosID ("+amigosId+").... se elimino correctamente! retornando...");
                                    out.writeBytes("1");
                                } // aqui luego si me dan ganas, avisarle al user Id otro que su amistad ha sido eliminada, entonces bye bye y que se actualice automaticamente su lista, nomas tengo que en el amigos controller del cliente mandar el userId del friend y eso seria parts[2]
                                else {
                                    GUI.SendToServerLog("eliminando la relacion amigosID ("+amigosId+").... no se logro eliminar! retornando...");
                                    out.writeBytes("0");
                                }
                            } catch (Exception e) {
                                out.writeBytes("0");

                            }
                            break;
                        case 4: // conectados desconectados
                            try {
                                int userIdQuery = Integer.parseInt(parts[1]);
                                int conexion = Integer.parseInt(parts[2]);
                                // System.out.println("userId del que buscamos conectados y desconectados:  "+userIdQuery);
                                ArrayList<Usuarios> conexiones = queries.usuariosPorConexion(conexion, userIdQuery);
                                StringBuilder messageAmigos = new StringBuilder();
                                if (conexiones.isEmpty()) {
                                    messageAmigos.append("0");
                                    GUI.SendToServerLog("buscando usuarios que tengan el status conexion como ("+conexion+") que no sean el usuario id ("+userIdQuery+")... retornando 0");
                                } else {
                                    for (Usuarios a : conexiones) {
                                        messageAmigos.append(a.usuarioId + ":" + a.nombreUsuario + "_");
                                    }
                                    GUI.SendToServerLog("buscando usuarios que tengan el status conexion como ("+conexion+") que no sean el usuario id ("+userIdQuery+")... retornando la lista");
                                }
                                out.writeBytes(messageAmigos.toString());

                            } catch (Exception e) {
                            }
                            break;
                        case 5: // search friends
                            String chatterId1_5 = parts[1];
                            String chatterId2_5 = parts[2];
                            boolean areFriends = queries.SearchFriends(Integer.parseInt(chatterId1_5), Integer.parseInt(chatterId2_5));
                            out.writeBytes(areFriends ? "true\n" : "false\n");
                            out.flush();
                            break;
                        case 6:
                            String usuarioName = parts[1];
                            String passwordUser = parts[2];
                            String cancionFavorita = parts[3];
                            System.out.println(usuarioName);
                            System.out.println(passwordUser);
                            System.out.println(cancionFavorita);
                            boolean insercionExitosa = queries.insertarUsuario(usuarioName, passwordUser, cancionFavorita);
                            if (insercionExitosa) {
                                out.writeBytes("1"); 
                            } else {
                                out.writeBytes("0"); 
                            }
                            break;
                        case 7:
                            String user = parts[1];
                            String pass = parts[2];
                            
                            System.out.println(user);
                            System.out.println(pass);
                            
                            String busqueda = queries.BuscarContrasena(user, pass);
                            if (!busqueda.equals("0")) {
                                out.writeBytes(busqueda); 
                            } else {
                                out.writeBytes("0"); 
                            }
                            break;
                        case 8:
                            String userPassId = parts[1];
                            String contraseña = queries.VerContraseña(userPassId);
                            System.out.println(userPassId);
                            System.out.println(contraseña);
                            out.writeBytes(contraseña+"\n");
                            out.flush();
                            break;
                        case 15:
                            String usuarioId_15 = parts[1];
                            String Username = queries.selectNameByUserId(Integer.parseInt(usuarioId_15));
                            out.writeBytes(Username + "\n");
                            out.flush();
                        case 46:
                            String usuarioRecibeId = parts[1];
                            String listaSolicitudesGrupos = queries.obtenerSolicitudesGrupos(usuarioRecibeId);
                            GUI.SendToServerLog("Usuario: " + usuarioRecibeId + " Obteniendo solicitudes de grupos...");
                            out.writeBytes(listaSolicitudesGrupos);
                            break;
                        case 52:
                            int UsuarioEnviaId = Integer.parseInt(parts[1]);
                            UsuarioRecibeId = Integer.parseInt(parts[2]);
                            boolean EnviarSolicitudesAmigos = queries.enviarSolicitudAmigos(UsuarioEnviaId, UsuarioRecibeId);
                            GUI.SendToServerLog("Usuario: " + UsuarioEnviaId + " Enviando solicitud de amistad...");
                            if (EnviarSolicitudesAmigos == true) {
                                out.writeBytes("true");
                            } else {
                                out.writeBytes("false");
                            }
                            break;
                        case 53:
                            int UsuarioDuenoId = Integer.parseInt(parts[1]);
                            String Nombre = parts[2];
                            int InsertarGrupos = queries.InsertarGrupo(UsuarioDuenoId, Nombre);
                            String InsertarGruposstr = String.valueOf(InsertarGrupos);
                            GUI.SendToServerLog("Usuario: " + UsuarioDuenoId + " Creando nuevo grupo...");
                            out.writeBytes(InsertarGruposstr);
                            break;
                        case 54:
                            int GrupoId = Integer.parseInt(parts[1]);
                            UsuarioRecibeId = Integer.parseInt(parts[2]);
                            Status = Integer.parseInt(parts[3]);
                            boolean EnviarSolicitudesGrupos = queries.enviarSolicitudGrupos(GrupoId, UsuarioRecibeId, Status);
                            GUI.SendToServerLog("Usuario: " + UsuarioRecibeId + " Recibiendo solicitud de grupo...");
                            if (EnviarSolicitudesGrupos == true) {
                                out.writeBytes("true");
                            } else {
                                out.writeBytes("false");
                            }
                            break;
                        case 55:
                            UsuarioRecibeId = Integer.parseInt(parts[1]);
                            String ObtenerSolicitudesAmigos = queries.obtenerSolicitudesAmigos(UsuarioRecibeId);
                            GUI.SendToServerLog("Usuario: " + UsuarioRecibeId + " Obteniendo solicitudes de amistad...");
                            out.writeBytes(ObtenerSolicitudesAmigos);
                            break;
                        case 57:
                            InvitacionId = Integer.parseInt(parts[1]);
                            boolean AceptarSolicitudesAmigos = queries.AceptarSolicitudAmigos(InvitacionId);
                            GUI.SendToServerLog("Aceptando solicitud de amistad...");
                            if (AceptarSolicitudesAmigos == true) {
                                out.writeBytes("true");
                            } else {
                                out.writeBytes("false");
                            }
                            break;
                        case 58:
                            InvitacionId = Integer.parseInt(parts[1]);
                            boolean EliminarSolicitudesAmigos = queries.EliminarSolicitudAmigos(InvitacionId);
                            GUI.SendToServerLog("Eliminado solicitud de amistad...");
                            if (EliminarSolicitudesAmigos == true) {
                                out.writeBytes("true");
                            } else {
                                out.writeBytes("false");
                            }
                            break;
                        case 59:
                            Status = Integer.parseInt(parts[1]);
                            InvitacionId = Integer.parseInt(parts[2]);
                            boolean ActualizarEstadoGrupo = queries.actualizarEstadoSolicitudGrupo(InvitacionId, Status);
                            GUI.SendToServerLog("Actualizando estado de un integrante del grupo...");
                            if (ActualizarEstadoGrupo == true) {
                                out.writeBytes("true");
                            } else {
                                out.writeBytes("false");
                            }
                            break;
                        case 75:
                            String nombreUsuario75 = parts[1];
                            System.out.println("entre a 75");
                            System.out.println(nombreUsuario75);
                            int EncontrarUsuario = queries.EncontrarUsuarios(nombreUsuario75);
                            System.out.println(EncontrarUsuario);
                            String userId = String.valueOf(EncontrarUsuario);
                            GUI.SendToServerLog("Usuario:  " + nombreUsuario75 +" Bucando usuarioId por nombre de usuario...");
                            out.writeBytes(userId);
                            break;
                        case 76:
                            String usuarioId_76 = parts[1];
                            String nombreUsuario = queries.buscarNombrePorId(usuarioId_76);
                            if(nombreUsuario!=null)
                            {
                                GUI.SendToServerLog("Buscando nombre del usuario "+usuarioId_76+"... y se encontro");
                            } else
                            {
                                GUI.SendToServerLog("Buscando nombre del usuario "+usuarioId_76+"... y no se encontro");
                            }
                            out.writeBytes(nombreUsuario);
                            break;
                        case 77:
                            String usuarioId_77 = parts[1];
                            String listaGrupos = queries.buscarMisGrupos(usuarioId_77);
                            if(listaGrupos!=null)
                            {
                                GUI.SendToServerLog("Buscando grupos del usuario "+usuarioId_77+"... y se encontraron");
                            } else
                            {
                                GUI.SendToServerLog("Buscando grupos del usuario "+usuarioId_77+"... y no se encontraron");
                            }
                            out.writeBytes(listaGrupos);
                            break;
                        case 78:
                            String invitacionId_78 = parts[1];
                            String status_78 = parts[2];
                            boolean verificacion_78 = queries.actualizarEstadoSolicitudGrupo(invitacionId_78, status_78);
                            if(verificacion_78){
                                GUI.SendToServerLog("Cambiando estado de invitacion " + invitacionId_78 + " a "+status_78+"... y si lo cambio");
                                out.writeBytes("true");
                            }else{
                                GUI.SendToServerLog("Cambiando estado de invitacion " + invitacionId_78 + " a "+status_78+"... y no lo cambio");
                                out.writeBytes("false");
                            }
                            break;
                        case 79:
                            String invitacionId_79 = parts[1];
                            int grupoId = queries.obtenerGrupoIdInvitaciones(invitacionId_79);
                            if(grupoId>0){
                               GUI.SendToServerLog("Buscando el id del grupo segun el id de la invitacion "+invitacionId_79+"... y si se encontro");
                            } else
                            {
                                GUI.SendToServerLog("Buscando el id del grupo segun el id de la invitacion "+invitacionId_79+"... y no se encontro");
                            }
                            out.write(grupoId);
                            break;
                        case 80:
                            String grupoId_80 = parts[1];
                            int cantParticipantes = queries.obtenerCantParticipantes(grupoId_80);
                            if(cantParticipantes!=-1)
                            {
                                GUI.SendToServerLog("Buscando cuantos particiapantes hay en el grupo "+grupoId_80+"... y se encontro la cuenta");
                            } else
                            {
                                GUI.SendToServerLog("Buscando cuantos particiapantes hay en el grupo "+grupoId_80+"... y no se encontro la cuenta");
                            }
                            out.write(cantParticipantes);
                            break;
                        case 81:
                            String grupoId_81 = parts[1];
                            int verificacion_81 = queries.deleteMensajesGrupos(grupoId_81);
                            if(verificacion_81>0)
                            {
                                GUI.SendToServerLog("Eliminando los mensajes del grupo "+grupoId_81+"... y se borraron correctamente");
                            } else
                            {
                                GUI.SendToServerLog("Eliminando los mensajes del grupo "+grupoId_81+"... y se borraron correctamente");
                            }
                            out.write(verificacion_81);
                            break;
                        case 82:
                            String grupoId_82 = parts[1];
                            int verificacion_82 = queries.deleteInvitacionesGrupos(grupoId_82);
                            out.write(verificacion_82);
                            if(verificacion_82>0)
                            {
                                GUI.SendToServerLog("Eliminando los participantes e invitaciones pendientes del grupo "+grupoId_82+"... y se borraron correctamente");
                            } else
                            {
                                GUI.SendToServerLog("Eliminando los participantes e invitaciones pendientes del grupo "+grupoId_82+"... y no se borraron");
                            }
                            break;
                        case 83:
                            String grupoId_83 = parts[1];
                            int verificacion_83 = queries.deleteGrupo(grupoId_83);
                            
                            if(verificacion_83>0)
                            {
                                GUI.SendToServerLog("Eliminando el grupo "+grupoId_83+"... y se borro correctamente");
                            } else
                            {
                                GUI.SendToServerLog("Eliminando el grupo "+grupoId_83+"... y no se borro");
                            }
                            out.write(verificacion_83);
                            break;
                        case 84:
                            String grupoId_84 = parts[1];
                            String usuarioId_84 = parts[2];
                            boolean verificacion_84 = queries.selectDuenoId(Integer.parseInt(grupoId_84), Integer.parseInt(usuarioId_84));
                            if(verificacion_84){
                                GUI.SendToServerLog("Buscando si el usuario " + usuarioId_84+ " es dueno del grupo "+grupoId_84+"... y si es");
                                out.writeBytes("true");
                            }else{
                                GUI.SendToServerLog("Buscando si el usuario " + usuarioId_84+ " es dueno del grupo "+grupoId_84+"... y no es");
                                out.writeBytes("false");
                            }
                            break;
                        case 85:
                            String grupoId_85 = parts[1];
                            String status_85 = parts[2];
                            String miembros = queries.MiembrosGrupo(Integer.parseInt(grupoId_85), Integer.parseInt(status_85));
                            if(miembros!=null){
                                GUI.SendToServerLog("Buscando los usuarios con status en conexion " + status_85+ "  del grupo "+grupoId_85 +"... y si se encontraron algunos");
                            }else{
                                GUI.SendToServerLog("Buscando los usuarios con status en conexion " + status_85+ "  del grupo "+grupoId_85 +"... y no se encontraron");
                            }
                            out.writeBytes(miembros);
                            break;
                        case 86:
                            String grupoId_86 = parts[1];
                            String usuarioRecibeId_86 = parts[2];
                            boolean verificacion_86 = queries.deleteUsuarioRecibeId(Integer.parseInt(grupoId_86), Integer.parseInt(usuarioRecibeId_86));
                            if(verificacion_86){
                                GUI.SendToServerLog("Eliminando al usuario "+ usuarioRecibeId_86 +  " del grupo "+grupoId_86+"... y se borro correctamente");
                                out.writeBytes("true");
                            }else{
                                GUI.SendToServerLog("Eliminando al usuario "+ usuarioRecibeId_86 +  " del grupo "+grupoId_86+"... y se no borro");
                                out.writeBytes("false");
                            }
                            break;
                        case 87:
                            String grupoId_87 = parts[1];
                            String nombreGrupo = queries.selectNombreGrupo(Integer.parseInt(grupoId_87));
                            if(nombreGrupo!=null)
                            {
                                GUI.SendToServerLog("Buscando el nombre del grupo "+grupoId_87+"... y se encontro");
                            } else
                            {
                                GUI.SendToServerLog("Buscando el nombre del grupo "+grupoId_87+"... y no se encontro");
                            }
                            out.writeBytes(nombreGrupo);
                            break;
                        case 88:
                            String grupoId_88 = parts[1];
                            String miembros_88 = queries.obtenerUsuariosIdGrupos(Integer.parseInt(grupoId_88));
                            if(miembros_88!=null)
                            {
                                GUI.SendToServerLog("Buscando los id de los miembros del grupo "+grupoId_88+"... y se encontraron");
                            } else
                            {
                                GUI.SendToServerLog("Buscando los id de los miembros del grupo "+grupoId_88+"... y no se encontraron");
                            }
                            out.writeBytes(miembros_88);
                            break;
                        case 99:
                            String UserId_99 = parts[1];
                            boolean status = queries.ChangeStatus(Integer.parseInt(UserId_99));
                            GUI.SendToServerLog("Se ha conectado el usuario" + UserId_99);
                            out.writeBytes(status ? "true\n" : "false\n");
                            out.flush();
                            break;
                        case 100:
                            String UserId_100 = parts[1];
                            boolean comprobar_100 = queries.cerrarSesion(Integer.parseInt(UserId_100));
                            GUI.SendToServerLog("Se ha descconectado el usuario" + UserId_100);
                            out.writeBytes(comprobar_100 ? "true\n" : "false\n");
                            out.flush();
                            break;
                    }

                }
            } catch (IOException | SQLException e) {

            } finally {
                try {
                    socket.close();
                    
                } catch (IOException e) {

                }
            }
        }
    }

    static class LogServer extends JFrame {

        private JTextArea textArea;
        private JButton addButton;
        private String textFile = "";

        public LogServer() {
            // Configurar el título de la ventana
            super("Log Server");

            // Configurar el tamaño de la ventana
            setSize(500, 400);

            // Configurar la operación por defecto al cerrar la ventana
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Crear los componentes
            textArea = new JTextArea();
            addButton = new JButton("Descargar Log del Server");

            // Configurar el área de texto para que tenga barras de desplazamiento
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            // Crear un panel para el campo de texto y el botón
            JPanel inputPanel = new JPanel();
            inputPanel.add(addButton);

            // Añadir los componentes a la ventana
            add(scrollPane, BorderLayout.CENTER);
            add(inputPanel, BorderLayout.SOUTH);

            // Añadir un ActionListener al botón
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Descargar archivo
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String FileName = "Chat_" + timestamp + ".txt"; 
                    try {
                        // Crear el objeto File
                        File archivo = new File(FileName);

                        // Crear el archivo si no existe
                        if (!archivo.exists()) {
                            archivo.createNewFile();
                        }

                        // Usar FileWriter y BufferedWriter para escribir en el archivo
                        FileWriter fw = new FileWriter(archivo);
                        BufferedWriter bw = new BufferedWriter(fw);

                        // Escribir el texto en el archivo
                        bw.write(textFile);

                        // Cerrar BufferedWriter y FileWriter
                        bw.close();
                        fw.close();

                        System.out.println("El archivo ha sido creado y el texto ha sido escrito.");
                    }catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    textFile = "";
                    textArea.selectAll();
                    textArea.replaceSelection("");
                }
            });

            // Centrar la ventana en la pantalla
            setLocationRelativeTo(null);
        }

        // Método para agregar texto al área de texto
        public void SendToServerLog(String texto) {
            textArea.append(texto + "\n");
            textFile += texto + "\n";
        }
    }

}