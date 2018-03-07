package com.juego.carlex.juegoandroid;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

/**
 * Clase con lo necesario para dibujar y controlar los asteroides
 */

public class Asteroide {

    // Pal debugger
    private static final String TAG = Asteroide.class.getSimpleName();

    float posX, posY;

    private float velocidad;

    private double direccion_horizontal;
    private double direccion_vertical;
    private double calcDireccion;

    private Juego juego;

    Asteroide(Juego j){
        juego=j;

        calcDireccion=Math.random();
        CalculaCoordenadas();
        switch (juego.dificultad){
            case 0:
                calculaDireccionEasy();
                break;
            case 1:
            case 2:
                calculaDireccionMedio();
                break;
        }

    }

    /**
     * Metodo que calcula la direccion del asteroide
     */
    private void calculaDireccionMedio(){
        double modulo= Math.abs(Math.sqrt((juego.posX_planeta - posX)*(juego.posX_planeta - posX)+(juego.posY_planeta - posY)*(juego.posY_planeta - posY)));
        direccion_horizontal = (juego.posX_planeta - posX)/modulo;
        direccion_vertical = (juego.posY_planeta - posY)/modulo;
    }
    private void calculaDireccionEasy(){
        // Direccion: abajo a la derecha
        if(calcDireccion<0.25){
            direccion_vertical=(float)Math.random();
            direccion_horizontal=(float)Math.random();
        }
        // Direccion: arriba a la derecha
        else if (calcDireccion<0.5){
            direccion_vertical=(float)Math.random()*(-1);
            direccion_horizontal=(float)Math.random();
        }
        // Direccion: abajo a la izquierda
        else if (calcDireccion<0.75){
            direccion_vertical=(float)Math.random();
            direccion_horizontal=(float)Math.random()*(-1);
        }
        // Direccion: arriba a la izquierda
        else{
            direccion_vertical=(float)Math.random()*(-1);
            direccion_horizontal=(float)Math.random()*(-1);
        }
    }

    /**
     * Metodo que calcula la posicion de salida del
     * asteroide por fuera de la pantalla
     */

    private void CalculaCoordenadas() {
        Random r = new Random();
        velocidad=r.nextInt(10)+2;

        // Direccion: abajo a la derecha
        if(calcDireccion<0.25){
            // Spawnea a la izq de la pantalla
            if(calcDireccion<0.125){
                posX=-ancho();
                posY=r.nextInt(juego.altoPantalla);
            }
            // Spawnea arriba
            else{
                posX=r.nextInt(juego.anchoPantalla);
                posY=-alto();
            }
        }
        // Direccion: arriba a la derecha
        else if (calcDireccion<0.5){
            // Spawnea a la izq de la pantalla
            if(calcDireccion<0.375){
                posX=-ancho();
                posY=r.nextInt(juego.altoPantalla);
            }
            // Spawnea abajo
            else{
                posX=r.nextInt(juego.anchoPantalla);
                posY=alto()+juego.altoPantalla;
            }
        }
        // Direccion: abajo a la izquierda
        else if (calcDireccion<0.75){
            //Spawnea a la dcha
            if(calcDireccion<0.625){
                posX=ancho()+juego.anchoPantalla;
                posY=r.nextInt(juego.altoPantalla);
            }
            // Spawnea arriba
            else {
                posX = r.nextInt(juego.anchoPantalla);
                posY = -alto();
            }
        }
        // Direccion: arriba a la izquierda
        else{
            // Spawnea abajo
            if(calcDireccion<0.875){
                posX=r.nextInt(juego.anchoPantalla);
                posY=alto()+juego.altoPantalla;
            }
            // Spawnea a la dcha
            else{
                posX=ancho()+juego.anchoPantalla;
                posY=r.nextInt(juego.altoPantalla);
            }
        }
    }

    /**
     * Metodo que actualiza las coordenadas del asteroide en el plano
     */
    void actualizaCoordenadas(){
        if(juego.dificultad == 2)
            calculaDireccionMedio();
        posX+=direccion_horizontal*velocidad;
        posY+=direccion_vertical*velocidad;
        //Log.i(TAG, "Posiciones: " +posX+", "+posY);
    }

    /**
     * Metodo que controla si un asteroide se ha salido del los limites, para borrarlo
     *
     * @return
     */
    boolean fueraDeBordes(){
        boolean fuera=false;
        if(posX<=(-ancho()-1))
            fuera=true;
        if(posX>=(juego.anchoPantalla+ancho()+1))
            fuera=true;
        if(posY<=(-alto()-1))
            fuera=true;
        if(posY>=(juego.altoPantalla+alto()+1))
            fuera=true;
        return fuera;
    }

    void dibujar(Canvas c, Paint p){
        c.drawBitmap(juego.asteroide, posX, posY, p);
    }

    int ancho(){
        return juego.asteroide.getWidth();
    }

    int alto(){
        return juego.asteroide.getHeight();
    }
}
