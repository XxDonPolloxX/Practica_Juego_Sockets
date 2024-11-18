package es.uva.sockets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AppTest {

    private ServidorJuego servidorJuego;
    public int puerto;

    @BeforeEach
    public void initServer() throws IOException {
        servidorJuego = new ServidorJuego(3, 0);
        puerto = servidorJuego.serverSocket.getLocalPort();
    }

    private void assertJugadoresEquals(List<Jugador> jugadores1, List<Jugador> jugadores2) {
        assertEquals(jugadores1.size(), jugadores2.size());
        Map<Integer, Jugador> clienteJugadores = new HashMap<>();
        for (Jugador jugador : jugadores1) {
            clienteJugadores.put(jugador.id, jugador);
        }

        for (Jugador jugador : jugadores2) {
            Jugador otro = clienteJugadores.get(jugador.id);
            assertTrue(otro != null);
            assertTrue(jugador.coordenadas.equals(otro.coordenadas));
        }
    }

    @Test
    public void shouldJoin() throws Exception {
        ClienteJuego cliente = new ClienteJuego(3);
        cliente.conectar("localhost", puerto);
        servidorJuego.aceptarConexion();
        cliente.procesarMensajeServidor();
        assertJugadoresEquals(servidorJuego.estado.jugadores, cliente.estado.jugadores);
    }

    @Test
    public void shouldBroadcastJoin() throws Exception {
        ClienteJuego clienteJuego1 = new ClienteJuego(3);
        clienteJuego1.conectar("localhost", puerto);
        servidorJuego.aceptarConexion();
        clienteJuego1.procesarMensajeServidor();
        ClienteJuego clienteJuego2 = new ClienteJuego(3);
        clienteJuego2.conectar("localhost", puerto);
        servidorJuego.aceptarConexion();
        clienteJuego1.procesarMensajeServidor();
        clienteJuego2.procesarMensajeServidor();
        clienteJuego2.procesarMensajeServidor();
        assertEquals(2, servidorJuego.estado.jugadores.size());
        assertJugadoresEquals(servidorJuego.estado.jugadores, clienteJuego1.estado.jugadores);
        assertJugadoresEquals(servidorJuego.estado.jugadores, clienteJuego2.estado.jugadores);
    }

    @ParameterizedTest
    @CsvSource({
            "W, 1, 0",
            "S, 1, 2",
            "A, 0, 1",
            "D, 2, 1",
    })
    public void shouldMove(String key, int x, int y) throws Exception {
        System.setProperty("junit.jupiter.execution.parallel.enabled", "false");
        ClienteJuego clienteJuego1 = new ClienteJuego(3);
        clienteJuego1.conectar("localhost", puerto);
        ManagerCliente managerCliente = servidorJuego.aceptarConexion();
        clienteJuego1.procesarMensajeServidor();

        // Cambiamos las coordenadas en el servidor y en el cliente a mano
        Jugador jugadorServidor = servidorJuego.estado.jugadores.get(0);
        jugadorServidor.coordenadas = new Coordenadas(1, 1);
        Jugador jugadorCliente = clienteJuego1.estado.jugadores.get(0);
        jugadorCliente.coordenadas = new Coordenadas(1, 1);

        ClienteJuego clienteJuego2 = new ClienteJuego(3);
        clienteJuego2.conectar("localhost", puerto);
        servidorJuego.aceptarConexion();
        clienteJuego2.procesarMensajeServidor(); // Añade al jugador existente
        clienteJuego2.procesarMensajeServidor(); // Añade al jugador nuevo
        clienteJuego1.procesarMensajeServidor(); // Añade al jugador nuevo

        System.out.print("EMPIEZA A MOVERSE\n");
        System.setIn(new ByteArrayInputStream(key.getBytes()));
        clienteJuego1.procesarInput();
        managerCliente.procesarMensajeCliente();
        clienteJuego1.procesarMensajeServidor();
        // El otro cliente tambien recibe la actualizacion de movimiento
        clienteJuego2.procesarMensajeServidor();
        assertTrue(jugadorServidor.coordenadas.equals(new Coordenadas(x, y)));
        assertTrue(jugadorCliente.coordenadas.equals(new Coordenadas(x, y)));
        assertJugadoresEquals(clienteJuego2.estado.jugadores, clienteJuego1.estado.jugadores);
    }

    @Test
    public void shouldDig() throws Exception {
        ClienteJuego clienteJuego1 = new ClienteJuego(3);
        clienteJuego1.conectar("localhost", puerto);
        ManagerCliente managerCliente = servidorJuego.aceptarConexion();
        clienteJuego1.procesarMensajeServidor();

        // Cambiamos las coordenadas en el servidor y en el cliente a mano
        Jugador jugadorServidor = servidorJuego.estado.jugadores.get(0);
        jugadorServidor.coordenadas = new Coordenadas(1, 1);
        servidorJuego.estado.tesoro = new Coordenadas(0, 0); // Coordenadas distintas
        Jugador jugadorCliente = clienteJuego1.estado.jugadores.get(0);
        jugadorCliente.coordenadas = new Coordenadas(1, 1);
        clienteJuego1.estado.tesoro = new Coordenadas(0, 0); // Coordenadas distintas

        ClienteJuego clienteJuego2 = new ClienteJuego(3);
        clienteJuego2.conectar("localhost", puerto);
        servidorJuego.aceptarConexion();
        clienteJuego2.procesarMensajeServidor(); // Añade al jugador existente
        clienteJuego2.procesarMensajeServidor(); // Añade al jugador nuevo
        clienteJuego1.procesarMensajeServidor(); // Añade al jugador nuevo

        System.setIn(new ByteArrayInputStream("Q".getBytes()));
        clienteJuego1.procesarInput();
        managerCliente.procesarMensajeCliente();
        clienteJuego1.procesarMensajeServidor();
        // El otro cliente tambien recibe la actualizacion de buscados
        clienteJuego2.procesarMensajeServidor();

        assertEquals(clienteJuego1.estado.buscadas.size(), 1);
        assertEquals(clienteJuego2.estado.buscadas.size(), 1);
        assertEquals(servidorJuego.estado.buscadas.size(), 1);
        assertTrue(clienteJuego1.estado.buscadas.get(0).equals(clienteJuego2.estado.buscadas.get(0)));
        assertTrue(servidorJuego.estado.buscadas.get(0).equals(clienteJuego2.estado.buscadas.get(0)));
    }

    @Test
    public void shouldEnd() throws Exception {
        ClienteJuego clienteJuego1 = new ClienteJuego(3);
        clienteJuego1.conectar("localhost", puerto);
        ManagerCliente managerCliente = servidorJuego.aceptarConexion();
        clienteJuego1.procesarMensajeServidor();

        // Cambiamos las coordenadas en el servidor y en el cliente a mano
        Jugador jugadorServidor = servidorJuego.estado.jugadores.get(0);
        jugadorServidor.coordenadas = new Coordenadas(1, 1);
        servidorJuego.estado.tesoro = new Coordenadas(1, 1); // Coordenadas iguales
        Jugador jugadorCliente = clienteJuego1.estado.jugadores.get(0);
        jugadorCliente.coordenadas = new Coordenadas(1, 1);
        clienteJuego1.estado.tesoro = new Coordenadas(1, 1); // Coordenadas iguales

        ClienteJuego clienteJuego2 = new ClienteJuego(3);
        clienteJuego2.conectar("localhost", puerto);
        servidorJuego.aceptarConexion();
        clienteJuego2.procesarMensajeServidor(); // Añade al jugador existente
        clienteJuego2.procesarMensajeServidor(); // Añade al jugador nuevo
        clienteJuego1.procesarMensajeServidor(); // Añade al jugador nuevo

        System.setIn(new ByteArrayInputStream("Q".getBytes()));
        clienteJuego1.procesarInput();
        managerCliente.procesarMensajeCliente();
        clienteJuego1.procesarMensajeServidor();
        // El otro cliente tambien recibe la actualizacion de buscados
        clienteJuego2.procesarMensajeServidor();

        assertTrue(servidorJuego.estado.estaTerminado());
        assertTrue(clienteJuego1.estado.estaTerminado());
        assertTrue(clienteJuego2.estado.estaTerminado());
    }

}

