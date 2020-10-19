package com.screen.share.newone.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class EditTextFilter implements InputFilter {

    private int min, max;

    public EditTextFilter(){
        this.min = 1;
        this.max = 255;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
