/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.util.Map;

/**
 * TODO: Add a soft dialpad for PIN entry.
 */
class EditPinPreference extends EditTextPreference {

    private boolean mDialogOpen;
    
    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference preference, boolean positiveResult);
    }
    
    private OnPinEnteredListener mPinListener;
    
    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        mPinListener = listener;
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        
        if (editText != null) {
            editText.setSingleLine(true);
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editText.setKeyListener(DigitsKeyListener.getInstance());
        }
    }

    public boolean isDialogOpen() {
        return mDialogOpen;
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mDialogOpen = false;
        if (mPinListener != null) {
            mPinListener.onPinEntered(this, positiveResult);
        }
    }
    
    public void showPinDialog() {
        mDialogOpen = true;
        showDialog(null);
    }
}