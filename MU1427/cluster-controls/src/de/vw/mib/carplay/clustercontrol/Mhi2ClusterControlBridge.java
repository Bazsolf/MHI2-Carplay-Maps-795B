package de.vw.mib.carplay.clustercontrol;

import de.vw.mib.bap.mqbab2.common.api.APIFactory;
import de.vw.mib.bap.mqbab2.common.api.APIFactoryInterface;
import de.vw.mib.bap.mqbab2.common.api.navigation.NavigationService;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public final class Mhi2ClusterControlBridge {
    private static final int KEYBOARD_MFW = 4;
    private static final int LOOPBACK_PORT = 7032;
    private static final int QUEUE_SIZE = 16;
    private static final Object LOCK = new Object();
    private static final int[] QUEUE = new int[QUEUE_SIZE];
    private static int queueHead;
    private static int queueTail;
    private static int queueCount;
    private static boolean workerStarted;
    private static int heldKey = -1;
    private static int keyCalls;
    private static int keySent;

    private Mhi2ClusterControlBridge() {}

    public static boolean isCarPlayAltStreamReady() {
        try {
            if (Boolean.getBoolean("cpmap.test.ready")) {
                return true;
            }
            return new File("/ramdisk/carplay-altstream.ready").exists();
        } catch (Throwable t) {
            return false;
        }
    }

    public static int kombiMapVisibility() {
        try {
            if (Boolean.getBoolean("cpmap.test.ready")) {
                return 1;
            }
            APIFactoryInterface api = APIFactory.getAPIFactory();
            if (api == null) {
                return 0;
            }
            NavigationService nav = api.getNavigationService();
            if (nav == null) {
                return 0;
            }
            return nav.getKombiMapVisibility();
        } catch (Throwable t) {
            return -1;
        }
    }

    /**
     * Short-press steering-wheel up/down (keys 38/39 on MFW keyboard 4) sends zoom-in/zoom-out
     * over UDP 127.0.0.1:7032 when CarPlay AltStream and kombi map are active.
     * Repeats while held are ignored until key release; returns false so stock dispatch continues.
     */
    public static boolean onKey2(int keyPanel, int keyId, int keyState, int p4, int p5) {
        try {
            if (keyPanel != KEYBOARD_MFW) {
                return false;
            }
            if (keyId != Mhi2ClusterKeyState.KEY_UP && keyId != Mhi2ClusterKeyState.KEY_DOWN) {
                return false;
            }

            int action = Mhi2ClusterKeyState.ACTION_NONE;
            boolean ready = isCarPlayAltStreamReady();
            int vis = kombiMapVisibility();

            synchronized (LOCK) {
                if (keyState == Mhi2ClusterKeyState.STATE_PRESSED) {
                    if (heldKey != keyId) {
                        heldKey = keyId;
                        action = (keyId == Mhi2ClusterKeyState.KEY_UP)
                                ? Mhi2ClusterKeyState.ACTION_ZOOM_IN
                                : Mhi2ClusterKeyState.ACTION_ZOOM_OUT;
                    }
                } else if (keyState == Mhi2ClusterKeyState.STATE_RELEASED && heldKey == keyId) {
                    heldKey = -1;
                }

                if (action != Mhi2ClusterKeyState.ACTION_NONE) {
                    keyCalls++;
                    if (ready && vis == 1) {
                        startWorkerLocked();
                        enqueueLocked(action);
                        keySent++;
                        LOCK.notifyAll();
                    }
                    writeStatusLocked(ready, vis, keyId, keyState);
                }
            }
        } catch (Throwable t) {
            // Never disrupt stock DSIKeyPanel dispatch
        }
        return false;
    }

    private static void writeStatusLocked(boolean ready, int vis, int keyId, int keyState) {
        try {
            File dir = new File("/ramdisk");
            if (!dir.isDirectory()) {
                return;
            }
            FileOutputStream fos = new FileOutputStream("/ramdisk/carplay-keys.status");
            try {
                String line = "calls=" + keyCalls + " sent=" + keySent
                        + " ready=" + (ready ? 1 : 0) + " vis=" + vis
                        + " last_key=" + keyId + " last_state=" + keyState + "\n";
                fos.write(line.getBytes("US-ASCII"));
            } finally {
                fos.close();
            }
        } catch (Throwable t) {
            // Ignore on non-QNX / read-only hosts
        }
    }

    private static void startWorkerLocked() {
        if (workerStarted) {
            return;
        }
        Thread worker = new Thread(new Runnable() {
            public void run() {
                commandLoop();
            }
        }, "cpvc-cluster-control");
        worker.setDaemon(true);
        workerStarted = true;
        worker.start();
    }

    private static void enqueueLocked(int action) {
        if (queueCount == QUEUE_SIZE) {
            queueHead = (queueHead + 1) % QUEUE_SIZE;
            queueCount--;
        }
        QUEUE[queueTail] = action;
        queueTail = (queueTail + 1) % QUEUE_SIZE;
        queueCount++;
    }

    private static int dequeueLocked() {
        int action = QUEUE[queueHead];
        queueHead = (queueHead + 1) % QUEUE_SIZE;
        queueCount--;
        return action;
    }

    private static void commandLoop() {
        DatagramSocket socket = null;
        InetAddress host = null;
        while (true) {
            int action;
            synchronized (LOCK) {
                if (queueCount > 0) {
                    action = dequeueLocked();
                } else {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {}
                    continue;
                }
            }
            try {
                String cmd = commandFor(action);
                byte[] bytes = cmd.getBytes("US-ASCII");
                if (socket == null) {
                    socket = new DatagramSocket();
                }
                if (host == null) {
                    host = InetAddress.getByName("127.0.0.1");
                }
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, host, LOOPBACK_PORT);
                socket.send(packet);
            } catch (Throwable t) {
                if (socket != null) {
                    socket.close();
                }
                socket = null;
                host = null;
            }
        }
    }

    private static String commandFor(int action) {
        switch (action) {
            case Mhi2ClusterKeyState.ACTION_ZOOM_IN: return "zoom-in";
            case Mhi2ClusterKeyState.ACTION_ZOOM_OUT: return "zoom-out";
            case Mhi2ClusterKeyState.ACTION_MAP_PREVIOUS: return "map-previous";
            default: return "map-next";
        }
    }
}
