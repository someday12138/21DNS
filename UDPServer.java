import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import java.io.*;
import java.net.*;
import java.util.HashMap;



public class UDPServer implements Runnable{
    private final DatagramPacket request;
    private final DatagramSocket socket;
    private HashMap<String ,String> ipmap;

    public UDPServer(DatagramPacket p,DatagramSocket s,HashMap<String ,String> map) {
        request = p;
        socket = s;
        ipmap=map;
    }

    public void run() {
        {
            System.out.println(Thread.currentThread().getName());
            {
                System.out.println("=======================");
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
                        System.out.println("UPdating............");
                        update(ipmap, ym, answerIpAddr.getHostAddress());
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
        }
    }
    public synchronized void update(HashMap<String ,String> map,String domain, String ip){
        map.put(domain,ip);
        this.ipmap=map;
    }
}
