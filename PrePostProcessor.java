package com.classify.myapplication;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


class Result{
    int classIndex;
    Float score;
    Rect rect;

    public Result(int cls, Float output, Rect rect){
        this.classIndex = cls;
        this.score = output;
        this.rect = rect;
    }
}


public class PrePostProcessor {
    static float[] NO_MEAN_RGB = new float[] {0.0f, 0.0f, 0.0f};
    static float[] NO_STD_RGB = new float[] {1.0f, 1.0f, 1.0f};

    static int mInputWidth = 640;
    static int mInputHeight = 640;

    private static int mOutputRow = 25200; // 640x640
    private static int mOutputColumn = 8; // left, top, right, bottom, score + 80 class
    private static float mThreshold = 0.30f;
    private static int mNmsLimit = 60;

    static  String[] mClasses;

    static float IOU(Rect a, Rect b)
    {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0 )*
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }

    static ArrayList<Result> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold)
    {
        Collections.sort(boxes,
                new Comparator<Result>()
                {
                    @Override
                    public int compare(Result o1, Result o2)
                    {
                        return o1.score.compareTo(o2.score);
                }
                });
        ArrayList<Result> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        boolean done = false;
        for (int i=0; i<boxes.size() && !done; i++)
        {
            if (active[i])
            {
                Result boxA = boxes.get(i);
                selected.add(boxA);
                if (selected.size() >= limit)
                    break;

                for(int j = i+1; j<boxes.size();j++)
                {
                    if(active[j])
                    {
                        Result boxB = boxes.get(j);
                        if (IOU(boxA.rect, boxB.rect)>threshold)
                        {
                            active[j] = false;
                            numActive -= 1;
                            if  (numActive <= 0)
                            {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }

    static ArrayList<Result> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY, float ivScaleX, float ivScaleY,float startX, float startY){
        ArrayList<Result> results = new ArrayList<>();
        for (int i=0; i<mOutputRow; i++){
            if (outputs[i* mOutputColumn +4]>mThreshold){
                float x = outputs[i* mOutputColumn];
                float y = outputs[i* mOutputColumn +1];
                float w = outputs[i* mOutputColumn +2];
                float h = outputs[i* mOutputColumn +3];

                float left = imgScaleX * (x - w/2);
                float top = imgScaleY * (y-h/2);
                float right = imgScaleX * (x + w/2);
                float bottom = imgScaleY * (y + h/2);

                float max = outputs[i* mOutputColumn +5];
                int cls = 0;
                for (int j=0; j<mOutputColumn-5;j++)
                {
                    if (outputs[i* mOutputColumn +5+j] > max)
                    {
                        max = outputs[i * mOutputColumn +5+j];
                        cls = j;
                    }
                }

                Rect rect = new Rect((int)(startX + ivScaleX*left),(int)(startY+top*ivScaleY),
                        (int)(startX+ivScaleX*right), (int) (startY+ivScaleY*bottom));
                Result result = new Result(cls, outputs[i * mOutputColumn+4], rect);
                results.add(result);
            }
        }
        return nonMaxSuppression(results, mNmsLimit, mThreshold);
    }
}
