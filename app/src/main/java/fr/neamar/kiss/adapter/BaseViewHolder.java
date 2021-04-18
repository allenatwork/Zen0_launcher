package fr.neamar.kiss.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.neamar.kiss.result.Result;
import fr.neamar.kiss.utils.FuzzyScore;

public class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void display(Result result, FuzzyScore fuzzyScore){

    }
}
