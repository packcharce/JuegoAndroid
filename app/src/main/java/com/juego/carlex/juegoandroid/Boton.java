package com.juego.carlex.juegoandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Clase con los metodos necesarios para crear y dibujar los controles
 */

class Boton {
    public boolean pulsado=false;
    public String nombre;
    public int posX;
    public int posY;
    private Bitmap imagen; //imagen del control
    private Context mContexto;

    public Boton(Context context, int x, int y) {
        posX=x;
        posY=y;
        mContexto=context;
    }

    public void dibujar(Canvas canvas, Paint myPaint) {
        canvas.drawBitmap(imagen,posX,posY,myPaint);
    }

    public void comprueba_pulsado(int x, int y) {
        if(x>posX && x<posX+ancho() && y>posY && y<posY+alto()){
            pulsado=true;
        }
    }

    public void comprueba_soltado(ArrayList<Toque> toques) {
        boolean aux=false;
        for(Toque t:toques){
            if(t.x>posX && t.x<posX+ancho() && t.y>posY && t.y<posY+alto()) {
                aux = true;
            }
        }
        if(!aux){
            pulsado=false;
        }
    }

    public void Cargar(int recurso) {
        imagen= BitmapFactory.decodeResource(mContexto.getResources(), recurso);
    }

    public int ancho() {
        return imagen.getWidth();
    }

    public int alto(){
        return imagen.getHeight();
    }
}
