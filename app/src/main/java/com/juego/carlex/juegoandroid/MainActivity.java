package com.juego.carlex.juegoandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Menu chustero para mostrar el high score y que parezca una app tó pro
 */
public class MainActivity extends AppCompatActivity {

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.textView);

        Button b1=findViewById(R.id.button3);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(MainActivity.this, ActividadJuego.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        //Toast.makeText(this, "Hola", Toast.LENGTH_LONG).show();
        SharedPreferences sh = getSharedPreferences("ContadorPuntos", Context.MODE_PRIVATE);
        int i = sh.getInt("puntos", 0);
        tv.setText("High score: " + String.valueOf(i));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.finishAffinity();
    }
}
