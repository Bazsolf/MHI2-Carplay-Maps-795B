package de.vw.mib.carplay.clustercontrol;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public final class Mhi2ClusterControlBridge {
    private static final int LOOPBACK_PORT = 7032;
    private static final int QUEUE_SIZE = 16;
    private static final int MAX_STEPS_PER_EVENT = 4;
    private static final Object LOCK = new Object();
    private static final int[] QUEUE = new int[QUEUE_SIZE];
    private static int queueHead;
    private static int queueTail;
    private static int queueCount;
    private static boolean workerStarted;
    private static int mapScaleCalls;
    private static int mapScaleSent;

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

    /**
     * Legacy DSIKeyPanelDispatcher hook kept as a no-op so scrolling AID menus
     * never triggers unwanted map zoom; zoom is driven cleanly via BAP MapScale.
     */
    public static boolean onKey2(int keyPanel, int keyId, int keyState, int p4, int p5) {
        return false;
    }

    /**
     * Called from hooked BAP NavSD MapScale.setGetProperty(MapScale_SetGet).
     * Negative steps decrease scale (zoom in); positive steps increase scale (zoom out).
     */
    public static void onMapScaleSteps(int steps) {
        try {
            if (steps == 0) {
                return;
            }
            boolean ready = isCarPlayAltStreamReady();
            int action = steps < 0 ? Mhi2ClusterKeyState.ACTION_ZOOM_IN : Mhi2ClusterKeyState.ACTION_ZOOM_OUT;
            int count = steps < 0 ? -steps : steps;
            if (count > MAX_STEPS_PER_EVENT) {
                count = MAX_STEPS_PER_EVENT;
            }
            synchronized (LOCK) {
                mapScaleCalls++;
                if (ready) {
                    startWorkerLocked();
                    for (int i = 0; i < count; i++) {
                        enqueueLocked(action);
                    }
                    mapScaleSent += count;
                    LOCK.notifyAll();
                }
                writeStatusLocked(steps, ready, commandFor(action));
            }
        } catch (Throwable t) {
            // Never interrupt stock BAP MapScale handling
        }
    }

    private static void writeStatusLocked(int steps, boolean ready, String cmd) {
        try {
            File dir = new File("/ramdisk");
            if (!dir.isDirectory()) {
                return;
            }
            FileOutputStream fos = new FileOutputStream("/ramdisk/carplay-mapscale.status");
            try {
                String line = "calls=" + mapScaleCalls + " sent=" + mapScaleSent
                        + " ready=" + (ready ? 1 : 0) + " last_steps=" + steps
                        + " last_cmd=" + cmd + "\n";
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
