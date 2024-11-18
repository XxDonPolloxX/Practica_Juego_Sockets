package es.uva.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClienteJuego {
    // La clase cliente tiene las siguientes responsabilidades
    // Unirse al juego conectandose al servidor
    // Mantener un estado de juego actualizado interpretando los
    // mensajes del servidor (y mostrar el estado)
    // Convertir input del jugador en un mensaje que enviar al servidor
    // NOTA: para simplificar el manejo de input podemos considerar
    // que el usario manda cada comando en una linea distinta
    // (aunque sea muy incomodo)

    public final Estado estado;
    // TODO: Faltarán atributos ...
    private Socket server;
    private BufferedReader in;
    private PrintWriter out;

    public ClienteJuego(int size) {
        // [OPCIONAL] TODO: Extiende el protocolo de comunicacion para
        // que el servidor envie el tamaño del mapa tras la conexion
        // de manera que el estado no se instancie hasta entonces
        // y conocer este parametro a priori no sea necesario.
        estado = new Estado(size);
        server= null;
    }

    public Estado getEstado() {
        return estado;
    }

    public void iniciar(String host, int puerto) throws InterruptedException, IOException {
        // Metodo que reune todo y mantiene lo necesario en un bucle
        conectar(host, puerto);
        Thread procesadorMensajesServidor = new Thread(() -> {
            while (!estado.estaTerminado()) {
                procesarMensajeServidor();
            }
        });
        Thread procesadorInput = new Thread(() -> {
            while (!estado.estaTerminado()) {
                procesarInput();
            }
        });
        procesadorMensajesServidor.start();
        procesadorInput.start();
        procesadorInput.join();
        procesadorMensajesServidor.join();
        // Si acaban los hilos es que el juego terminó
        cerrarConexion();
    }

    public void cerrarConexion() {
        // TODO: cierra todos los recursos asociados a la conexion con el servidor

        try {
            server.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void conectar(String host, int puerto) throws IOException {
        // TODO: iniciar la conexion con el servidor
        // (Debe guardar la conexion en un atributo)

        try {
            server = new Socket(host, puerto);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        out = new PrintWriter(server.getOutputStream(), true);


    }

    public void procesarInput() {
        // TODO: Comprueba la entrada estandar y
        // se procesa mediante intrepretar input,
        // Se genera un mensaje que se envia al servidor

        Scanner sc = new Scanner(System.in);
        char tecla = sc.next().charAt(0);
        String mensaje = interpretarInput(tecla);
        out.println(mensaje);
        out.flush();
        // server.getOutputStream().write(mensaje.getBytes());


    }

    public void procesarMensajeServidor() {
        // TODO: Comprueba la conexion y obtiene un mensaje
        // que se procesa con interpretarMensaje
        // Al recibir la actualizacion del servidor podeis
        // Usar el metodo mostrar del estado
        // Para enseñarlo

        try {
            String mensaje = in.readLine();
            interpretarMensaje(mensaje);
            estado.mostrar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String interpretarInput(char tecla) {
        // TODO: WASD para moverse, Q para buscar
        // Este metodo debe devolver el comando necesario
        // Que enviar al servidor

        switch (tecla) {
            case 'W':
                return "MOVER UP";
            case 'A':
                return "MOVER LEFT";
            case 'S':
                return "MOVER DOWN";
            case 'D':
                return "MOVER RIGHT";
            case 'Q':
                return "CAVAR";
            default:
                return "ERROR TECLA";

        }
    }

    public void interpretarMensaje(String mensaje) {
        // TODO: interpretar los mensajes del servidor actualizando el estado

        String mensaje1 = mensaje.split(" ")[0];
        switch (mensaje1){
            case "PLAYER":
                int id = Integer.parseInt(mensaje.split(" ")[2]);
                int x = Integer.parseInt(mensaje.split(" ")[3]);
                int y = Integer.parseInt(mensaje.split(" ")[4]);
                estado.nuevoJugador(new Jugador(id, new Coordenadas(x, y)));
                break;

            case "MOVE":
                String direccion = mensaje.split(" ")[1];
                int id1 = Integer.parseInt(mensaje.split(" ")[2]);
                switch (direccion){
                    case "UP":
                        estado.mover(id1, Direccion.UP);
                        break;
                    case "DOWN":
                        estado.mover(id1, Direccion.DOWN);
                        break;
                    case "LEFT":
                        estado.mover(id1, Direccion.LEFT);
                        break;
                    case "RIGHT":
                        estado.mover(id1, Direccion.RIGHT);
                        break;
                }
                break;

            case "DIG":
                int id2 = Integer.parseInt(mensaje.split(" ")[1]);
                int exito = Integer.parseInt(mensaje.split(" ")[2]);
                if (exito == 1) {
                    estado.terminar();
                }
                estado.buscar(id2);
                break;
        }
    }
}
