package es.uva.sockets;

public class Jugador {
    public final int id;
    public Coordenadas coordenadas;

    public Jugador(int id, Coordenadas coordenadas) {
        this.id = id;
        this.coordenadas = coordenadas;
    }

    public char getChar(){
        return (char) ('A' + (id - 1) % 26);
    }
}
