package com.example.was;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ImageButton home = (ImageButton)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),"홈으로 이동", Toast.LENGTH_SHORT).show();
                Intent intent_home = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent_home);
            }
        });

        final TextView helpContent = (TextView) findViewById(R.id.helpContent);

        try {
            InputStream inputS = getResources().openRawResource(R.raw.help_text);
            byte[] txt = new byte[inputS.available()];
            inputS.read(txt);
            helpContent.setText(new String(txt));
            inputS.close();
        } catch (IOException e){ }
    }
}