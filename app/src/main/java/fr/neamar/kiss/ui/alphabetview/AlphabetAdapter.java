package fr.neamar.kiss.ui.alphabetview;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.neamar.kiss.R;
import fr.neamar.kiss.ui.LightTextView;

public class AlphabetAdapter extends RecyclerView.Adapter<AlphabetAdapter.CharacterHolder> {
    public static final String[] listAlphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    //    private Drawable drawable;
    private int size;

    public AlphabetAdapter(Context context) {
//        drawable = context.getResources().getDrawable(R.drawable.cirle_gray_ripple);
        size = (int) context.getResources().getDimension(R.dimen.alphabe_button_size);
    }

    @NonNull
    @Override
    public CharacterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LightTextView textView = new LightTextView(parent.getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(size, size);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundResource(R.drawable.cirle_gray_ripple);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setLayoutParams(layoutParams);
        textView.setTextColor(parent.getResources().getColor(android.R.color.white));
        return new CharacterHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterHolder holder, int position) {
        holder.bindData(listAlphabet[position]);
    }

    @Override
    public int getItemCount() {
        return listAlphabet.length;
    }

    public static class CharacterHolder extends RecyclerView.ViewHolder {
        LightTextView textView;

        public CharacterHolder(@NonNull View itemView) {
            super(itemView);
            textView = (LightTextView) itemView;
        }

        public void bindData(String s) {
            textView.setText(s);
        }
    }
}
