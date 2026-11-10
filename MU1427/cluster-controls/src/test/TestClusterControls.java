package test;

import de.vw.mib.carplay.clustercontrol.Mhi2ClusterControlBridge;
import java.io.File;

public class TestClusterControls {
    static int checks = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FAIL: " + msg);
        checks++;
    }

    public static void main(String[] args) throws Exception {
        // Test 1: When altstream file doesn't exist, isCarPlayMapActiveOnCluster is false
        File alt = new File("/ramdisk/carplay-altstream.ready");
        if (alt.exists()) alt.delete();
        check(!Mhi2ClusterControlBridge.isCarPlayMapActiveOnCluster(), "Inactive when altstream.ready missing");

        // Test 2: onKey2 with wrong key panel (e.g. 1 instead of 4) returns false (not consumed)
        check(!Mhi2ClusterControlBridge.onKey2(1, 38, 1, 0, 0), "Ignore non-MFW keyPanel");

        // Test 3: onKey2 with other keys (e.g. Volume up, Left, Right) returns false
        check(!Mhi2ClusterControlBridge.onKey2(4, 20, 1, 0, 0), "Ignore non-up/down keyId");
        check(!Mhi2ClusterControlBridge.onKey2(4, 37, 1, 0, 0), "Ignore key 37");

        // Test 4: onKey2 with Key 38/39 when NOT in CarPlay Map returns false (normal passthrough)
        check(!Mhi2ClusterControlBridge.onKey2(4, 38, 1, 0, 0), "Passthrough Key 38 when not in CarPlay Map");
        check(!Mhi2ClusterControlBridge.onKey2(4, 39, 1, 0, 0), "Passthrough Key 39 when not in CarPlay Map");

        System.out.println("{\"cluster_controls_tests_passed\":" + checks + "}");
    }
}
