package com.example.was;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt  = (Button)findViewById(R.id.bt);
        Button map = (Button)findViewById(R.id.map);
        Button help = (Button)findViewById(R.id.help);

        bt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent_bt = new Intent(getApplicationContext(),bt.class);
                startActivity(intent_bt);
            }
        });


        map.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent_map = new Intent(getApplicationContext(),map.class);
                startActivity(intent_map);
            }
        });

        help.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent_help = new Intent(getApplicationContext(),help.class);
                startActivity(intent_help);
            }
        });
    }
}