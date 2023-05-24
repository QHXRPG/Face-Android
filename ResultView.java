package com.classify.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import org.pytorch.IValue;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ResultView extends View {

    //定义矩形框和矩形框文字
    private Paint mPaintRectangle; // 用于绘制矩形框的画笔
    Bitmap mybitmap1;
    Bitmap mybitmap2;
    Bitmap mybitmap3;
    Bitmap mybitmap4;
    Bitmap mybitmap5;
    Bitmap mybitmap6;
    double distant;
    float[] myscores6 = new float[128];
    float[] myscores2 = new float[128];
    float[] myscores1 = new float[128];
    float[] myscores3 = new float[128];
    float[] myscores4 = new float[128];
    float[] myscores5 = new float[128];
    private ArrayList<Result> mResults; // 保存检测结果的数组
    private Bitmap mBitmap; // 保存传递的 Bitmap 对象
    private float mivScaleX;
    private float mivScaleY;
    private float mstartX;
    private float mstartY;
    private Module model;
    private boolean mIsrealtime;
    public ResultView(Context context){ super(context);}

    public  ResultView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint(); // 初始化矩形框画笔
        mPaintRectangle.setColor(Color.BLUE); // 设置矩形框画笔颜色为蓝色
    }



    //计算两个向量的欧式距离
    public static double euclideanDistance(float[] a, float[] b) {
        double distance = 0.0;
        for (int i = 0; i < a.length; i++) {
            distance += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(distance);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {

        super.onDraw(canvas);
        if (mResults == null)
            return; // 如果没有检测结果，则直接返回

        double[] arr = new double[mResults.size()];
        int index = 0;
        //找到目标者
        for (Result result : mResults) // 遍历检测结果数组
        {
            int left = (int)((result.rect.left - mstartX)/mivScaleX);
            int top = (int) ((result.rect.top - mstartY)/mivScaleY);
            int right = (int)((result.rect.right - mstartX)/mivScaleX);
            int bottom = (int)((result.rect.bottom - mstartY)/mivScaleY);
            int w = right - left;
            int h = bottom - top;
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createBitmap(mBitmap, left, top, w, h);

            // 步骤零  缩放Bitmap
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 160, 160, true);
            //步骤一  读取bitmap上的图片，将它变为tensor格式
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap,
                    PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);

            //步骤二 前向传播
            final Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();
            //步骤三 获取tensor输出，转化为float的java矩阵
            final float[] scores = outputTensor.getDataAsFloatArray();  //一个128维的向量

            AssetManager assetManager = this.getContext().getAssets();

            if(mResults.size() >= 10)
            {
                try
                {
                    InputStream inputStream5 = assetManager.open("img_5.png");
                    mybitmap5 = BitmapFactory.decodeStream(inputStream5);
                    int b = 0;
                    // 在此处使用bitmap
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                // 步骤零  缩放Bitmap
                Bitmap myresizedBitmap5 = Bitmap.createScaledBitmap(mybitmap5, 160, 160, true);
                //步骤一  读取bitmap上的图片，将它变为tensor格式
                final Tensor myinputTensor5 = TensorImageUtils.bitmapToFloat32Tensor(myresizedBitmap5,
                        PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
                //步骤二 前向传播
                final Tensor myoutputTensor5 = model.forward(IValue.from(myinputTensor5)).toTensor();
                //步骤三 获取tensor输出，转化为float的java矩阵
                myscores5 = myoutputTensor5.getDataAsFloatArray();  //一个128维的向量

                try
                {
                    InputStream inputStream4 = assetManager.open("img_4.png");
                    mybitmap4 = BitmapFactory.decodeStream(inputStream4);
                    int b = 0;
                    // 在此处使用bitmap
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                // 步骤零  缩放Bitmap
                Bitmap myresizedBitmap4 = Bitmap.createScaledBitmap(mybitmap4, 160, 160, true);
                //步骤一  读取bitmap上的图片，将它变为tensor格式
                final Tensor myinputTensor4 = TensorImageUtils.bitmapToFloat32Tensor(myresizedBitmap4,
                        PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
                //步骤二 前向传播
                final Tensor myoutputTensor4 = model.forward(IValue.from(myinputTensor4)).toTensor();
                //步骤三 获取tensor输出，转化为float的java矩阵
                myscores4 = myoutputTensor4.getDataAsFloatArray();  //一个128维的向量

                try
                {
                    InputStream inputStream1 = assetManager.open("img_6.png");
                    mybitmap1 = BitmapFactory.decodeStream(inputStream1);
                    int b = 0;
                    // 在此处使用bitmap
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                // 步骤零  缩放Bitmap
                Bitmap myresizedBitmap1 = Bitmap.createScaledBitmap(mybitmap1, 160, 160, true);
                //步骤一  读取bitmap上的图片，将它变为tensor格式
                final Tensor myinputTensor1 = TensorImageUtils.bitmapToFloat32Tensor(myresizedBitmap1,
                        PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
                //步骤二 前向传播
                final Tensor myoutputTensor1 = model.forward(IValue.from(myinputTensor1)).toTensor();
                //步骤三 获取tensor输出，转化为float的java矩阵
                myscores1 = myoutputTensor1.getDataAsFloatArray();  //一个128维的向量

            }
            else {

                /*读取第二张图片*/
                try {
                    InputStream inputStream2 = assetManager.open("img_1.png");
                    mybitmap2 = BitmapFactory.decodeStream(inputStream2);
                    int b = 0;
                    // 在此处使用bitmap
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 步骤零  缩放Bitmap
                Bitmap myresizedBitmap2 = Bitmap.createScaledBitmap(mybitmap2, 160, 160, true);
                //步骤一  读取bitmap上的图片，将它变为tensor格式
                final Tensor myinputTensor2 = TensorImageUtils.bitmapToFloat32Tensor(myresizedBitmap2,
                        PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
                //步骤二 前向传播
                final Tensor myoutputTensor2 = model.forward(IValue.from(myinputTensor2)).toTensor();
                //步骤三 获取tensor输出，转化为float的java矩阵
                myscores2 = myoutputTensor2.getDataAsFloatArray();  //一个128维的向量


                /*读取第三张图片*/
                try {
                    InputStream inputStream3 = assetManager.open("img_2.png");
                    mybitmap3 = BitmapFactory.decodeStream(inputStream3);
                    int b = 0;
                    // 在此处使用bitmap
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 步骤零  缩放Bitmap
                Bitmap myresizedBitmap3 = Bitmap.createScaledBitmap(mybitmap3, 160, 160, true);
                //步骤一  读取bitmap上的图片，将它变为tensor格式
                final Tensor myinputTensor3 = TensorImageUtils.bitmapToFloat32Tensor(myresizedBitmap3,
                        PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
                //步骤二 前向传播
                final Tensor myoutputTensor3 = model.forward(IValue.from(myinputTensor3)).toTensor();
                //步骤三 获取tensor输出，转化为float的java矩阵
                myscores3 = myoutputTensor3.getDataAsFloatArray();  //一个128维的向量


                try {
                    InputStream inputStream6 = assetManager.open("img_3.png");
                    mybitmap6 = BitmapFactory.decodeStream(inputStream6);
                    int b = 0;
                    // 在此处使用bitmap
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 步骤零  缩放Bitmap
                Bitmap myresizedBitmap6 = Bitmap.createScaledBitmap(mybitmap6, 160, 160, true);
                //步骤一  读取bitmap上的图片，将它变为tensor格式
                final Tensor myinputTensor6 = TensorImageUtils.bitmapToFloat32Tensor(myresizedBitmap6,
                        PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
                //步骤二 前向传播
                final Tensor myoutputTensor6 = model.forward(IValue.from(myinputTensor6)).toTensor();
                //步骤三 获取tensor输出，转化为float的java矩阵
                myscores6 = myoutputTensor6.getDataAsFloatArray();  //一个128维的向量
            }
            if(mResults.size() >= 10)
            {
                double distant4 = euclideanDistance(scores, myscores4);
                double distant5 = euclideanDistance(scores, myscores5);
                double distant1 = euclideanDistance(scores, myscores1);
                distant = distant4 +distant5 + distant1;
            }
            else
            {

                double distant2 = euclideanDistance(scores, myscores2);
                double distant3 = euclideanDistance(scores, myscores3);
                double distant6 = euclideanDistance(scores, myscores6);
                distant = distant2 +distant3 + distant6;
            }


            arr[index] = distant;
            index = index + 1;
        }

        /*找到与目标人脸最相似的人脸*/
        index = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[index]) {
                index = i; // 如果当前元素小于最小值，更新最小值下标
            }
        }

        int i = 0;
        //画框
        for (Result result : mResults) // 遍历检测结果数组
        {
            if(i == index)
            {
                mPaintRectangle.setColor(Color.RED); // 设置矩形框画笔的颜色为蓝色
                mPaintRectangle.setStrokeWidth(5); // 设置矩形框画笔的线条宽度
                mPaintRectangle.setStyle(Paint.Style.STROKE); // 设置矩形框画笔的样式为描边
                canvas.drawRect(result.rect, mPaintRectangle); // 绘制矩形框
            }
            else
            {
                mPaintRectangle.setColor(Color.GREEN); // 设置矩形框画笔的颜色为蓝色
                mPaintRectangle.setStrokeWidth(5); // 设置矩形框画笔的线条宽度
                mPaintRectangle.setStyle(Paint.Style.STROKE); // 设置矩形框画笔的样式为描边
                canvas.drawRect(result.rect, mPaintRectangle); // 绘制矩形框
            }
            i = i + 1;
        }
    }


    public void setResults(ArrayList<Result> results, Bitmap bitmap, boolean isrealtime, float ivScaleX, float ivScaleY, float startX, float startY, Module premodel)
    {
        mBitmap = bitmap;
        mResults = results;
        mIsrealtime = isrealtime;
        mivScaleX = ivScaleX;
        mivScaleY = ivScaleY;
        mstartX = startX;
        mstartY = startY;
        model = premodel;
    } // 设置检测结果数组
}
