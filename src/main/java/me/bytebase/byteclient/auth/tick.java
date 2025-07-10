package me.bytebase.byteclient.auth;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

import me.bytebase.byteclient.util.fe;
import net.minecraft.client.MinecraftClient;
import org.json.JSONObject;

public class tick {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static int failCount = 0; // count fails

    public static void startTick() {
        
        scheduler.scheduleAtFixedRate(() -> {
            
            try {
                String urlStr = "https://s1.cubzyn.net/other/byteclient/authorize/?apik=835ywnsdiughv8shiuw78g5t9wh5tr89hwe8hfrt89str&code=" + init.myClientId;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) sb.append((char) ch);
                reader.close();

                // decrypt twice base64 encoded + AES-256-CBC
                byte[] enc = Base64.getDecoder().decode(Base64.getDecoder().decode(sb.toString()));

                // key same as php (raw sha256)
                byte[] key = java.util.Arrays.copyOfRange(
                        java.security.MessageDigest.getInstance("SHA-256")
                                .digest("B98sYisdfTiasdf9Eyhds9ychsC8y9tLdfh89shIsidgh9sEijgfodjNidsh9goigT".getBytes()),
                        0, 32
                );

                // iv must be first 16 bytes of hex sha256 string (php uses hex substr)
                String ivHex = bytesToHex(
                        java.security.MessageDigest.getInstance("SHA-256")
                                .digest("ByteClient78579026872975982uy983592998sdf9".getBytes())
                );
                byte[] iv = ivHex.substring(0, 16).getBytes();

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

                String decrypted = new String(cipher.doFinal(enc));
                JSONObject json = new JSONObject(decrypted);
                long remoteTime = Long.parseLong(json.getString("time"));
                long now = System.currentTimeMillis() / 1000L;

                if (now - remoteTime > 200) {

                    // move destruction to main thread
                    MinecraftClient.getInstance().execute(fe::en);
                } else {
                    failCount = 0; // reset on success
                    me.bytebase.byteclient.auth.data.premium = Boolean.valueOf((json.getString("premium")));
                    data.username=((json.getString("name")));
                    data.discordId=((json.getString("uids")));
                }

            } catch (Exception ignored) {
                
                failCount++;
                if (failCount > 5) me.bytebase.byteclient.util.fe.en();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
