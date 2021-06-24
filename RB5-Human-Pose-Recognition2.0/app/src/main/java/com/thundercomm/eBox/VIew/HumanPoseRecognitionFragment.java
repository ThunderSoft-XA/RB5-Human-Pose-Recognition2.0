package com.thundercomm.eBox.VIew;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;

import com.thundercomm.eBox.Utils.LogUtil;
import com.thundercomm.eBox.Data.PointData;

public class HumanPoseRecognitionFragment extends PlayFragment {
    private static final String TAG = "HumanPoseRecognitionFragment";
    protected Paint paint_Point;
    protected Paint paint_Skeleton;

     private  int[][] pointPairs= {{1, 0}, {1, 2}, {1, 5}, {2, 3},
             {3, 4}, {5, 6}, {6, 7}, {0, 15}, {15, 17}, {0, 16},
             {16, 18}, {1, 8}, {8, 9}, {9, 10}, {10, 11},
             {11, 22}, {22, 23}, {11, 24}, {8, 12},
             {12, 13}, {13, 14}, {14, 19}, {19, 20}, {14, 21}};

    @Override
    void initPaint() {
        super.initPaint();

        paint_Point = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Point.setColor(Color.CYAN);
        paint_Point.setShadowLayer(10f, 0, 0, Color.CYAN);
        paint_Point.setStyle(Paint.Style.STROKE);
        paint_Point.setStrokeWidth(4);
        paint_Point.setFilterBitmap(true);

        paint_Skeleton = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_Skeleton.setColor(Color.RED);
        paint_Skeleton.setShadowLayer(10f, 0, 0, Color.CYAN);
        paint_Skeleton.setStyle(Paint.Style.STROKE);
        paint_Skeleton.setStrokeWidth(4);
        paint_Skeleton.setFilterBitmap(true);
    }

    public HumanPoseRecognitionFragment(int id) {
        super(id);
    }

    private void draw(final SurfaceHolder mHolder, PointData pointDate) {
        Canvas canvas = null;
        if (mHolder != null) {
            LogUtil.i(TAG, " " + pointDate.toString());
            try {
                if (paint_Skeleton == null || paint_Point == null) {
                    initPaint();
                }
                canvas = mHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                for (int i = 0; i < 25; i++) {
                    drawSkeleton(canvas, pointDate.mPoints[pointPairs[i][0]].x, pointDate.mPoints[pointPairs[i][0]].y,
                            pointDate.mPoints[pointPairs[i][1]].x, pointDate.mPoints[pointPairs[i][1]].y);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != canvas) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
        hasDrawn = false;
    }

    void drawSkeleton(Canvas canvas, int x1, int y1, int x2, int y2) {

        canvas.drawLine(x1, y1, x2, y2, paint_Skeleton);

        canvas.drawCircle(x1, y1, 5, paint_Point);
        canvas.drawCircle(x2, y2, 5, paint_Point);
    }

    public void Ondraw(PointData pointDate) {
        draw(mFaceViewHolder, pointDate);
        hasDrawn = true;
    }

}
