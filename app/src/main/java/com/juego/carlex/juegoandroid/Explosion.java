package com.juego.carlex.juegoandroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Carlex on 20/02/2018.
 */

class Explosion {

    private int ancho_sprite;
    private int alto_sprite;
    public float coordenada_x, coordenada_y; //coordenadas donde se dibuja el control
    private Juego juego;
    private int estado;


    public Explosion(Juego j, float x, float y){
        coordenada_x=x;
        coordenada_y=y;
        juego=j;
        ancho_sprite=juego.explosion.getWidth()/17;
        alto_sprite=juego.explosion.getHeight();
        estado=15;


    }

    public void Dibujar(Canvas canvas, Paint myPaint) {
        int posicionSprite=estado*ancho_sprite;

        //Calculamos el cuadrado del sprite que vamos a dibujar
        Rect origen = new Rect(posicionSprite, 0, posicionSprite + ancho_sprite,
                alto_sprite);

        //calculamos donde vamos a dibujar la porcion del sprite
        Rect destino = new Rect((int)coordenada_x,(int) coordenada_y,(int) coordenada_x + ancho_sprite,
                (int) coordenada_y + juego.explosion.getHeight());

        canvas.drawBitmap(juego.explosion, origen, destino, myPaint);


    }
}
