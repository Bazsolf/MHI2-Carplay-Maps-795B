package de.vw.mib.carplay.clustercontrol;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import de.vw.mib.bap.mqbab2.common.api.APIFactory;
import de.vw.mib.bap.mqbab2.common.api.APIFactoryInterface;
import de.vw.mib.bap.mqbab2.common.api.navigation.NavigationService;

public final class Mhi2ClusterControlBridge {
    private static final int KEYBOARD_MFW = 4;
    private static final int LOOPBACK_PORT = 7032;
    private static final int QUEUE_SIZE = 16;
    private static final Object LOCK = new Object();
    private static final Mhi2ClusterKeyState STATE = new Mhi2ClusterKeyState();
    private static final int[] QUEUE = new int[QUEUE_SIZE];
    private static int queueHead;
    private static int queueTail;
    private static int queueCount;
    private static boolean workerStarted;

    private Mhi2ClusterControlBridge() {}

    public static boolean isCarPlayMapActiveOnCluster() {
        try {
            // 1. Is CarPlay AltScreen video active?
            File ready = new File("/ramdisk/carplay-altstream.ready");
            if (!ready.exists()) {
                return false;
            }
            // 2. Is navigation map visible in the cluster (kombi)?
            APIFactoryInterface api = APIFactory.getAPIFactory();
            if (api == null) {
                return false;
            }
            NavigationService nav = api.getNavigationService();
            if (nav == null) {
                return false;
            }
            // 1 = visible in kombi, 0 = invisible (Driving Data, Audio, etc.)
            return nav.getKombiMapVisibility() == 1;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean onKey2(int keyPanel, int keyId, int keyState, int p4, int p5) {
        if (keyPanel != KEYBOARD_MFW) {
            return false;
        }
        if (keyId != Mhi2ClusterKeyState.KEY_UP && keyId != Mhi2ClusterKeyState.KEY_DOWN) {
            return false;
        }

        // Only consume key and send zoom if CarPlay Map is actively showing in the cluster
        if (!isCarPlayMapActiveOnCluster()) {
            return false;
        }

        synchronized (LOCK) {
            startWorkerLocked();
            int action = STATE.onKey(keyId, keyState, System.currentTimeMillis());
            if (action != Mhi2ClusterKeyState.ACTION_NONE) {
                enqueueLocked(action);
            }
            LOCK.notifyAll();
        }
        // Consumed: swallow so DSIKeyPanelDispatcher does NOT forward to the cluster!
        return true;
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
                int timerAction = STATE.onTimer(System.currentTimeMillis());
                if (timerAction != Mhi2ClusterKeyState.ACTION_NONE) {
                    enqueueLocked(timerAction);
                }
                if (queueCount > 0) {
                    action = dequeueLocked();
                } else {
                    long waitMillis = STATE.millisUntilLongAction(System.currentTimeMillis());
                    try {
                        if (waitMillis > 0L) {
                            LOCK.wait(waitMillis);
                        } else {
                            LOCK.wait();
                        }
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
