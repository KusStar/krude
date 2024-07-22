package android.app;

import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.IInterface;

import java.util.List;

// https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/app/ActivityManager.java
public interface IActivityManager extends IInterface {
    /**
     * Returns a list of application processes that are running on the device.
     *
     * <p><b>Note: this method is only intended for debugging or building
     * a user-facing process management UI.</b></p>
     *
     * @return Returns a list of RunningAppProcessInfo records, or null if there are no
     * running processes (it will not return an empty list).  This list ordering is not
     * specified.
     */
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();

    void forceStopPackage(String packageName, int userId);

    /**
     * Return information about the memory usage of one or more processes.
     *
     * <p><b>Note: this method is only intended for debugging or building
     * a user-facing process management UI.</b></p>
     *
     * <p>As of {@link android.os.Build.VERSION_CODES#Q Android Q}, for regular apps this method
     * will only return information about the memory info for the processes running as the
     * caller's uid; no other process memory info is available and will be zero.
     * Also of {@link android.os.Build.VERSION_CODES#Q Android Q} the sample rate allowed
     * by this API is significantly limited, if called faster the limit you will receive the
     * same data as the previous call.</p>
     *
     * @param pids The pids of the processes whose memory usage is to be
     *             retrieved.
     * @return Returns an array of memory information, one for each
     * requested pid.
     */
    Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids);

    abstract class Stub extends Binder implements IActivityManager {
        public static IActivityManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }
}
