package es.uva.sockets;

import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws IllegalArgumentException, UnknownHostException, IOException, InterruptedException{
        if (args[0].equals("cliente")) {
            new ClienteJuego(10).iniciar(args[1], Integer.parseInt(args[2]));
        } else if (args[0].equals("servidor")) {
            new ServidorJuego(10, Integer.parseInt(args[1])).iniciar();
        }
        throw new IllegalArgumentException(
                "El primer argumento debe especificar cliente o servidor");
    }
}
