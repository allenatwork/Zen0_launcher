package fr.neamar.kiss;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.neamar.kiss.adapter.RecordAdapter;
import fr.neamar.kiss.broadcast.IncomingCallHandler;
import fr.neamar.kiss.result.Result;
import fr.neamar.kiss.searcher.HistorySearcher;
import fr.neamar.kiss.searcher.QueryInterface;
import fr.neamar.kiss.searcher.QuerySearcher;
import fr.neamar.kiss.searcher.Searcher;
import fr.neamar.kiss.ui.ListPopup;
import fr.neamar.kiss.ui.SearchEditText;
import fr.neamar.kiss.ui.alphabetview.AlphabetRecycleView;
import fr.neamar.kiss.ui.slideuppanel.SlidingUpPanelLayout;
import fr.neamar.kiss.utils.PackageManagerUtils;
import fr.neamar.kiss.utils.Permission;
import fr.neamar.kiss.utils.SystemUiVisibilityHelper;
import fr.neamar.kiss.utils.Tool;
import fr.neamar.kiss.utils.Utilities;
import fr.neamar.kiss.utils.Widgets;
import timber.log.Timber;

public class MainActivity extends Activity implements QueryInterface,
        View.OnTouchListener {

    public static final String START_LOAD = "fr.neamar.summon.START_LOAD";
    public static final String LOAD_OVER = "fr.neamar.summon.LOAD_OVER";
    public static final String FULL_LOAD_OVER = "fr.neamar.summon.FULL_LOAD_OVER";

    private static final String TAG = "MainActivity";
    private RecordAdapter adapter;
    public SharedPreferences prefs;
    private BroadcastReceiver mReceiver;
    public SearchEditText searchEditText;
    public RecyclerView list;
    public View listContainer;
    private Searcher searchTask;
    private SystemUiVisibilityHelper systemUiVisibilityHelper;
    private PopupWindow mPopup;
    private Permission permissionManager;
    private GestureDetector gd;

    private Widgets widgetManager;
    private ViewGroup vgWidgerArea;

    SlidingUpPanelLayout panelLayout;
    int tempAlpha = -1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeTransparent);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        KissApplication.getApplication(this).initDataHandler();

        /*
         * Initialize preferences
         */
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        getTheme().applyStyle(prefs.getBoolean("small-results", false) ?
                R.style.OverlayResultSizeSmall :
                R.style.OverlayResultSizeStandard, true);
        /*
         * Initialize all forwarders
         */
        permissionManager = new Permission(this);

        /*
         * Initialize data handler and start loading providers
         */
        IntentFilter intentFilterLoad = new IntentFilter(START_LOAD);
        IntentFilter intentFilterLoadOver = new IntentFilter(LOAD_OVER);
        IntentFilter intentFilterFullLoadOver = new IntentFilter(FULL_LOAD_OVER);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //noinspection ConstantConditions
                if (intent.getAction().equalsIgnoreCase(LOAD_OVER)) {
//                    updateSearchRecords(true);
                } else if (intent.getAction().equalsIgnoreCase(FULL_LOAD_OVER)) {
                    Log.v(TAG, "All providers are done loading.");
                    // Run GC once to free all the garbage accumulated during provider initialization
                    System.gc();
                }

                // New provider might mean new favorites
                onFavoriteChange();
            }
        };

        this.registerReceiver(mReceiver, intentFilterLoad);
        this.registerReceiver(mReceiver, intentFilterLoadOver);
        this.registerReceiver(mReceiver, intentFilterFullLoadOver);

        /*
         * Set the view and store all useful components
         */
        setContentView(R.layout.main);
        panelLayout = findViewById(R.id.panel_root);
        this.list = this.findViewById(android.R.id.list);
        this.listContainer = (View) this.list.getParent();
        this.searchEditText = findViewById(R.id.searchEditText);
        // Create adapter for records
        adapter = new RecordAdapter(this, new ArrayList<>());
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(this.adapter);

        // Listen to changes
        searchEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && s.charAt(0) == ' ')
                    s.delete(0, 1);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                updateSearchRecords(text);
            }
        });

        // On validate, launch first record
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.R.id.closeButton) {
                systemUiVisibilityHelper.onKeyboardVisibilityChanged(false);
                if (mPopup != null) {
                    mPopup.dismiss();
                    return true;
                }
                systemUiVisibilityHelper.onKeyboardVisibilityChanged(false);
                return false;
            }

            adapter.onClick(0, v);

            return true;
        });

        // Enable/disable phone broadcast receiver
        PackageManagerUtils.enableComponent(this,
                IncomingCallHandler.class,
                prefs.getBoolean("enable-phone-history",
                        false));

        systemUiVisibilityHelper = new SystemUiVisibilityHelper(this);

        // For devices with hardware keyboards, give focus to search field.
        if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY
                || getResources().getConfiguration().keyboard == Configuration.KEYBOARD_12KEY) {
            searchEditText.requestFocus();
        }

        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float directionY = e2.getY() - e1.getY();
                float directionX = e2.getX() - e1.getX();
                if (Math.abs(directionX) > Math.abs(directionY)) {
                    if (directionX > 0) { // Gesture right
                        Timber.d("Gesture right");
                    } else { // Gesture left
                        Timber.d("Gesture left");
                    }
                } else {
                    if (directionY > 0) { // Gesture down
                        Timber.d("Gesture down");
                        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                        searchEditText.focusAndShowKeyboard();
                        showHistory(true);
                    } else { // Gesture up
                        Timber.d("Gesture up");
                    }
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (widgetManager.onOptionsItemSelected()) return;
                super.onLongPress(e);

            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                int offset = (int) Math.floor(slideOffset * 10);
                if (offset != tempAlpha) {
                    tempAlpha = offset;
                    Timber.d("tempAlpha: " + tempAlpha);
                    if (tempAlpha == 0) {
                        vgWidgerArea.setAlpha(1);
                    } else if (tempAlpha == 10) {
//                        vgWidgerArea.setAlpha(0);
                    } else {
                        float alpha = 0.9F - (float) tempAlpha / 10;
                        vgWidgerArea.setAlpha(alpha > 0 ? alpha : 0 );
                    }

                }

            }

            @Override
            public void onPanelStateChanged(View panel,
                                            SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                Timber.d("onPanelStateChanged: old %s - new %s", previousState.toString(), newState.toString());
                if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    if (previousState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        if (Tool.isKeyboardShowing(searchEditText)) {
                            Tool.hideKeyboard(searchEditText);
                        }
                    }
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    if (Tool.isKeyboardShowing(searchEditText)) {
                        Tool.hideKeyboard(searchEditText);
                    }

                    if (searchEditText.hasFocus()) {
                        searchEditText.clearFocus();
                        searchEditText.setText("");
                    }
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if (!searchEditText.hasFocus()) {
                        searchEditText.focusAndShowKeyboard();
                    }
                }
            }
        });
        vgWidgerArea = findViewById(R.id.widgetLayout);
        vgWidgerArea.setOnTouchListener(this);
        widgetManager = new Widgets(this, vgWidgerArea);
        widgetManager.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        widgetManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        widgetManager.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        widgetManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showHistory(boolean show) {
        if (show) {
            runTask(new HistorySearcher(this));
        } else {
            adapter.clear();
        }
    }

    /**
     * Restart if required,
     * Hide the kissbar by default
     */
    @SuppressLint("CommitPrefEdits")
    protected void onResume() {
        Timber.d("onResume()");
        dismissPopup();
        if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        if (Tool.isKeyboardShowing(panelLayout)) {
            Tool.hideKeyboard(panelLayout);
        }

        if (!searchEditText.getText().toString().isEmpty()) {
            searchEditText.setText("");
        }

        // We need to update the history in case an external event created new items
        // (for instance, installed a new app, got a phone call or simply clicked on a favorite)
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.mReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent");
        //Set the intent so KISS can tell when it was launched as an assistant
        setIntent(intent);

        if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        // This is called when the user press Home again while already browsing MainActivity
        // onResume() will be called right after, hiding the kissbar if any.
        // http://developer.android.com/reference/android/app/Activity.html#onNewIntent(android.content.Intent)
        // Animation can't happen in this method, since the activity is not resumed yet, so they'll happen in the onResume()
        // https://github.com/Neamar/KISS/issues/569
        if (!searchEditText.getText().toString().isEmpty()) {
            Timber.i("Clearing search field");
            searchEditText.setText("");
        }

        // Close the backButton context menu
        closeContextMenu();
    }

    @Override
    public void onBackPressed() {
        if (mPopup != null) {
            mPopup.dismiss();
        } else {
            // If no kissmenu, empty the search bar
            // (this will trigger a new event if the search bar was already empty)
            // (which means pressing back in minimalistic mode with history displayed
            // will hide history again)
            searchEditText.setText("");
        }

        // Calling super.onBackPressed() will quit the launcher, only do this if KISS is not the user's default home.
        if (!Utilities.isKissDefaultLauncher(this)) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keycode, @NonNull KeyEvent e) {
        if (keycode == KeyEvent.KEYCODE_MENU) {
            // For devices with a physical menu button, we still want to display *our* contextual menu
            return true;
        }
        if (keycode != KeyEvent.KEYCODE_BACK) {
            searchEditText.requestFocus();
            searchEditText.dispatchKeyEvent(e);
        }
        return super.onKeyDown(keycode, e);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                return true;
            case R.id.wallpaper:
                Tool.hideKeyboard(this.getCurrentFocus());
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                startActivity(Intent.createChooser(intent, getString(R.string.menu_wallpaper)));
                return true;
            case R.id.preferences:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view.getId() == vgWidgerArea.getId()) {
            gd.onTouchEvent(event);
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mPopup != null && ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            dismissPopup();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }


    public void onFavoriteChange() {
    }

    /**
     * This function gets called on query changes.
     * It will ask all the providers for data
     * This function is not called for non search-related changes! Have a look at onDataSetChanged() if that's what you're looking for :)
     *
     * @param query the query on which to search
     */
    private void updateSearchRecords(String query) {
        if (TextUtils.isEmpty(query.trim())) {
            adapter.clear();
            return;
        }

        resetTask();
        dismissPopup();

        QuerySearcher querySearcher = new QuerySearcher(this, query);
        runTask(querySearcher);
    }

    public void updateResults(List<Result> results, String query) {
        adapter.updateResults(results, query);
    }

    public void runTask(Searcher task) {
        resetTask();
        searchTask = task;
        searchTask.executeOnExecutor(Searcher.SEARCH_THREAD);
    }

    public void resetTask() {
        if (searchTask != null) {
            searchTask.cancel(true);
            searchTask = null;
        }
    }

    @Override
    public void temporarilyDisableTranscriptMode() {

    }

    @Override
    public void updateTranscriptMode(int transcriptMode) {

    }

    /**
     * Call this function when we're leaving the activity after clicking a search result
     * to clear the search list.
     * We can't use onPause(), since it may be called for a configuration change
     */
    @Override
    public void launchOccurred() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        Tool.hideKeyboard(searchEditText);
        if (searchEditText.hasFocus()) {
            searchEditText.clearFocus();
        }
    }

    public void registerPopup(ListPopup popup) {
        if (mPopup == popup)
            return;
        dismissPopup();
        mPopup = popup;
        popup.setVisibilityHelper(systemUiVisibilityHelper);
        popup.setOnDismissListener(() -> MainActivity.this.mPopup = null);
    }

    @Override
    public void showDialog(DialogFragment dialog) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        systemUiVisibilityHelper.onWindowFocusChanged(hasFocus);
    }

    public void dismissPopup() {
        if (mPopup != null)
            mPopup.dismiss();
    }
}
