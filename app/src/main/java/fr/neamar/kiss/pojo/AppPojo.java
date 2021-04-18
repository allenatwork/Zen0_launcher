package fr.neamar.kiss.pojo;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import fr.neamar.kiss.utils.BitmapUtils;
import fr.neamar.kiss.utils.UserHandle;

public final class AppPojo extends PojoWithTags {

    public static String getComponentName(String packageName, String activityName,
                                          UserHandle userHandle) {
        return userHandle.addUserSuffixToString(packageName + "/" + activityName, '#');
    }

    public final String packageName;
    public final String activityName;
    public final UserHandle userHandle;

    private boolean excluded;
    private boolean excludedFromHistory;
    private long customIconId = 0;
    public Drawable icon;

    public AppPojo(String id, String packageName, String activityName, UserHandle userHandle,
                   boolean isExcluded, boolean isExcludedFromHistory) {
        super(id);

        this.packageName = packageName;
        this.activityName = activityName;
        this.userHandle = userHandle;

        this.excluded = isExcluded;
        this.excludedFromHistory = isExcludedFromHistory;
    }

    public String getComponentName() {
        return getComponentName(packageName, activityName, userHandle);
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public boolean isExcludedFromHistory() {
        return excludedFromHistory;
    }

    public void setExcludedFromHistory(boolean excludedFromHistory) {
        this.excludedFromHistory = excludedFromHistory;
    }

    public void setCustomIconId(long iconId) {
        customIconId = iconId;
    }

    public long getCustomIconId()
    {
        return customIconId;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = new BitmapDrawable(BitmapUtils.drawableToBitmap(icon));
        this.icon = icon;
    }
}
