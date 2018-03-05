package com.juego.carlex.juegoandroid;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Clase hilo principal que controla el juego
 */

class BucleJuego extends Thread {

    // Frames por segundo deseados
    public final static int 	MAX_FPS = 30;

    // Máximo número de frames saltados
    private final static int	MAX_FRAMES_SALTADOS = 5;

    // El periodo de frames
    private final static int	TIEMPO_FRAME = 1000 / MAX_FPS;
    private static final String TAG = Juego.class.getSimpleName();
    public CargadorAsteroides ca;
    private Juego juego;
    private boolean ejecutandose=true;
    private SurfaceHolder surfaceHolder;

    /**
     * Constructor que llama al creador de asteroides.
     * Magia de hilos que parece que funciona
     *
     * @param sh
     * @param s
     */
    BucleJuego(SurfaceHolder sh, Juego s){
        juego=s;
        surfaceHolder=sh;

        ca=new CargadorAsteroides(s);
        ca.start();
        while (!ca.areAsteroidesCargados){
            this.interrupt();
        }
        try {
            ca.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void fin() {
        ejecutandose = false;
    }

    /**
     * Metodo principal con el bucle del juego
     */
    @Override
    public void run() {
        Canvas canvas;
        //Log.d(TAG, "Comienza el game loop");


        long tiempoComienzo;		// Tiempo en el que el ciclo comenzó
        long tiempoDiferencia;		// Tiempo que duró el ciclo
        int tiempoDormir;		// Tiempo que el thread debe dormir (<0 si vamos mal de tiempo)
        int framesASaltar;	// número de frames saltados

        tiempoDormir = 0;

        while (ejecutandose) {
            canvas = null;
            // bloquear el canvas para que nadie más escriba en el
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    tiempoComienzo = System.currentTimeMillis();
                    framesASaltar = 0;	// resetear los frames saltados
                    // Actualizar estado del juego
                    juego.actualizar();
                    // renderizar la imagen
                    juego.renderizar(canvas);
                    // Calcular cuánto tardó el ciclo
                    tiempoDiferencia = System.currentTimeMillis() - tiempoComienzo;
                    // Calcular cuánto debe dormir el thread antes de la siguiente iteración
                    tiempoDormir = (int)(TIEMPO_FRAME - tiempoDiferencia);

                    if (tiempoDormir > 0) {
                        // si sleepTime > 0 vamos bien de tiempo
                        try {
                            // Enviar el thread a dormir
                            // Algo de batería ahorramos
                            Thread.sleep(tiempoDormir);
                        } catch (InterruptedException e) {}
                    }

                    while (tiempoDormir < 0 && framesASaltar < MAX_FRAMES_SALTADOS) {
                        // Vamos mal de tiempo: Necesitamos ponernos al día
                        juego.actualizar(); // actualizar si rendering
                        tiempoDormir += TIEMPO_FRAME;	// actualizar el tiempo de dormir
                        framesASaltar++;
                    }


                }
            } finally {
                // si hay excepción desbloqueamos el canvas
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            //Log.d(TAG, "Nueva iteración!"+ejecutandose);
        }
    }
}
