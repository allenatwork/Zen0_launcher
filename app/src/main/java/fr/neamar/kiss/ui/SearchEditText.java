package fr.neamar.kiss.ui;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SearchEditText extends EditText {
    private OnEditorActionListener mEditorListener;
    private boolean showKeyboardDelayed = false;

    public SearchEditText(Context context) {
        super(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnEditorActionListener(OnEditorActionListener listener) {
        mEditorListener = listener;
        super.setOnEditorActionListener(listener);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
            if (mEditorListener != null && mEditorListener.onEditorAction(this, android.R.id.closeButton, event))
                return true;
        return super.onKeyPreIme(keyCode, event);
    }

    public void focusAndShowKeyboard () {
        requestFocus();
        showKeyboardDelayed = true;
        maybeShowKeyboard();
    }

    private void maybeShowKeyboard () {
        if (hasWindowFocus() && showKeyboardDelayed) {
            if (isFocused()) {
                post(() -> {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(SearchEditText.this, InputMethodManager.SHOW_IMPLICIT);
                });
            }
            showKeyboardDelayed = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        maybeShowKeyboard();
    }
}
