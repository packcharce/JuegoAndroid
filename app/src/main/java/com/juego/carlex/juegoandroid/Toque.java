package com.juego.carlex.juegoandroid;

/**
 * Created by Carlex on 20/02/2018.
 */

class Toque {
    public int x; //coordenada x del toque
    public int y; //coordenada y
    public int index; //indice del pointer
    Toque(int mIndex, int mX, int mY){
        index=mIndex;
        x=mX;
        y=mY;
    }
}
