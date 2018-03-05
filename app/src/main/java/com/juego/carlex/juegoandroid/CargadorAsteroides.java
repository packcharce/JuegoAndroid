package com.juego.carlex.juegoandroid;

/**
 * Clase hilo que solo crea los asteroides y los guarda en un array
 * para aliviar el peso del hilo principal
 */

class CargadorAsteroides extends Thread {

    protected boolean areAsteroidesCargados=false;
    private int numAstCargados = 0;
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
