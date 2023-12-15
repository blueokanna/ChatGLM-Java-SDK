package top.pulselink.chatglm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;

public class SyncTime {

    public long TimeToSync() {
        long timestamp = Instant.now().toEpochMilli();
        try {
            if (getNTPTime() - timestamp < 1000) {
                return timestamp;
            } else {
                return getNTPTime();
            }
        } catch (IOException e) {
            System.out.println("Failed to sync time: " + e.getMessage());
            return timestamp;
        }
    }

    private long getNTPTime() throws IOException {
        int port = 123;
        InetAddress address = InetAddress.getByName("ntp.aliyun.com");

        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf = new byte[48];
            buf[0] = 0x1B;
            DatagramPacket requestPacket = new DatagramPacket(buf, buf.length, address, port);
            socket.send(requestPacket);

            DatagramPacket responsePacket = new DatagramPacket(new byte[48], 48);
            socket.receive(responsePacket);

            byte[] rawData = responsePacket.getData();
            long secondsSince1900 = 0;
            for (int i = 40; i <= 43; i++) {
                secondsSince1900 = (secondsSince1900 << 8) | (rawData[i] & 0xff);
            }
            return (secondsSince1900 - 2208988800L) * 1000;
        }
    }
}
