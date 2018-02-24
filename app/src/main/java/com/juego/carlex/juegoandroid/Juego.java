package com.juego.carlex.juegoandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Carlex on 20/02/2018.
 */

class Juego extends SurfaceView implements SurfaceHolder.Callback, SurfaceView.OnTouchListener {
    private SurfaceHolder holder;
    public BucleJuego bucle;
    private Activity actividad;


    public int anchoPantalla;
    public int altoPantalla;


    // Pal debugger
    private static final String TAG= Juego.class.getSimpleName();

    // Array de toques
    private ArrayList<Toque> toques= new ArrayList<>();
    boolean hayToque=false;


    // Botones
    private final int ROTAR_IZQ = 0;
    private final int ROTAR_DCHA = 1;
    private final int MOVER = 2;
    private final int CARGA = 3;

    private final float VELOCIDAD_PLANETA = 10;
    Boton controles[]=new Boton[4];


    //Asteroides
    Bitmap asteroide;
    public final int TOTAL_ASTEROIDES=200;
    private int enemigos_minuto=30;
    private int frames_para_nuevo_asteroide=0;
    private int asteroides_creados=0;
    private int asteroides_destruidos=0;

    // Fin del juego
    private boolean derrota=false;

    // Lista Asteroides
    private ArrayList<Asteroide> listaAsteroides=new ArrayList<>();

    // Explosion
    Bitmap explosion;
    Explosion exp;
    ArrayList<Explosion> listaExplosiones = new ArrayList<>();
    private final Short AJUSTE_BITMAP_EXPLOSION = 192;

    // Planeta
    Bitmap planeta;
    float posX_planeta;
    float posY_planeta;
    int angulo_planeta=270, ang_bitmap_planeta=0;
    private final int VELOCIDAD_ROTACION = 5;

    // PowerUp activos
    // 0:WIFI 1:CARGA
    PowerUp[] listaPoderes= new PowerUp[4];

