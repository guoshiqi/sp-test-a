package wendu.spidersdk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by du on 16/4/19.
 */
class CircleProgress extends View {

    double startDegree =-Math.PI/2 ;
    double endDegree = Math.PI*2+startDegree;
    double desDegree =startDegree ;
    int max=100;
    int percent;
    float WIDTH = 2f * getResources().getDisplayMetrics().density;
    int foregroundColor = Helper.getColor(getContext(), R.color.colorPrimary);
    int backgroundColor = Color.argb(30, Color.red(foregroundColor),
            Color.green(foregroundColor),Color.blue(foregroundColor));

    public int getInnerColor() {
        return innerColor;
    }

    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
        invalidate();
    }

    int innerColor =Color.TRANSPARENT;

    Helper.ColorGradientHelper colorGradientHelper;

    public CircleProgress(Context context)
    {
        super(context);
    }

    public CircleProgress(Context context, AttributeSet attrs) {

        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
        int foregroundColorEnd=foregroundColor;
        try {
            foregroundColor = a.getColor(R.styleable.CircleProgress_foreground_color, foregroundColor);
            foregroundColorEnd = a.getColor(R.styleable.CircleProgress_foreground_end_color, foregroundColor);
            backgroundColor = a.getColor(R.styleable.CircleProgress_background_color,
                    Color.argb(30, Color.red(foregroundColor),Color.green(foregroundColor),Color.blue(foregroundColor)));
            innerColor = a.getColor(R.styleable.CircleProgress_inner_color, innerColor);
            WIDTH=a.getDimension(R.styleable.CircleProgress_width,WIDTH);

        } finally {
            if (a != null) {
                a.recycle();
            }
        }
        colorGradientHelper=new Helper.ColorGradientHelper(foregroundColor,foregroundColorEnd);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float strokeWidth = (int) (2*WIDTH);
        if (innerColor!=Color.TRANSPARENT) {
            paint.setColor(innerColor);
            canvas.drawCircle(width / 2, height / 2, width / 2 - strokeWidth, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        RectF rectF = new RectF(WIDTH, WIDTH, (float) width - WIDTH, (float) height - WIDTH);

        float start=toDegree(startDegree);
        float end=toDegree(desDegree);
        float partition=end - start;
        if (partition<1&&partition>0){
            partition=1;
        }
        int delta=(int) partition;
        colorGradientHelper.setPartition(delta);
        if (delta<10){
            paint.setColor(foregroundColor);
            canvas.drawArc(rectF, start, delta, false, paint);
            start+=delta;
        }else if(delta==360){
            paint.setColor(colorGradientHelper.getEndColor());
            canvas.drawArc(rectF, start, 360, false, paint);
            start=toDegree(endDegree);
        }else
        {
            for (int i = 0; i < delta; i++) {
                int color = colorGradientHelper.getColor(i);
                paint.setColor(color);
                canvas.drawArc(rectF, start , 1.1f, false, paint);
                start++;
            }
        }
        paint.setColor(backgroundColor);
        canvas.drawArc(rectF, start, toDegree(endDegree) - start, false, paint);

    }

    float toDegree(double d) {
        return (float) (d / Math.PI * 180);
    }

    public void setProgress(int percent) {
        this.percent=percent;
        desDegree = 2*Math.PI*percent/max+startDegree;
        invalidate();
    }

    public int getProgress(){
        return percent;
    }

    public void  setMax( int max){
        this.max=max;
    }

}
