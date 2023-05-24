package com.classify.myapplication;

import android.graphics.Bitmap;

public class DataService {

    public Bitmap mBitmap;

    private static  DataService dataService;

    public Bitmap getmBitmap(){ return mBitmap;}

    public void setmBitmap(Bitmap mBitmap){this.mBitmap = mBitmap;}

    public static DataService getInstance(){
        if (dataService == null){
            synchronized (DataService.class){
                if (dataService == null){
                    dataService = new DataService();
                }
            }
        }
        return dataService;
    }


}
