package com.thundercomm.eBox.Data;

public class PointData {
    public Point[] mPoints;

    public PointData () {
        mPoints = new Point[25];
        for (int i =0; i < 25; i++) {
            mPoints[i] = new Point();
        }
    }

    public void setValue(int x, int y, float prob, int index) {
        mPoints[index].x = x;
        mPoints[index].y = y;
        mPoints[index].prob = prob;
    }

    public class Point {
        public int x = 0;
        public int y = 0;
        public float prob = 0.0f;
    }
}
