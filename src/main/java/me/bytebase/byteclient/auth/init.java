package me.bytebase.byteclient.auth;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.security.MessageDigest;
import java.util.Base64;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.List;

public class init {
    public static final String myClientId;

    static {
        String id;
        try {
            List<String> dataBits = new ArrayList<>();

            // mac address
            String mac = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(n -> {
                        try {
                            return n.getHardwareAddress() != null && !n.isLoopback();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .map(n -> {
                        try {
                            byte[] macBytes = n.getHardwareAddress();
                            StringBuilder sb = new StringBuilder();
                            for (byte b : macBytes) sb.append(String.format("%02X", b));
                            return sb.toString();
                        } catch (Exception e) {
                            return "unknownmac";
                        }
                    }).orElse("nomac");

            dataBits.add("mac=" + mac);

            // hostname
            try {
                InetAddress ip = InetAddress.getLocalHost();
                dataBits.add("host=" + ip.getHostName());
            } catch (Exception ignored) {}

            // sys props
            dataBits.add("user=" + System.getProperty("user.name"));
            dataBits.add("os=" + System.getProperty("os.name"));
            dataBits.add("arch=" + System.getProperty("os.arch"));
            dataBits.add("java=" + System.getProperty("java.version"));
            dataBits.add("home=" + System.getProperty("user.home"));

            // hash it
            String raw = String.join("|", dataBits);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(raw.getBytes(StandardCharsets.UTF_8));

            id = Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            id = Base64.getEncoder().encodeToString(("error-" + UUID.randomUUID()).getBytes());
        }

        myClientId = id;

        // 📋 copy to clipboard
        try {
            System.setProperty("java.awt.headless", "false");
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(myClientId), null);
        } catch (Exception e) {
            System.err.println("clipboard failed: " + e.getMessage());
        }
    }

    public static void initAuthSystem() {
        tick.startTick();
    }
}
