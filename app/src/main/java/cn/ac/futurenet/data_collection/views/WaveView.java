package cn.ac.futurenet.data_collection.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

public class WaveView extends View {
    private static final int MAX_POINTS_NUM = 44100 / 4;

    private Paint paint;
    private short[] audio;
    private float[] points;

    private int widthPixels;
    private int heightPixels;

    private int pos;

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(1);

        points = new float[MAX_POINTS_NUM * 4];
        pos = 0;

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                widthPixels = getWidth();
                heightPixels = getHeight();

                for (int i = 0; i < MAX_POINTS_NUM; ++i) {
                    points[i * 4] = (float) i / MAX_POINTS_NUM * widthPixels;
                    points[i * 4 + 2] = (float) (i + 1) / MAX_POINTS_NUM * widthPixels;
                }
            }
        });
    }

    public void update(short[] audio, int len) {
        this.audio = audio;
        int base;

        // 移动旧数据
        int offset = (pos + len) - MAX_POINTS_NUM;
        if (offset > 0) {
            for (int i = offset; i < pos; ++i) {
                base = i * 4;
                points[(i - offset) * 4 + 1] = points[base + 1];
                points[(i - offset) * 4 + 3] = points[base + 3];
            }
            pos -= offset;
        }

        // 存放新数据
        for (int i = pos, end = pos + len - 1; i < end; ++i) {
            base = 4 * i;
            points[base+ 1] = heightPixels / 2 + (float) audio[i - pos] / 32768 * 4 * heightPixels / 2;
            points[base+ 3] = heightPixels / 2 + (float)audio[i - pos + 1] / 32768 * 4 * heightPixels / 2;
        }

        pos += len;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLines(points, paint);
    }
}
