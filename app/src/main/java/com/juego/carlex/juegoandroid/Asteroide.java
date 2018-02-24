package com.juego.carlex.juegoandroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

/**
 * Created by Carlex on 20/02/2018.
 */

public class Asteroide extends Thread {

    private static final String TAG = Asteroide.class.getSimpleName();

    public float posX, posY;

    public float velocidad;

    public float direccion_horizontal;
    public float direccion_vertical;
    Double calcDireccion;

    private Juego juego;

    Asteroide(Juego j){
        juego=j;

        this.start();
    }

    @Override
    public void run() {
        calculaDireccion();
        CalculaCoordenadas();
    }

    /**
     * Metodo que calcula la direccion del asteroide
     */
    private void calculaDireccion(){
        calcDireccion=Math.random();

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
        Random r= new Random();
        velocidad=r.nextInt(10);
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
            else{
                posX=r.nextInt(juego.anchoPantalla);
                posY=-alto();
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

    public void actualizaCoordenadas(){
        posX+=direccion_horizontal*velocidad;
        posY+=direccion_vertical*velocidad;
        Log.i(TAG, "Posiciones: " +posX+", "+posY);
    }

    public boolean fueraDeBordes(){
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

    public void dibujar(Canvas c, Paint p){
        c.drawBitmap(juego.asteroide, posX, posY, p);
    }

    public int ancho(){
        return juego.asteroide.getWidth();
    }

    public int alto(){
        return juego.asteroide.getHeight();
    }
}
