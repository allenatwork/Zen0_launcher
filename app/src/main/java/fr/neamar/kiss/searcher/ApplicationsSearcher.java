package fr.neamar.kiss.searcher;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.pojo.AppPojo;
import fr.neamar.kiss.pojo.Pojo;
import fr.neamar.kiss.pojo.PojoComparator;
import fr.neamar.kiss.result.Result;

/**
 * Returns the list of all applications on the system
 */
public class ApplicationsSearcher extends Searcher {
    public ApplicationsSearcher(MainActivity activity) {
        super(activity, "<application>");
    }

    @Override
    PriorityQueue<Pojo> getPojoProcessor(Context context) {
        return new PriorityQueue<>(DEFAULT_MAX_RESULTS, new PojoComparator());
    }

    @Override
    protected int getMaxResultCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return null;

        List<AppPojo> pojos = KissApplication.getApplication(activity).getDataHandler().getApplicationsWithoutExcluded();

        if (pojos != null)
            this.addResult(pojos.toArray(new Pojo[0]));
        return null;
    }

    //todo: bad code, find the way to refactor it
    @Override
    protected void onPostExecute(Void param) {
        MainActivity activity = activityWeakReference.get();
        if (activity != null) {
            if (this.processedPojos.isEmpty()) {
//                activity.updateListApps(Collections.emptyList());
            } else {
                PriorityQueue<Pojo> queue = this.processedPojos;
                ArrayList<Result> results = new ArrayList<>(queue.size());
                while (queue.peek() != null) {
                    results.add(Result.fromPojo(activity, queue.poll()));
                }

                activity.updateResults(results, "<application>");
            }
        }
//        super.onPostExecute(param);
    }
}
