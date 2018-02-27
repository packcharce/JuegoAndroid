package com.juego.carlex.juegoandroid;

/**
 * Created by Carlex on 26/02/2018.
 */

class CargadorAsteroides extends Thread {

    private int numAstCargados=0;
    protected boolean areAsteroidesCargados=false;
    private Juego juego;
    public CargadorAsteroides(Juego j){
        juego=j;
    }

    @Override
    public void run() {
        while (numAstCargados<juego.TOTAL_ASTEROIDES){
            synchronized (juego.bufferAsteroides) {
                juego.bufferAsteroides[numAstCargados] = new Asteroide(juego);
            }
            numAstCargados++;
        }
        areAsteroidesCargados=true;
    }
}
