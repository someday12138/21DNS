package DNS;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class dnsrelay {
    public static void main(String[] args){
        ExecutorService sPool = Executors.newFixedThreadPool(20);
        UDPServer udp= new UDPServer();
        sPool.execute(udp);
    }
}
