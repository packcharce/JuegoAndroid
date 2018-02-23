package com.juego.carlex.juegoandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;


/**
 * Created by Carlex on 21/02/2018.
 */

public class PowerUp {

    private float posX, posY;
    Bitmap powerUp;
    private String nombre;
    private int tipoPower;
    private Juego juego;
    private static final int WIFI=0;
    private static final int CARGA=1;
    public int duracion;
    boolean powerAct;

    private Context cont;

    PowerUp(Juego j, int tipoPower, Context c){
        juego=j;
        powerAct=false;

        posX=juego.posX_planeta;
        posY=juego.posY_planeta;

        this.tipoPower=tipoPower;

        cont=c;
        actualizaSkin(tipoPower);
    }

    public int getTipoPower() {
        return tipoPower;
    }

    public String getNombre() {
        return nombre;
    }

    public void actualizaSkin(int tipo){
        switch (tipo){
            case WIFI:
                juego.planeta= BitmapFactory.decodeResource(cont.getResources(), R.drawable.tierra_wifi);
                duracion=5;
                nombre="WIFI";
                break;
            case CARGA:
                duracion=1;
                nombre="CARGA";
                break;
            default:
                juego.planeta=BitmapFactory.decodeResource(cont.getResources(), R.drawable.tierra);
        }
    }

    public void actualizaCoordenadas(){
        posX=juego.posX_planeta;
        posY=juego.posY_planeta;
    }

    public void dibujar(Canvas c, Paint p){
        c.drawBitmap(powerUp, posX, posY, p);
    }

    public int ancho(){
        return powerUp.getWidth();
    }

    public int alto(){
        return powerUp.getHeight();
    }
}
