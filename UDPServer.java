import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import java.io.*;
import java.net.*;
import java.util.HashMap;


public class UDPServer implements Runnable{
    private final DatagramPacket request;
    private final DatagramSocket socket;


    private HashMap<String ,String> ipmap=map();
    public UDPServer(DatagramPacket p,DatagramSocket s) {
        request = p;
        socket = s;
    }

    public void run() {
         System.out.println("=======================");
         System.out.println(Thread.currentThread().getName());
                try {
                    //输出客户端的dns请求数据
                    InetAddress sourceIpAddr = request.getAddress();
                    int sourcePort = request.getPort();
                    System.out.println("\nsourceIpAddr = " + sourceIpAddr.toString() + "\nsourcePort = " + sourcePort);
                    //分析dns数据包格式
                    Message indata = new Message(request.getData());
                    System.out.println("\nindata = " + indata);
                    String ym = indata.getQuestion().getName().toString();
                    ym = ym.substring(0, ym.length() - 1);
                    System.out.println("ym:::" + ym);
                    Record question = indata.getQuestion();
                    System.out.println("question = " + question);
                    String domain = indata.getQuestion().getName().toString();
                    System.out.println("domain = " + domain);
                    //解析域名
                    if (ipmap.containsKey(ym)) {

                        InetAddress answerIpAddr = InetAddress.getByName(ipmap.get(ym));

                        Message outdata = (Message) indata.clone();
                        if (ipmap.get(ym).contains("0.0.0.0")) {
                            System.out.println("**********************************************************");
                            Header reply = outdata.getHeader();
                            reply.setRcode(3);
                            outdata.setHeader(reply);
                        }
                        System.out.println("**********************************************************");
                        //由于接收到的请求为A类型，因此应答也为ARecord。查看Record类的继承，发现还有AAAARecord(ipv6)，CNAMERecord等
                        Record answer = new ARecord(question.getName(), question.getDClass(), 64, answerIpAddr);
                        outdata.addRecord(answer, Section.ANSWER);
                        //发送消息给客户端
                        System.out.println("data:" + outdata);
                        byte[] buf = outdata.toWire();
                        DatagramPacket response = new DatagramPacket(buf, buf.length, sourceIpAddr, sourcePort);
                        socket.send(response);
                        System.out.println("socket:" + socket);
                    } else {
                        InetAddress answerIpAddr = InetAddress.getByName(domain);
                        Message outdata = (Message) indata.clone();
                        //由于接收到的请求为A类型，因此应答也为ARecord。查看Record类的继承，发现还有AAAARecord(ipv6)，CNAMERecord等
                        Record answer = new ARecord(question.getName(), question.getDClass(), 64, answerIpAddr);
                        outdata.addRecord(answer, Section.ANSWER);
                        //发送消息给客户端
                        System.out.println("data:" + outdata);
                        byte[] buf = outdata.toWire();
                        DatagramPacket response = new DatagramPacket(buf, buf.length, sourceIpAddr, sourcePort);
                        socket.send(response);
                        System.out.println("socket:" + socket);
                            ipmap = update(ipmap, ym, answerIpAddr.getHostAddress());
                    }
                    System.out.println(ipmap);

                } catch (SocketException e) {
                    System.out.println("SocketException:");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("IOException:");
                    e.printStackTrace();
                }
    }

    public HashMap<String ,String> map(){
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
    public synchronized HashMap<String ,String> update(HashMap<String ,String> ipmap,String domain, String ip){
        ipmap.put(domain,ip);
        return ipmap;
    }
}
