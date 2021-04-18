package fr.neamar.kiss.adapter;

import android.app.DialogFragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neamar.kiss.R;
import fr.neamar.kiss.normalizer.StringNormalizer;
import fr.neamar.kiss.result.AppResult;
import fr.neamar.kiss.result.ContactsResult;
import fr.neamar.kiss.result.PhoneResult;
import fr.neamar.kiss.result.Result;
import fr.neamar.kiss.result.SearchResult;
import fr.neamar.kiss.result.SettingsResult;
import fr.neamar.kiss.result.ShortcutsResult;
import fr.neamar.kiss.searcher.QueryInterface;
import fr.neamar.kiss.ui.ListPopup;
import fr.neamar.kiss.utils.FuzzyScore;
import fr.neamar.kiss.utils.Tool;

public class RecordAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final QueryInterface parent;
    private FuzzyScore fuzzyScore;

    /**
     * Array list containing all the results currently displayed
     */
    private List<Result> results;

    public RecordAdapter(QueryInterface parent, ArrayList<Result> results) {
        this.parent = parent;
        this.results = results;
        this.fuzzyScore = null;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case ResultType.APP:
                view = inflater.inflate(R.layout.item_app, parent, false);
                break;
            case ResultType.SEARCH:
                view = inflater.inflate(R.layout.item_search, parent, false);
                break;
            case ResultType.CONTACT:
                view = inflater.inflate(R.layout.item_contact, parent, false);
                break;
            case ResultType.SETTING:
                view = inflater.inflate(R.layout.item_setting, parent, false);
                break;
            case ResultType.PHONE:
                view = inflater.inflate(R.layout.item_phone, parent, false);
                break;
            case ResultType.SHORTCUT:
                view = inflater.inflate(R.layout.item_shortcut, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.item_app, parent, false);
        }
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        results.get(position).display(holder.itemView.getContext(), holder.itemView, fuzzyScore);
        final int pos = position;
        holder.itemView.setOnClickListener(v -> {
            try {
                results.get(pos).launch(v.getContext(),holder.itemView,parent);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            ListPopup menu = results.get(pos).getPopupMenu(v.getContext(), RecordAdapter.this, v);

            // check if menu contains elements and if yes show it
            if (menu.getAdapter().getCount() > 0) {
                parent.registerPopup(menu);
                menu.show(v);
            }
            return true;
        });
    }

    public void  onClick(final int position, View v) {
        if (results == null) return;
        final Result result;
        try {
            result = results.get(position);
            result.launch(v.getContext(), v, parent);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    @Override
    public int getItemViewType(int position) {
        return results.get(position).getViewType();
    }

    public void onLongClick(final int pos, View v) {
        ListPopup menu = results.get(pos).getPopupMenu(v.getContext(), this, v);

        // check if menu contains elements and if yes show it
        if (menu.getAdapter().getCount() > 0) {
            parent.registerPopup(menu);
            menu.show(v);
        }
    }

    public void removeResult(Context context, Result result) {
        results.remove(result);
        notifyDataSetChanged();
        // Do not reset scroll, we want the remaining items to still be in view
        parent.temporarilyDisableTranscriptMode();
    }

    public void updateResults(List<Result> results, String query) {
        this.results.clear();
        this.results.addAll(results);
        StringNormalizer.Result queryNormalized = StringNormalizer.normalizeWithResult(query, false);

        fuzzyScore = new FuzzyScore(queryNormalized.codePoints, true);
        notifyDataSetChanged();
    }

    /**
     * Force set transcript mode on the list.
     * Prefer to use `parent.temporarilyDisableTranscriptMode();`
     */
    public void updateTranscriptMode(int transcriptMode) {
        parent.updateTranscriptMode(transcriptMode);
    }


    public void clear() {
        this.results.clear();
        notifyDataSetChanged();
    }

    public void showDialog(DialogFragment dialog) {
        parent.showDialog(dialog);
    }
}
