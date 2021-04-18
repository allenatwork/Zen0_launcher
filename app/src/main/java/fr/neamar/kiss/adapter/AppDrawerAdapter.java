package fr.neamar.kiss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.neamar.kiss.R;
import fr.neamar.kiss.pojo.AppPojo;
import fr.neamar.kiss.result.AppResult;
import fr.neamar.kiss.result.Result;

public class AppDrawerAdapter extends RecyclerView.Adapter<AppDrawerAdapter.AppDrawerHolder> {
    List<Result> results;

    public AppDrawerAdapter(List<Result> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public AppDrawerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_app_drawer, parent, false);
        return new AppDrawerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppDrawerHolder holder, int position) {
        holder.display(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public static class AppDrawerHolder extends RecyclerView.ViewHolder {
        TextView tvAppTitle;
        ImageView ivAppIcon;
        public AppDrawerHolder(@NonNull View itemView) {
            super(itemView);
            tvAppTitle = itemView.findViewById(R.id.app_title);
            ivAppIcon = itemView.findViewById(R.id.item_app_icon);
        }

        public void display(Result result) {
            AppPojo appPojo = ((AppResult)result).getAppPojo();
            tvAppTitle.setText(appPojo.getName());
            Glide.with(ivAppIcon).load(result.getDrawable(itemView.getContext())).into(ivAppIcon);
            ivAppIcon.setOnClickListener(v -> result.launch(v.getContext(), v, null));
        }
    }
}
