
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 21DNS
 */
public class DNSRelay {
    private static Map<String, String> domainNameIpMap=map();

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
    public static HashMap<String ,String> map(){
        String fileName = "src/dnsrelay.txt";
        File file = new File(fileName);
        FileInputStream fis;
        HashMap<String, String> Sites = new HashMap<>(100);
        try {
            fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                String[] contentList = line.split(" ");
                if (contentList.length < 2) {
                    continue;
                }
                Sites.put(contentList[1], contentList[0]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Sites;
    }
    static Map<String, String> getDomainNameIpMap() {
        return domainNameIpMap;
    }

    static void setDomainIpMap(Map<String, String> m) {
        domainNameIpMap = m;
    }

    static DatagramSocket getSocket() {
        return socket;
    }
}