    public Juego(Activity context){
        super(context);
        actividad=context;
        holder=getHolder();
        holder.addCallback(this);

        CalculaTamanioPantalla();

        // Cargar planeta
        /// Cambiar a tierra
        planeta= BitmapFactory.decodeResource(getResources(), R.drawable.tierra);

        // Cargar Explosion
        explosion= BitmapFactory.decodeResource(getResources(), R.drawable.explosion);

        // Posicion inicial de la nave
        posX_planeta = anchoPantalla/2 - planeta.getWidth()/2;
        posY_planeta = altoPantalla/2 - planeta.getHeight()/2;

        // Cargar los botones y los enemigos
        CargaControles();
        CargaAsteroides();

        // Crear receiver
        context.registerReceiver(WifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        context.registerReceiver(cargaReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // poner el listener
        setOnTouchListener(this);

    }

    private void CalculaTamanioPantalla() {
        if(Build.VERSION.SDK_INT > 13) {
            Display display = actividad.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            anchoPantalla = size.x;
            altoPantalla = size.y;
        }
        else{
            Display display = actividad.getWindowManager().getDefaultDisplay();
            anchoPantalla = display.getWidth();  // deprecated
            altoPantalla = display.getHeight();  // deprecated
        }
        Log.i(Juego.class.getSimpleName(), "alto:" + altoPantalla + "," + "ancho:" + anchoPantalla);
    }

    private void CargaAsteroides() {
        frames_para_nuevo_asteroide=bucle.MAX_FPS*60/enemigos_minuto;
        asteroide= BitmapFactory.decodeResource(getResources(), R.drawable.asteroide);
    }

    private void CargaControles() {
        // Boton Rotar izquierda
        controles[ROTAR_IZQ]=new Boton(getContext(), 0, altoPantalla-200);
        controles[ROTAR_IZQ].Cargar(R.drawable.flecha_izda);
        controles[ROTAR_IZQ].nombre="ROTAR IZQ";

        // Boton Rotar derecha
        controles[ROTAR_DCHA]=new Boton(getContext(), controles[0].ancho()+controles[0].posX+5, controles[0].posY);
        controles[ROTAR_DCHA].Cargar(R.drawable.flecha_dcha);
        controles[ROTAR_DCHA].nombre="ROTAR DCHA";

        // Boton Acelerar
        controles[MOVER]=new Boton(getContext(), anchoPantalla - controles[0].ancho(), controles[0].posY);
        controles[MOVER].Cargar(R.drawable.powerr);
        controles[MOVER].nombre="Acelerar";

        // Boton Acelerar
        controles[CARGA]=new Boton(getContext(), anchoPantalla - controles[0].ancho(), controles[0].posY-controles[0].alto());
        controles[CARGA].Cargar(R.drawable.flecha_izda);
        controles[CARGA].nombre="Estallar Carga";
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // Se crea la superficie, se crea el bucle

        // Para interceptar los eventos
        getHolder().addCallback(this);

        // Game loop
        bucle= new BucleJuego(getHolder(), this);

        // Hacer que pueda capturar eventos
        setFocusable(true);

        // Comenzar el bucle
        bucle.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // cerrar el thread y esperar que acabe
        boolean retry = true;
        while (retry) {
            try {
                fin();
                bucle.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

    public void anhadePoder(int tipo){
        listaPoderes[tipo]= new  PowerUp(this, tipo, getContext());
    }

    // Comprueba si hay poder activo (escudos)
    private boolean compruebaPoderes(){
        // Aqui pierde el jugador, no tiene poderes activos
        boolean isPowered=false;
        for(byte i=0; i<listaPoderes.length; i++)
            if(listaPoderes[i] != null && i != 1)
                isPowered=true;
        if(!isPowered) {
            muerteJugador();
        }
        return isPowered;
    }

    private void muerteJugador(){
        exp = new Explosion(this, posX_planeta - AJUSTE_BITMAP_EXPLOSION, posY_planeta - AJUSTE_BITMAP_EXPLOSION);
        derrota = true;
    }

    public void destruyeTodosAsteroides(){
        if(listaPoderes[1] != null) {
            for (Iterator<Asteroide> it_asteroide = listaAsteroides.iterator(); it_asteroide.hasNext(); ) {
                Asteroide a = it_asteroide.next();
                listaExplosiones.add(new Explosion(this, a.posX - AJUSTE_BITMAP_EXPLOSION, a.posY - AJUSTE_BITMAP_EXPLOSION));
            }
            asteroides_destruidos += listaAsteroides.size();
            listaPoderes[1].duracion--;
            listaAsteroides.clear();
            listaPoderes[1] = null;
        }
    }


    /**
     * Este método actualiza el estado del juego. Contiene la lógica del videojuego
     * generando los nuevos estados y dejando listo el sistema para un repintado.
     */


    public void actualizar() {
        if(!derrota){

            // Rotacion del planeta
            if(controles[ROTAR_IZQ].pulsado){
                angulo_planeta -= VELOCIDAD_ROTACION;
                ang_bitmap_planeta -= VELOCIDAD_ROTACION;
            }
            if (controles[ROTAR_DCHA].pulsado){
                angulo_planeta += VELOCIDAD_ROTACION;
                ang_bitmap_planeta += VELOCIDAD_ROTACION;

            }
            // Moverse
            if (controles[MOVER].pulsado){
                posX_planeta += Math.cos(Math.toRadians(angulo_planeta)) * VELOCIDAD_PLANETA;
                posY_planeta += Math.sin(Math.toRadians(angulo_planeta)) * VELOCIDAD_PLANETA;
            }
            // Estallar carga
            if (controles[CARGA].pulsado && listaPoderes[1] != null){
                destruyeTodosAsteroides();
            }
            Log.i(TAG, "Angulo: "+angulo_planeta);
        }



        if(frames_para_nuevo_asteroide == 0){
            CrearNuevoAsteroide();
            frames_para_nuevo_asteroide=bucle.MAX_FPS+60/enemigos_minuto;
        }
        frames_para_nuevo_asteroide--;

        // Los asteroides se mueven
        for(int i=0; i<listaAsteroides.size(); i++){
            listaAsteroides.get(i).actualizaCoordenadas();
            if(listaAsteroides.get(i).fueraDeBordes()){
                try {
                    listaAsteroides.remove(i);
                    asteroides_destruidos++;
                }catch (Exception e){}
            }
        }

        // Colisiones
        for(Iterator<Asteroide> it_asteroide = listaAsteroides.iterator(); it_asteroide.hasNext();){
            Asteroide a = it_asteroide.next();
            if(ColisionNave(a)){

                // Si tiene poder activo (escudos)
                if(compruebaPoderes()) {

                    for(byte i=0; i<listaPoderes.length; i++) {
                        if(listaPoderes[i] != null)
                            switch (listaPoderes[i].getTipoPower()){
                                // Buscar poder wifi
                                case 0:
                                    listaPoderes[i].duracion--;
                                    listaExplosiones.add(new Explosion(this, a.posX - AJUSTE_BITMAP_EXPLOSION, a.posY - AJUSTE_BITMAP_EXPLOSION));
                                    it_asteroide.remove();
                                    asteroides_destruidos++;
                                    break;
                                default:break;
                            }
                    }
                }
            }
        }
        for(byte i=0; i<listaPoderes.length; i++)
            if(listaPoderes[i] != null && listaPoderes[i].duracion <= 0) {
                listaPoderes[i].actualizaSkin(-1);
                listaPoderes[i] = null;
            }
    }

    public boolean ColisionNave(Asteroide e){
        Rect planet = new Rect((int)posX_planeta+20, (int)posY_planeta+20, (int)posX_planeta-20 + planeta.getWidth(), (int)posY_planeta-20 + planeta.getHeight());
        Rect aster = new Rect((int)e.posX, (int)e.posY, (int)e.posX + asteroide.getWidth(), (int)e.posY + asteroide.getHeight());

        if(Rect.intersects(planet, aster))
            return true;
        else
            return false;
    }

    private void CrearNuevoAsteroide() {
        if(TOTAL_ASTEROIDES-asteroides_creados>0) {
            listaAsteroides.add(new Asteroide(this));
            asteroides_creados++;
            if(listaExplosiones.size()>0)
                listaExplosiones.remove(listaExplosiones.size()-1);
        }
    }

    /**
     * Este método dibuja el siguiente paso de la animación correspondiente
     */
    public void renderizar(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            //Pinceles
            Paint myPaint = new Paint();
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setColor(Color.WHITE);
            myPaint.setTextSize(30);

            Paint myPaint2 = new Paint();
            myPaint2.setStyle(Paint.Style.FILL);
            myPaint2.setTextSize(50);

            //Si ha ocurrido un toque en la pantalla "Touch", dibujar un círculo
            if (hayToque) {
                synchronized (this) {
                    for (Toque t : toques) {
                        canvas.drawCircle(t.x, t.y, 100, myPaint);
                    }
                }
            }

            canvas.drawText("Asteroides esquivados: "+asteroides_destruidos,0,30, myPaint);
            for(byte i=0; i<listaPoderes.length; i++) {
                if (listaPoderes[i] != null)
                    canvas.drawText("Usos de " + listaPoderes[i].getNombre() + ": " + listaPoderes[i].duracion, 0, 60 + 30 * i, myPaint);
            }

            Matrix matrix = new Matrix();
            matrix.preRotate(ang_bitmap_planeta, planeta.getWidth()/2, planeta.getHeight()/2);
            if (!derrota) {
                matrix.postTranslate(posX_planeta, posY_planeta);

//--------------------------------------------- Poner poderes activos------------------------------------------
                /*if(listaPoderes[0] != null) {
                    planeta = BitmapFactory.decodeResource(getResources(), R.drawable.tierra_wifi);

                }*/
                canvas.drawBitmap(planeta, matrix, null);


            }

            for (Asteroide a : listaAsteroides)
                a.dibujar(canvas, myPaint);

            // Explosion de nave
            if(exp != null)
                exp.Dibujar(canvas, myPaint);
            // Explosion de asteroides
            for(Explosion e: listaExplosiones)
                e.Dibujar(canvas, myPaint);

            myPaint.setAlpha(200);
            for (int i = 0; i < controles.length; i++) {
                controles[i].dibujar(canvas, myPaint);
            }

            if (derrota) {
                myPaint.setAlpha(0);
                myPaint.setColor(Color.RED);
                myPaint.setTextSize(anchoPantalla / 10);
                canvas.drawText("DERROTA!!", 50, altoPantalla / 2 - 100, myPaint);
                myPaint.setTextSize(anchoPantalla / 20);
                canvas.drawText("La raza humana está condenada!!!!", 50, altoPantalla / 2 + 100, myPaint);
                fin();
            }

        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int index;
        int x,y;

        // Obtener el pointer asociado con la acción
        index = MotionEventCompat.getActionIndex(event);

        x = (int) MotionEventCompat.getX(event, index);
        y = (int) MotionEventCompat.getY(event, index);

        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                hayToque=true;

                synchronized(this) {
                    toques.add(index, new Toque(index, x, y));
                }
                //se comprueba si se ha pulsado
                for(int i=0;i<controles.length;i++)
                    controles[i].comprueba_pulsado(x,y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                synchronized(this) {
                    toques.remove(index);
                }

                //se comprueba si se ha soltado el botón
                for(int i=0;i<controles.length;i++)
                    controles[i].comprueba_soltado(toques);
                break;

            case MotionEvent.ACTION_UP:
                synchronized(this) {
                    toques.clear();
                }
                hayToque=false;
                //se comprueba si se ha soltado el botón
                for(int i=0;i<controles.length;i++)
                    controles[i].comprueba_soltado(toques);
                break;
        }

        return true;
    }

    public void fin() {
        bucle.fin();
        planeta.recycle();
        asteroide.recycle();
    }

    private BroadcastReceiver cargaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
                if(isCharging){
                    anhadePoder(1);
                }
            }catch (Exception e){}
        }
    };

    private BroadcastReceiver WifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int WifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (WifiState) {
                case WifiManager.WIFI_STATE_ENABLED: {
                    anhadePoder(0);
                }
                break;

                case WifiManager.WIFI_STATE_ENABLING: {

                }
                break;
                case WifiManager.WIFI_STATE_DISABLED: {
                    listaPoderes[0]=null;

                }
                break;

                case WifiManager.WIFI_STATE_DISABLING: {
                }
                break;



                case WifiManager.WIFI_STATE_UNKNOWN: {
                }
                break;

            }
        } };

}

