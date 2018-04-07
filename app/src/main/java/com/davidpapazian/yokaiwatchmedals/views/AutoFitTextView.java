package com.davidpapazian.yokaiwatchmedals.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import com.davidpapazian.yokaiwatchmedals.R;

public class AutoFitTextView extends TextView {

    private Paint mTestPaint;
    private float max;
    private float min;

    public AutoFitTextView(Context context) {
        super(context);
        initialise();
    }

    public AutoFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttr(context,attrs);
        initialise();
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        max = (int) Math.floor(getMeasuredHeight()/2);
        min = (int) Math.floor(getMeasuredHeight()/3);
        //max size defaults to the initially specified text size unless it is too small
    }

    private void readAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoFitTextViewAttributes);

        String maxSize = a.getString(R.styleable.AutoFitTextViewAttributes_maxSize);
        String minSize = a.getString(R.styleable.AutoFitTextViewAttributes_minSize);
        if (maxSize != null) {
            //max = Float.valueOf(maxSize);
        }
        if (minSize != null) {
            //min = Float.valueOf(minSize);
        }

        a.recycle();
    }

    private void refitText(String text, int textWidth){
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        max = getHeight()/2;
        min = getHeight()/6;
        float hi = max;
        float lo = min;
        final float threshold = 0.5f; // How close we have to be
        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText(text) >= targetWidth)
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    public void setMaxSize(int size) {
        max = dpToPx(size);
    }

    public void setMinSize(int size) {
        min = dpToPx(size);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    public void setText(String text) {
        setText(text, 0);
    }

    public void setText(String text, int size) {
        //max = dpToPx(size);
        //min = max/2;
        if (text.contains("[")) {
            int nb = text.indexOf("]");
            SpannableString spanText = new SpannableString(text.replace("[","").replace("]"," "));
            spanText.setSpan(new RelativeSizeSpan(0.7f), 0, nb, 0);
            spanText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red)), 0, nb, 0);
            super.setText(spanText);
        } else {
            super.setText(text);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int pxToDp(int px){
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        return Math.round(px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /*
    public class SuperscriptSpanAdjuster extends MetricAffectingSpan {
        double ratio = 0.5;

        public SuperscriptSpanAdjuster(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public void updateDrawState(TextPaint paint) {
            paint.baselineShift += (int) (paint.ascent() * ratio);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            paint.baselineShift += (int) (paint.ascent() * ratio);
        }
    }
    */

}
