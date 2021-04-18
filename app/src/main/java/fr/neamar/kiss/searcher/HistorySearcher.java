package fr.neamar.kiss.searcher;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.pojo.Pojo;

/**
 * Retrieve pojos from history
 */
public class HistorySearcher extends Searcher {
    public HistorySearcher(MainActivity activity) {
        super(activity, "<history>");
    }

    @Override
    int getMaxResultCount() {
        // Convert `"number-of-display-elements"` to double first before truncating to int to avoid
        // `java.lang.NumberFormatException` crashes for values larger than `Integer.MAX_VALUE`
//        try {
//            return Double.valueOf(prefs.getString("number-of-display-elements", String.valueOf(DEFAULT_MAX_RESULTS))).intValue();
//        } catch (NumberFormatException e) {
//            return DEFAULT_MAX_RESULTS;
//        }
        return 5;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activityWeakReference.get());

        // Ask for records
        String historyMode = prefs.getString("history-mode", "recency");
        //Gather excluded

        List<Pojo> pojos = KissApplication.getApplication(activity).getDataHandler()
                .getHistory(activity, getMaxResultCount(), historyMode);

        int size = pojos.size();
        for(int i = 0; i < size; i += 1) {
            pojos.get(i).relevance = size - i;
        }

        this.addResult(pojos.toArray(new Pojo[0]));
        return null;
    }
}
