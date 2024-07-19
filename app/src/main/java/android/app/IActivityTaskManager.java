package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import java.util.List;

// https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/app/ActivityTaskManager.java
public interface IActivityTaskManager extends IInterface {
    /**
     * @return List of running tasks.
     * @hide
     */
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum);

    /**
     * @return List of running tasks that can be filtered by visibility in recents.
     * @hide
     */
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, boolean filterOnlyVisibleRecents);

    /**
     * @return List of running tasks that can be filtered by visibility in recents and keep intent
     * extra.
     * @hide
     */
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, boolean filterOnlyVisibleRecents, boolean keepIntentExtra);

    /**
     * @param displayId the target display id, or {@link INVALID_DISPLAY} not to filter by displayId
     * @return List of running tasks that can be filtered by visibility and displayId in recents
     * and keep intent extra.
     * @hide
     */
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, boolean filterOnlyVisibleRecents, boolean keepIntentExtra, int displayId);

    abstract class Stub extends Binder implements IActivityTaskManager {
        public static IActivityTaskManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }
}
