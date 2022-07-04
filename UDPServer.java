import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author 21
 */
public class UDPServer {
    private static DatagramSocket socket;
    private FileLock fileLock;
    private FileChannel channel;

    public UDPServer() {
        //设置socket，监听端口53
        try {
            socket = new DatagramSocket(53);
        } catch (SocketException e) {
            e.printStackTrace();
        }


    }

    public void start() {

        System.out.println("Starting......\n");
        int count=0;
        while (true) {
            HashMap<String, String> map = map();
            System.out.println("=======================");
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                //卡住直到收到receive
                socket.receive(request);
                //输出客户端的dns请求数据
                InetAddress sourceIpAddr = request.getAddress();
                int sourcePort = request.getPort();
                System.out.println("\nsourceIpAddr = " + sourceIpAddr.toString() + "\nsourcePort = " + sourcePort);
                //分析dns数据包格式
                Message indata = new Message(request.getData());
                System.out.println("\nindata = " + indata);
                String ym=indata.getQuestion().getName().toString();
                ym=ym.substring(0,ym.length()-1);
                System.out.println("ym:::"+ym);
                Record question = indata.getQuestion();
                System.out.println("question = " + question);
                String domain = indata.getQuestion().getName().toString();
                System.out.println("domain = " + domain);
                //解析域名
                if(map.containsKey(ym)){

                    InetAddress answerIpAddr = InetAddress.getByName(map.get(ym));

                    Message outdata = (Message)indata.clone();
                    if(map.get(ym).contains("0.0.0.0")) {
                        System.out.println("**********************************************************");
                        Header reply = outdata.getHeader();
                        reply.setRcode(3);
                        outdata.setHeader(reply);
                    }

                    //由于接收到的请求为A类型，因此应答也为ARecord。查看Record类的继承，发现还有AAAARecord(ipv6)，CNAMERecord等
                    Record answer = new ARecord(question.getName(), question.getDClass(), 64, answerIpAddr);
                    outdata.addRecord(answer, Section.ANSWER);
                    //发送消息给客户端
                    System.out.println("data:" + outdata);
                    byte[] buf = outdata.toWire();
                    DatagramPacket response = new DatagramPacket(buf, buf.length, sourceIpAddr, sourcePort);
                    socket.send(response);
                    System.out.println("socket:" + socket);


                }
                else{
                    InetAddress answerIpAddr = InetAddress.getByName(domain);
                    Message outdata = (Message)indata.clone();
                    //由于接收到的请求为A类型，因此应答也为ARecord。查看Record类的继承，发现还有AAAARecord(ipv6)，CNAMERecord等
                    Record answer = new ARecord(question.getName(), question.getDClass(), 64, answerIpAddr);
                    outdata.addRecord(answer, Section.ANSWER);
                    //发送消息给客户端
                    System.out.println("data:"+outdata);
                    byte[] buf = outdata.toWire();
                    DatagramPacket response = new DatagramPacket(buf, buf.length, sourceIpAddr, sourcePort);
                    socket.send(response);
                    System.out.println("socket:"+socket);
                    if(count %2 ==1) {
                        String path = "src/dnsrelay.txt";
                        String word = "\n" + answerIpAddr.getHostAddress() + " " + ym;
                        BufferedWriter out = new BufferedWriter(
                                new OutputStreamWriter(new FileOutputStream(path, true)));
                        out.write(word);
                        out.close();
                    }
                }

            } catch (SocketException e) {
                System.out.println("SocketException:");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("IOException:");
                e.printStackTrace();
            }
            count++;
            try {
                if(fileLock!=null||channel!=null){

                        fileLock.release();


                    channel.close();
                }
                else{
                    System.out.println("wrong");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public HashMap<String ,String> map(){
        String fileName = "src/dnsrelay.txt";
        File file = new File(fileName);
        FileInputStream fis;
        HashMap<String, String> Sites = new HashMap<>(100);
        try {
            fis = new FileInputStream(file);
            channel=fis.getChannel();
            fileLock=channel.lock(0L, channel.size(), true);
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
}
