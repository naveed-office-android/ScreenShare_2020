package com.screen.share.newone.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.screen.share.newone.R;

public class MyCardView extends LinearLayout {

    ImageView icon;
    TextView text;
    LinearLayout ll_background;

    /**
     * Initiates custom card view
     * @param context - context
     */
    public MyCardView(@NonNull Context context) {
        super(context);
        init();
    }

    /**
     * Initiates custom card view with attributes
     * @param context - context
     * @param attrs - attributes set
     */
    public MyCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     * Initiates custom card view with attributes set
     * @param context - context
     * @param attrs - attributes set
     * @param defStyleAttr - attrbute style
     */
    public MyCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Initialize cardview with no attribute sets
     */
    private void init() {
        inflate(getContext(), R.layout.item_view_enhancement_option, this);
        this.text = findViewById(R.id.option_name);
        this.icon = findViewById(R.id.option_image);
       // this.ll_background = findViewById(R.id.passwordProtectedPDF);
    }

    /**
     * Initialize cardview with attributes recevied
     * @param attrs - attribute set
     */
    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.item_view_enhancement_option, this);

        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MyCardView);

        this.text = findViewById(R.id.option_name);
        this.icon = findViewById(R.id.option_image);
        this.ll_background = findViewById(R.id.container_card_view);
        this.text.setText(a.getString(R.styleable.MyCardView_option_text));
        Drawable drawable = a.getDrawable(R.styleable.MyCardView_option_icon);
        this.icon.setImageDrawable(drawable);
        //this.ll_background.setBackground(a.getDrawable(R.styleable.MyCardView_ll_background));

        a.recycle();
    }
    public void  setImageResource(Drawable drawable)
    {
        this.icon.setImageDrawable(drawable);
    } public void  setText(String text)
    {
        this.text.setText(text);
    }
}

