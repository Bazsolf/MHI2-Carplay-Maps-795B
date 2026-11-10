package de.vw.mib.carplay.clustercontrol;

final class Mhi2ClusterKeyState {
    static final int ACTION_NONE = 0;
    static final int ACTION_ZOOM_IN = 1;
    static final int ACTION_ZOOM_OUT = 2;
    static final int ACTION_MAP_PREVIOUS = 3;
    static final int ACTION_MAP_NEXT = 4;

    static final int KEY_UP = 38;
    static final int KEY_DOWN = 39;

    static final int STATE_RELEASED = 0;
    static final int STATE_PRESSED = 1;
    static final int STATE_LONG_PRESSED = 3;
    static final int STATE_LONG_PRESSED_2 = 4;
    static final int STATE_LONG_PRESSED_3 = 5;

    static final long LONG_PRESS_MILLIS = 2500L;

    private int activeKey = -1;
    private long pressedAtMillis;
    private boolean longActionSent;

    Mhi2ClusterKeyState() {}

    int onKey(int keyId, int keyState, long stamp) {
        if (keyId != KEY_UP && keyId != KEY_DOWN) {
            return ACTION_NONE;
        }
        if (keyState == STATE_PRESSED) {
            if (activeKey == keyId) {
                return ACTION_NONE;
            }
            activeKey = keyId;
            pressedAtMillis = stamp;
            longActionSent = false;
            return keyId == KEY_UP ? ACTION_ZOOM_IN : ACTION_ZOOM_OUT;
        }
        if (keyState == STATE_RELEASED) {
            if (activeKey == keyId) {
                activeKey = -1;
                longActionSent = false;
            }
            return ACTION_NONE;
        }
        if (keyState == STATE_LONG_PRESSED || keyState == STATE_LONG_PRESSED_2 || keyState == STATE_LONG_PRESSED_3) {
            if (activeKey < 0) {
                activeKey = keyId;
                pressedAtMillis = stamp;
                longActionSent = false;
                return ACTION_NONE;
            }
            if (activeKey == keyId) {
                return onTimer(stamp);
            }
        }
        return ACTION_NONE;
    }

    int onTimer(long stamp) {
        if (activeKey < 0 || longActionSent) {
            return ACTION_NONE;
        }
        if (stamp - pressedAtMillis < LONG_PRESS_MILLIS) {
            return ACTION_NONE;
        }
        longActionSent = true;
        return activeKey == KEY_UP ? ACTION_MAP_PREVIOUS : ACTION_MAP_NEXT;
    }

    long millisUntilLongAction(long stamp) {
        if (activeKey < 0 || longActionSent) {
            return 0L;
        }
        long remaining = LONG_PRESS_MILLIS - (stamp - pressedAtMillis);
        return remaining > 0L ? remaining : 1L;
    }
}
