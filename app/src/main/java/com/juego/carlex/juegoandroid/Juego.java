package com.juego.carlex.juegoandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Clase que desarrolla la logica del juego y la renderizacion
 */

class Juego extends SurfaceView implements SurfaceHolder.Callback, SurfaceView.OnTouchListener {
    // Pal debugger
    private static final String TAG= Juego.class.getSimpleName();
    // Botones en pantalla
    private final int ROTAR_IZQ = 0;
    private final int ROTAR_DCHA = 1;
    private final int MOVER = 2;
    private final int CARGA = 3;
    private float velocidad_planeta;
    private final int enemigos_minuto = 20;
    private final Short AJUSTE_BITMAP_EXPLOSION = 192;
    private final int VELOCIDAD_ROTACION = 10;
    public byte dificultad;

    /**
     * Metodo que spawnea los rayos del poder 'carga' y los pone direccion
     * [0] imagen, [1] posX, [2] posY, [3] angulo rayo
     */
    private final int NUMRAYOS = 36, VELOCIDAD_RAYOS = 5;
    public BucleJuego bucle;
    public int anchoPantalla;
    public int altoPantalla;
    public int TOTAL_ASTEROIDES = 2000;
    protected Asteroide[] bufferAsteroides = new Asteroide[TOTAL_ASTEROIDES];
    int NIVEL = 1;
    boolean hayToque = false;
    Boton controles[]=new Boton[4];

    //Asteroides
    Bitmap asteroide;

    // Explosion
    Bitmap explosion;
    Explosion exp;
    ArrayList<Explosion> listaExplosiones = new ArrayList<>();

    // Planeta
    Bitmap planeta;
    float posX_planeta;
    float posY_planeta;
    int angulo_planeta=270, ang_bitmap_planeta=0;

    // PowerUp activos
    // 0:WIFI 1:CARGA
    PowerUp[] listaPoderes= new PowerUp[2];
    Context c;
    private SurfaceHolder holder;
    private Activity actividad;

    // Array de toques de pantalla
    private ArrayList<Toque> toques = new ArrayList<>();
    private int frames_para_nuevo_asteroide = 0;
    private int asteroides_creados = 0;
    private int asteroides_destruidos = 0;

    // Fin del juego
    private boolean derrota = false;

    // Lista Asteroides
    private ArrayList<Asteroide> listaAsteroides = new ArrayList<>();
    private boolean isCargaUsada = false;

