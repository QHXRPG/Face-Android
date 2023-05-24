package com.classify.myapplication;

import static com.classify.myapplication.DetectResultActivity.assetFilePath;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LiveActivity extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    PreviewView previewView;
    TextView textView;
    private Module premodel = null;

    private Module mModule = null;
    private ResultView mResultView;

    String mResultString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        previewView = (PreviewView) findViewById(R.id.cameraView);
        textView =  (TextView) findViewById(R.id.textview);
        mResultView = findViewById(R.id.resultView);

        // 加载模型
        try {
            mModule = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "mask.torchscript.ptl"));
            premodel = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "model.pt"));

        } catch (IOException e) {
            Log.e("Object Detection","Error reading assets",e);
            finish();
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(()->{
            try{
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCamera(cameraProvider);

            } catch (ExecutionException | InterruptedException e){

            }
        }, ContextCompat.getMainExecutor(this));

    }

    Executor executor = Executors.newSingleThreadExecutor();

    void startCamera(@NonNull ProcessCameraProvider cameraProvider){
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(640,640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();


        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
//                int rotation = image.getImageInfo().getRotationDegrees();
                analyzeImage(image, 0);
                image.close();
            }
        });



        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
    }
    //Planar格式（P）的处理
    private static ByteBuffer getuvBufferWithoutPaddingP(ByteBuffer uBuffer,ByteBuffer vBuffer, int width, int height, int rowStride, int pixelStride){
        int pos = 0;
        byte []byteArray = new byte[height*width/2];
        for (int row=0; row<height/2; row++) {
            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                byteArray[pos++] = vBuffer.get(vuPos);
                byteArray[pos++] = uBuffer.get(vuPos);
            }
        }
        ByteBuffer bufferWithoutPaddings=ByteBuffer.allocate(byteArray.length);
        // 数组放到buffer中
        bufferWithoutPaddings.put(byteArray);
        //重置 limit 和postion 值否则 buffer 读取数据不对
        bufferWithoutPaddings.flip();
        return bufferWithoutPaddings;
    }
    //Semi-Planar格式（SP）的处理和y通道的数据
    private static ByteBuffer getBufferWithoutPadding(ByteBuffer buffer, int width, int rowStride, int times,boolean isVbuffer){
        if(width == rowStride) return buffer;  //没有buffer,不用处理。
        int bufferPos = buffer.position();
        int cap = buffer.capacity();
        byte []byteArray = new byte[times*width];
        int pos = 0;
        //对于y平面，要逐行赋值的次数就是height次。对于uv交替的平面，赋值的次数是height/2次
        for (int i=0;i<times;i++) {
            buffer.position(bufferPos);
            //part 1.1 对于u,v通道,会缺失最后一个像u值或者v值，因此需要特殊处理，否则会crash
            if(isVbuffer && i==times-1){
                width = width -1;
            }
            buffer.get(byteArray, pos, width);
            bufferPos+= rowStride;
            pos = pos+width;
        }

        //nv21数组转成buffer并返回
        ByteBuffer bufferWithoutPaddings=ByteBuffer.allocate(byteArray.length);
        // 数组放到buffer中
        bufferWithoutPaddings.put(byteArray);
        //重置 limit 和postion 值否则 buffer 读取数据不对
        bufferWithoutPaddings.flip();
        return bufferWithoutPaddings;
    }

    private static byte[] YUV_420_888toNV21(Image image) {
        int width =  image.getWidth();
        int height = image.getHeight();
        ByteBuffer yBuffer = getBufferWithoutPadding(image.getPlanes()[0].getBuffer(), image.getWidth(), image.getPlanes()[0].getRowStride(),image.getHeight(),false);
        ByteBuffer vBuffer;
        //part1 获得真正的消除padding的ybuffer和ubuffer。需要对P格式和SP格式做不同的处理。如果是P格式的话只能逐像素去做，性能会降低。
        if(image.getPlanes()[2].getPixelStride()==1){ //如果为true，说明是P格式。
            vBuffer = getuvBufferWithoutPaddingP(image.getPlanes()[1].getBuffer(), image.getPlanes()[2].getBuffer(),
                    width,height,image.getPlanes()[1].getRowStride(),image.getPlanes()[1].getPixelStride());
        }else{
            vBuffer = getBufferWithoutPadding(image.getPlanes()[2].getBuffer(), image.getWidth(), image.getPlanes()[2].getRowStride(),image.getHeight()/2,true);
        }

        //part2 将y数据和uv的交替数据（除去最后一个v值）赋值给nv21
        int ySize = yBuffer.remaining();
        int vSize = vBuffer.remaining();
        byte[] nv21;
        int byteSize = width*height*3/2;
        nv21 = new byte[byteSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);

        //part3 最后一个像素值的u值是缺失的，因此需要从u平面取一下。
        ByteBuffer uPlane = image.getPlanes()[1].getBuffer();
        byte lastValue = uPlane.get(uPlane.capacity() - 1);
        nv21[byteSize - 1] = lastValue;
        return nv21;
    }


    @SuppressLint("UnsafeOptInUsageError")
    private Bitmap toBitmap(Image image) {
        byte[] nv21 = YUV_420_888toNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    void analyzeImage(ImageProxy imageProxy, int rotation){
        // 将 ImageProxy 转为 bitmap
        // imageProxy -> image
        @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();

        // image -> bitmap
        Bitmap bitmap = toBitmap(image);

        Matrix matrix = new Matrix();
        matrix.postRotate(90.0f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        // 缩放Bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        // Bitmap -> Tensor
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);    // 将bitmap转为Tensor
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();     // 前向传播
        final Tensor outputTensor = outputTuple[0].toTensor();                          // 获取结果里面的值
        final float[] outputs = outputTensor.getDataAsFloatArray();                     // 将结果转为浮点矩阵

        float imgScaleX = (float)bitmap.getWidth() / PrePostProcessor.mInputWidth;
        float imgScaleY = (float)bitmap.getHeight() / PrePostProcessor.mInputHeight;
        float ivScaleX = (float)mResultView.getWidth() / bitmap.getWidth();
        float ivScaleY = (float)mResultView.getHeight() / bitmap.getHeight();

        final ArrayList<Result> results = PrePostProcessor.outputsToNMSPredictions(outputs, imgScaleX, imgScaleY, ivScaleX, ivScaleY, 0, 0);

        mResultView.setResults(results,bitmap,true, ivScaleX, ivScaleY, 0, 0,premodel);
        mResultView.invalidate();

        mResultString += "检测到的总人数："+results.size()+"\n";
        textView.setText(mResultString);
        mResultString = "";
    }
}