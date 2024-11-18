package es.uva.sockets;

public class Coordenadas {
    private int x;
    private int y;

    public Coordenadas(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public Coordenadas mover(Direccion dir){
        //TODO: Devolver unas coordenadas movidas según direccion
        //DONE

        switch (dir){
            case UP -> this.y-=1;
            case DOWN -> this.y+=1;
            case LEFT -> this.x-=1;
            case RIGHT -> this.x+=1;
        }
        return this;
    }

    public boolean equals(Coordenadas otras) {
        return (this.x == otras.x) && (this.y == otras.y);
    }
}
