package com.juego.carlex.juegoandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Alumno on 25/01/2018.
 */

public class EjemploBroadCast extends BroadcastReceiver {

    private Juego juego;

    public EjemploBroadCast(){

    }
    public EjemploBroadCast(Juego j){
        juego=j;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
            if(isCharging){
                juego.anhadePoder(1);
            }
        }catch (Exception e){}
    }

}
