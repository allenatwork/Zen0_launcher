package fr.neamar.kiss.ui.alphabetview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import fr.neamar.kiss.R;
import fr.neamar.kiss.ui.LightTextView;

public class AlphabetRecycleView extends RecyclerView {
    public interface OnCharaterSelected {
        void onCharaterSelected(String c);
    }

    private OnCharaterSelected onCharaterSelected;
    private GestureDetector mGestureDetector;

    public AlphabetRecycleView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AlphabetRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlphabetRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setOnCharaterSelected(OnCharaterSelected onCharaterSelected) {
        this.onCharaterSelected = onCharaterSelected;
    }

    OnItemTouchListener onItemTouchListener = new OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
            if (childView != null
                    && childView instanceof LightTextView
                    && mGestureDetector != null
                    && mGestureDetector.onTouchEvent(e)) {
                String c = ((LightTextView) childView).getText().toString();
                if (!TextUtils.isEmpty(c) && onCharaterSelected != null) {
                    onCharaterSelected.onCharaterSelected(c.toLowerCase(Locale.US));
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    private void init(Context context) {
        setLayoutManager(new LinearLayoutManager(context));
        setHasFixedSize(true);
        setAdapter(new AlphabetAdapter(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context,
                LinearLayout.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        addItemDecoration(dividerItemDecoration);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        removeOnItemTouchListener(onItemTouchListener);
        addOnItemTouchListener(onItemTouchListener);
    }
}
