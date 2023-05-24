package com.classify.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    int CODE_ALBUM = 001;
    int CODE_CAMERA = 002;

    Button mButtonAlbum;
    Button mButtonCamera;
    Button mButtonRealtime;

    Uri camerafileUri;

    ImageView imageView;
    Bitmap bitmap;

    private final String[] REQUIRED_PERMISSIONS = new String[] {"android.permission.CAMERA"};
    private int REQUEST_CODE_PERMISSION = 101;

    private boolean checkPermissions(){
        for (String permission: REQUIRED_PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonAlbum = (Button) findViewById(R.id.button_album);
        mButtonCamera = (Button) findViewById(R.id.button_camera);
        mButtonRealtime = (Button) findViewById(R.id.button_realtime);

        imageView = (ImageView) findViewById(R.id.imageView_logo2);

        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("img.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);

        mButtonAlbum.setOnClickListener(this);
        mButtonRealtime.setOnClickListener(this);
        mButtonCamera.setOnClickListener(this);

        if(!checkPermissions()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_album:
                Toast.makeText(this,"打开相册",Toast.LENGTH_SHORT).show();
                Intent chooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooseIntent.setType("image/*");
                chooseIntent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivity(chooseIntent);
                startActivityForResult(chooseIntent, CODE_ALBUM);

                break;
            case R.id.button_camera:
                Toast.makeText(this,"打开相机",Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 给拍摄的图片指定储存位置
                String f = System.currentTimeMillis()+".jpg";
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),f);
                camerafileUri = FileProvider.getUriForFile(MainActivity.this, getPackageName()+".fileprovider",file);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, camerafileUri);
                startActivityForResult(cameraIntent, CODE_CAMERA);
                break;

            case R.id.button_realtime:
                Toast.makeText(this,"实时检测",Toast.LENGTH_SHORT).show();
                Intent realtimeIntent = new Intent(MainActivity.this, LiveActivity.class);
                startActivity(realtimeIntent);
                break;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,  resultCode,  data);
        Intent detect_result_activity = new Intent(this,DetectResultActivity.class);
        Bitmap mBitmap;

        if (requestCode == CODE_ALBUM){
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                DataService instance = DataService.getInstance();
                instance.setmBitmap(mBitmap);

                setResult(Activity.RESULT_OK, detect_result_activity);
                startActivity(detect_result_activity);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        else if (requestCode == CODE_CAMERA){

            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), camerafileUri);

                DataService instance = DataService.getInstance();
                instance.setmBitmap(mBitmap);

                setResult(Activity.RESULT_OK, detect_result_activity);
                startActivity(detect_result_activity);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}