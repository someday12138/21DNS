import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * @author 21DNS
 */
public class DNSRelay {

    private static DatagramSocket socket;
    public static void main(String[] args){
        System.out.println("Starting......\n");
        try {
            socket = new DatagramSocket(53);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        ExecutorService sPool = Executors.newFixedThreadPool(20);
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sPool.execute(new UDPServer(packet,socket));
        }
    }
}