    private int auxCuenta = 0;
    private Bitmap rayos;
    private double posRayos[][] = new double[NUMRAYOS][3];
    /**
     * Receptor de señal de que el movil esta conectado a la corriente, para activar el powerup
     */
    private BroadcastReceiver cargaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
                if (isCharging) {
                    anhadePoder(1);
                }
            } catch (Exception e) {
            }
        }
    };
    /**
     * Receptor de señal de que el movil tien el WIFI activo, para activar el powerup
     */
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
                    if (listaPoderes[0] != null)
                        listaPoderes[0].actualizaSkin(-1);
                    listaPoderes[0] = null;
                }
                break;

                case WifiManager.WIFI_STATE_DISABLING: {
                }
                break;
                case WifiManager.WIFI_STATE_UNKNOWN: {
                }
                break;

            }
        }
    };

    public Juego(Activity context){
        super(context);
        actividad=context;
        holder=getHolder();
        holder.addCallback(this);
        c = context;

        // Calcula y rellena las variables anchoPantalla y altoPantalla
        CalculaTamanioPantalla();

        // Carga de bitmaps
        // Cargar planeta
        planeta = BitmapFactory.decodeResource(getResources(), R.drawable.tierra);

        // Cargar Explosion
        explosion = BitmapFactory.decodeResource(getResources(), R.drawable.explosion);

        // Posicion inicial de la nave
        posX_planeta = anchoPantalla / 2 - planeta.getWidth() / 2;
        posY_planeta = altoPantalla / 2 - planeta.getHeight() / 2;

        // Cargar los botones y los enemigos
        CargaAsteroides();
        CargaControles();


        // Crear receiver de los poderes
        c.registerReceiver(WifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        c.registerReceiver(cargaReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Listener de pantalla
        setOnTouchListener(this);

    }
    public void setVelocidad_planeta(float velocidad_planeta) {
        this.velocidad_planeta = velocidad_planeta;
    }

    /**
     * Calcula el alto y ancho de la pantalla para rellenar las variables
     */
    private void CalculaTamanioPantalla() {
        if(Build.VERSION.SDK_INT > 13) {
            Display display = actividad.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            anchoPantalla = size.x;
            altoPantalla = size.y;
        } else{
            Display display = actividad.getWindowManager().getDefaultDisplay();
            anchoPantalla = display.getWidth();  // deprecated
            altoPantalla = display.getHeight();  // deprecated
        }
        //Log.i(Juego.class.getSimpleName(), "alto:" + altoPantalla + "," + "ancho:" + anchoPantalla);
    }

    /**
     * Carga el bitmap de los asteroides y el intervalo de aparicion
     */
    private void CargaAsteroides() {
        frames_para_nuevo_asteroide = BucleJuego.MAX_FPS * 60 / enemigos_minuto;
        asteroide= BitmapFactory.decodeResource(getResources(), R.drawable.asteroide);
    }

    /**
     * Crea los botones de control de la nave y de los poderes
     */
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

        // Boton Carga Electrica
        controles[CARGA]=new Boton(getContext(), anchoPantalla - controles[0].ancho(), controles[0].posY-controles[0].alto());
        controles[CARGA].Cargar(R.drawable.boton_rayo);
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
        // Cerrar el thread y esperar que acabe
        boolean retry = true;
        while (retry) {
            try {

                // Guarda el high score de la partida
                Context c= getContext();
                SharedPreferences sharedPref = c.getSharedPreferences("ContadorPuntos", Context.MODE_PRIVATE);
                int i = sharedPref.getInt("puntos", 0);
                if (i < asteroides_destruidos) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("puntos", asteroides_destruidos);
                    editor.apply();
                }
                fin();
                bucle.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * Metodo que aniade poderes a la lista de poderes
     * en origen era para añadir indefinidos poderes
     *
     * @param tipo: el numero en el array del poderes
     */
    public void anhadePoder(int tipo){
        listaPoderes[tipo]= new  PowerUp(this, tipo, getContext());
    }

    /**
     * Comprueba si hay poder activo (escudos)
     */
    private boolean compruebaPoderes(){
        // Aqui pierde el jugador, no tiene poderes activos
        boolean isPowered=false;
        for(byte i = 0; i<listaPoderes.length; i++)
            if(listaPoderes[i] != null && i != 1)
                isPowered=true;
        if(!isPowered) {
            muerteJugador();
        }
        return isPowered;
    }

    /**
     * Ajusta el bitmap de la explosion del jugador al perder
     */
    private void muerteJugador(){
        exp = new Explosion(this, posX_planeta - AJUSTE_BITMAP_EXPLOSION, posY_planeta - AJUSTE_BITMAP_EXPLOSION);
        derrota = true;
    }

    /**
     * Metodo que destruye los asteroides que toquen los rayos al usar el poder
     */
    public void destruyeTodosAsteroides(){
        if(listaPoderes[1] != null) {
            spawnRayos();
            listaPoderes[1].duracion--;
            listaPoderes[1] = null;
        }
    }

    /**
     * Este método actualiza el estado del juego. Contiene la lógica del videojuego
     * generando los nuevos estados y dejando listo el sistema para un repintado.
     */
    public void actualizar() {
        if (!derrota && bucle.ca.areAsteroidesCargados) {

            // Rotacion del planeta
            if (controles[ROTAR_IZQ].pulsado) {
                angulo_planeta = (angulo_planeta - VELOCIDAD_ROTACION) % 360;
                ang_bitmap_planeta = (ang_bitmap_planeta - VELOCIDAD_ROTACION) % 360;
            }
            if (controles[ROTAR_DCHA].pulsado) {
                angulo_planeta = (angulo_planeta + VELOCIDAD_ROTACION) % 360;
                ang_bitmap_planeta = (ang_bitmap_planeta + VELOCIDAD_ROTACION) % 360;

            }
            // Moverse
            if (controles[MOVER].pulsado) {
                posX_planeta += Math.cos(Math.toRadians(angulo_planeta)) * velocidad_planeta;
                posY_planeta += Math.sin(Math.toRadians(angulo_planeta)) * velocidad_planeta;
            }
            // Estallar carga
            if (controles[CARGA].pulsado && listaPoderes[1] != null) {
                isCargaUsada = true;
                destruyeTodosAsteroides();
            }
            //Log.i(TAG, "Angulo: "+angulo_planeta);


            // Llama a un nuevo grupo de asteroides a pantalla desde el array buffer
            // cada ciertos frames
            if (frames_para_nuevo_asteroide == 0) {
                CrearNuevoAsteroide();
                frames_para_nuevo_asteroide = 100;
            }
            frames_para_nuevo_asteroide--;

            // Los rayos del powerup se mueven
            if (isCargaUsada)
                mueveRayos();

            // Colisiones
            for (Iterator<Asteroide> it_asteroide = listaAsteroides.iterator(); it_asteroide.hasNext(); ) {
                Asteroide a = it_asteroide.next();

                // Los asteroides se mueven
                a.actualizaCoordenadas();
                if (a.fueraDeBordes()) {
                    try {
                        it_asteroide.remove();
                        asteroides_destruidos++;
                    } catch (Exception e) {
                    }
                } else if (ColisionNave(a)) {

                    // Si tiene poder activo (escudos como el wifi)
                    if (compruebaPoderes()) {

                        for (byte i = 0; i < listaPoderes.length; i++) {
                            if (listaPoderes[i] != null)
                                switch (listaPoderes[i].getTipoPower()) {
                                    // Buscar poder wifi
                                    case 0:
                                        listaPoderes[i].duracion--;
                                        listaExplosiones.add(new Explosion(this, a.posX - AJUSTE_BITMAP_EXPLOSION, a.posY - AJUSTE_BITMAP_EXPLOSION));
                                        it_asteroide.remove();
                                        asteroides_destruidos++;
                                        break;
                                    default:
                                        break;
                                }
                        }
                    }
                }

                // Caso en el que el usuario haya usado el poder carga
                if (isCargaUsada)
                    for (byte i = 0; i < NUMRAYOS; i++)
                        if (colisionRayo(a, posRayos[i][0], posRayos[i][1])) {
                            try {
                                it_asteroide.remove();
                            } catch (IllegalStateException is) {
                            }
                            listaExplosiones.add(new Explosion(this, a.posX - AJUSTE_BITMAP_EXPLOSION, a.posY - AJUSTE_BITMAP_EXPLOSION));
                            asteroides_destruidos++;
                        }
            }

            // Si el usuario pierde un powerup, se devuelve al bitmap origen
            for (byte i = 0; i < listaPoderes.length; i++)
                if (listaPoderes[i] != null && listaPoderes[i].duracion <= 0) {
                    listaPoderes[i].actualizaSkin(-1);
                    listaPoderes[i] = null;
                }

            // Sistema de niveles
            if (asteroides_creados == 10 * NIVEL - 10) {
                NIVEL++;
            }
        }
    }

    /**
     * Metodo que calcula si la nave colisiona con asteroide
     * @param e: asteroide a comprobar
     * @return Si hay colision
     */
    public boolean ColisionNave(Asteroide e){
        Rect planet = new Rect((int)posX_planeta+20, (int)posY_planeta+20, (int)posX_planeta-20 + planeta.getWidth(), (int)posY_planeta-20 + planeta.getHeight());
        Rect aster = new Rect((int)e.posX, (int)e.posY, (int)e.posX + asteroide.getWidth(), (int)e.posY + asteroide.getHeight());

        // TODO  true
        return Rect.intersects(planet, aster);
    }

    /**
     * Metodo que comprueba si algun rayo colisiona con algun asteroide
     * @param e: el asteroide con el que puede colisionar
     * @param posXRayo: posicion X actual del rayo
     * @param posYRayo: posicion Y actual del rayo
     * @return: si hay colision
     */
    private boolean colisionRayo(Asteroide e, double posXRayo, double posYRayo){
        Rect planet = new Rect((int)posXRayo, (int)posYRayo, (int)posXRayo + rayos.getWidth(), (int)posYRayo + rayos.getHeight());
        Rect aster = new Rect((int)e.posX, (int)e.posY, (int)e.posX + asteroide.getWidth(), (int)e.posY + asteroide.getHeight());

        return Rect.intersects(planet, aster);
    }

    /**
     * Metodo magico que extrae 40+NIVEL asteroides del buffer
     * y a su vez borra las explosiones existentes en pantalla
     */
    private void CrearNuevoAsteroide() {
        if(asteroides_creados - asteroides_destruidos <=40+NIVEL) {
            for (int i = auxCuenta; i < auxCuenta+10 && auxCuenta < TOTAL_ASTEROIDES; i++) {
                listaAsteroides.add(bufferAsteroides[i]);
                asteroides_creados++;

                if (listaExplosiones.size() > 0)
                    listaExplosiones.remove(listaExplosiones.size() - 1);
            }
            auxCuenta+= 10;
        }
    }

    private void spawnRayos(){
        for(int i = 0; i<NUMRAYOS; i++){
            rayos = BitmapFactory.decodeResource(getResources(), R.drawable.rayo);
            posRayos[i][0] = posX_planeta + planeta.getWidth()/2;
            posRayos[i][1] = posY_planeta + planeta.getHeight()/2;
            posRayos[i][2] = i*10;
        }
    }

    /**
     * Metodo que mueve los rayos cada 10 grados a la redonda
     */
    private void mueveRayos(){
        for(int i = 0; i<NUMRAYOS; i++){
            posRayos[i][0] = posRayos[i][0] + Math.cos(Math.toRadians(posRayos[i][2])) * VELOCIDAD_RAYOS;
            posRayos[i][1] = posRayos[i][1] + Math.sin(Math.toRadians(posRayos[i][2])) * VELOCIDAD_RAYOS;

            if(posRayos[i][0]<=(-rayos.getWidth()-1))
                isCargaUsada=false;
            if(posRayos[i][0]>=(this.anchoPantalla+rayos.getWidth()+1))
                isCargaUsada=false;
            if(posRayos[i][1]<=(-rayos.getHeight()-1))
                isCargaUsada=false;
            if(posRayos[i][1]>=(this.altoPantalla+rayos.getHeight()+1))
                isCargaUsada=false;
        }
    }

    /**
     * Este método dibuja el siguiente paso de la animación correspondiente
     */
    public void renderizar(Canvas canvas) {
        if (canvas != null && bucle.ca.areAsteroidesCargados) {
            canvas.drawColor(Color.BLACK);
            //Pinceles
            Paint myPaint = new Paint();
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setColor(Color.WHITE);
            myPaint.setTextSize(30);

            Paint myPaint2 = new Paint();
            myPaint2.setStyle(Paint.Style.FILL);
            myPaint2.setTextSize(50);

            // Aqui se calcula y redibuja la nave con respecto al angulo que deba tener
            Matrix matrix = new Matrix();
            matrix.preRotate(ang_bitmap_planeta, planeta.getWidth()/2, planeta.getHeight()/2);
            if (!derrota) {
                matrix.postTranslate(posX_planeta, posY_planeta);
                canvas.drawBitmap(planeta, matrix, null);
            }

            // Dibujar asteroides
            for (Asteroide a : listaAsteroides)
                a.dibujar(canvas, myPaint);

            // Dibujar rayos
            if(isCargaUsada)
                for(byte i = 0; i<NUMRAYOS; i++){
                    canvas.drawBitmap(rayos, (float)posRayos[i][0], (float)posRayos[i][1], myPaint);
                }

            // Explosion de nave
            if(exp != null)
                exp.Dibujar(canvas, myPaint);

            // Explosion de asteroides
            for(Explosion e: listaExplosiones)
                e.Dibujar(canvas, myPaint);

            // Controles
            myPaint.setAlpha(200);
            for (int i = 0; i < controles.length-1; i++) {
                controles[i].dibujar(canvas, myPaint);
            }
            if(listaPoderes[1] != null)
                controles[3].dibujar(canvas, myPaint);

            // Marcador con informacion de los asteroides esquivados y los poderes activos
            canvas.drawText("Asteroides esquivados: "+asteroides_destruidos,0,30, myPaint);
            for(byte i = 0; i<listaPoderes.length; i++) {
                if (listaPoderes[i] != null)
                    canvas.drawText("Usos de " + listaPoderes[i].getNombre() + ": " + listaPoderes[i].duracion, 0, 60 + 30 * i, myPaint);
            }

            // Cartel de derrota
            if (derrota) {
                myPaint.setAlpha(0);
                myPaint.setColor(Color.RED);
                myPaint.setTextSize(anchoPantalla / 10);
                canvas.drawText("DERROTA!!", anchoPantalla/4, altoPantalla / 2 - 200, myPaint);
                myPaint.setTextSize(anchoPantalla / 20);
                canvas.drawText("Uyyyy, pls try again!!!", anchoPantalla/4, altoPantalla / 2 + 100, myPaint);
            }

        }
    }

    /**
     * Metodo que captura los toques en pantalla
     * @param view
     * @param event
     * @return
     */
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
                for(int i=0; i<controles.length; i++)
                    controles[i].comprueba_pulsado(x,y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                synchronized(this) {
                    toques.remove(index);
                }

                //se comprueba si se ha soltado el botón
                for(int i = 0; i<controles.length; i++)
                    controles[i].comprueba_soltado(toques);
                break;

            case MotionEvent.ACTION_UP:
                synchronized(this) {
                    toques.clear();
                }
                hayToque=false;
                //se comprueba si se ha soltado el botón
                for(int i = 0; i<controles.length; i++)
                    controles[i].comprueba_soltado(toques);
                break;
        }
        return true;
    }

    /**
     * Metodo que manda señal al bucle que se ha perdido
     */
    public void fin() {
        c.unregisterReceiver(cargaReceiver);
        c.unregisterReceiver(WifiReceiver);
        bucle.fin();

    }

}

