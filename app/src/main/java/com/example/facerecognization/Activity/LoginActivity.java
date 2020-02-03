package com.example.facerecognization.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.facerecognization.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @OnClick(R.id.faceSignIn)
    public void FaceSignIn()
    {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("class","facesignin");
        startActivity(intent);
    }

    @OnClick(R.id.faceSignUp)
    public void FaceSignUp()
    {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("class","facesignup");
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }
}
