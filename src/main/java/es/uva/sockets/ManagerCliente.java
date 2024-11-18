package es.uva.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManagerCliente extends Thread {
    // Clase para que el encargado de cada cliente
    // Se ejecute en un hilo diferente

    private final Socket socket;
    private final ServidorJuego servidor;
    private final int idJugador;
    private final PrintWriter out ;
    private final BufferedReader in;
    // Se pueden usar mas atributos ...

    public ManagerCliente(Socket socket, ServidorJuego servidor, int idJugador) throws IOException {
        this.socket = socket;
        this.servidor = servidor;
        this.idJugador = idJugador;
        // Se pueden usar mas atributos ...
        this.out  = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public int getIdJugador() {
        return idJugador;
    }

    public void enviarMensaje(String message) {
        // TODO: enviar un mensaje. NOTA: a veces hace falta usar flush.
        out.println(message);
        out.flush();
    }

    @Override
    public void run() {
        // Mantener todos los procesos necesarios hasta el final
        // de la partida (alguien encuentra el tesoro)
        while (!servidor.estado.estaTerminado() && !socket.isClosed()) {
            procesarMensajeCliente();
        }
    }

    public void procesarMensajeCliente() {
        // TODO: leer el mensaje del cliente
        // y procesarlo usando interpretarMensaje
        // Si detectamos el final del socket
        // gestionar desconexion ...

        try{
            String mensaje = in.readLine();
            interpretarMensaje(mensaje);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void interpretarMensaje(String mensaje) {
        // TODO: Esta función debe realizar distintas
        // Acciones según el mensaje recibido
        // Manipulando el estado del servidor
        // Si el mensaje recibido no tiene el formato correcto
        // No ocurre nada

        String[] partes = mensaje.split(" ");
        if (partes.length == 0) {
            return;
        }
        switch (partes[0]) {
            case "MOVER":
                if (partes.length != 2) {
                    return;
                }
                Direccion dir = Direccion.valueOf(partes[1]);
                servidor.estado.mover(idJugador, dir);
                servidor.broadcast("MOVE " + dir + " " + idJugador);
                break;
            case "CAVAR":
                boolean encontrado = servidor.estado.buscar(idJugador);
                servidor.broadcast("DIG " + idJugador + " " + (encontrado ? "1" : "0"));
                if (encontrado) {
                    servidor.estado.terminar();
                }
                break;
            default:
                break;
        }




    }
}