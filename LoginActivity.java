package com.classify.myapplication;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent main_activity = new Intent(this,MainActivity.class);
        startActivity(main_activity);
        super.onCreate(savedInstanceState);

    }
    public void onClick(View v)
    {
        Intent main_activity = new Intent(this,MainActivity.class);
        startActivity(main_activity);
    }
}