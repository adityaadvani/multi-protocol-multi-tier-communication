//package tsapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * class server creates an instance for server machine. It has instance methods
 * to forward the time to requesting clients using the TCP and UDP transport
 * protocols. It has instance methods to update time, as requested by authorized
 * users, using the TCP and UDP transport protocols.
 *
 * @author Aditya Advani
 */
class server implements Runnable {

    public static String T = "";
    // instance variables
    String time;
    String uname;
    String pword;
    int UDP_port;
    int TCP_port;
    boolean change;
    boolean mode;
    ServerSocket ss;
    DatagramSocket d;
    long start_time;

    /**
     * constructor for initializing instance variables from the tsapp class.
     *
     * @param s_time
     * @param s_uname
     * @param s_pword
     * @param s_UDP_port
     * @param s_TCP_port
     * @param s_change
     * @param s_mode
     * @throws IOException
     */
    public server(
            String s_time,
            String s_uname,
            String s_pword,
            int s_UDP_port,
            int s_TCP_port,
            boolean s_change,
            boolean s_mode
    ) throws IOException {
        this.time = s_time;
        this.uname = s_uname;
        this.pword = s_pword;
        this.TCP_port = s_TCP_port;
        this.UDP_port = s_UDP_port;
        this.change = s_change;
        this.mode = s_mode;
        T = this.time;
    }

    /**
     * constructor of the server Instance
     *
     * @param s_time
     * @param s_uname
     * @param s_pword
     * @param s_UDP_port
     * @param s_TCP_port
     * @param s_change
     * @throws IOException
     */
    public server(
            String s_time,
            String s_uname,
            String s_pword,
            int s_UDP_port,
            int s_TCP_port,
            boolean s_change
    ) throws IOException {
        this.time = s_time;
        this.uname = s_uname;
        this.pword = s_pword;
        this.TCP_port = s_TCP_port;
        this.UDP_port = s_UDP_port;
        this.change = s_change;
    }

    /**
     * server's Code for running TCP and UDP on two ports of the same machine
     * simultaneously using multi-threading.
     */
    public void run() {
        try {
            //Run as TCP
            if (mode == true) {
                ss = new ServerSocket(TCP_port);
                ss.setSoTimeout(1000000);
                System.out.println("TCP Service on server Started");

                while (true) {
                    //ready to accept connections
                    System.out.println();
                    System.out.println("Waiting for TCP client at " + ss.getLocalSocketAddress() + " on port " + ss.getLocalPort() + "...");
                    Socket s = ss.accept();
                    start_time = System.currentTimeMillis();

                    //Connected to client
                    System.out.println("connected to client at: " + s.getRemoteSocketAddress());
                    DataInputStream in = new DataInputStream(s.getInputStream());
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());

                    //Initial Information exchange
                    System.out.println();
                    String protocol = in.readUTF();
                    System.out.println("Client uses TCP: " + protocol);
                    String get_set = in.readUTF();
                    System.out.println("Client wants to get time: " + get_set);
                    String update_time = in.readUTF();
                    System.out.println("Client wants to update time to: " + update_time);
                    String user_pword = in.readUTF();
                    System.out.println("Client password: " + user_pword + ", server password: " + pword);
                    String user_uname = in.readUTF();
                    System.out.println("Client username: " + user_uname + ", server username: " + uname);
                    String hops = in.readUTF();
                    String t = in.readUTF();
                    long t_hop=Long.parseLong(t)-start_time;
                    hops = hops +t_hop+ "ms\tServer Machine: "+InetAddress.getLocalHost()+" at port: "+TCP_port;
                    System.out.println("hop updated");
                    
                    //Client wants to get time in TCP
                    if (get_set.equals("true")) {
                        out.writeUTF(hops);
                        out.writeUTF(T);
                        System.out.println("\nReturned time: " + T);

                        //Client wants to set time in TCP
                    } else {
                        if (change == false) {
                            out.writeUTF(hops);
                            out.writeUTF("Time change feature is disabled at server.");
                            System.out.println("Time change feature is disabled.");
                        } else {
                            if ((user_uname.equals(uname))
                                    && (user_pword.equals(pword))) {
                                T = update_time;
                                out.writeUTF(hops);
                                out.writeUTF("Time at server changed to: " + update_time);
                                System.out.println("Time changed to: " + update_time);

                            } else {
                                out.writeUTF(hops);
                                out.writeUTF("Invalid credentials.");
                                System.out.println("Invalid credentials.");

                            }
                        }
                    }
                }
            }

            //Run as UDP
            if (mode == false) {

                d = new DatagramSocket(UDP_port);
                byte[] receive = new byte[256];
                byte[] send = new byte[256];

                //Ready to accept packets
                System.out.println("UDP Service on server Started");
                while (true) {
                    System.out.println();
                    System.out.println("Waiting for UDP client at port: "+UDP_port+"...");
                    //Initial Information exchange
                    DatagramPacket proto = new DatagramPacket(receive, receive.length);
                    d.receive(proto);
                    start_time = System.currentTimeMillis();
                    String protocol = new String(proto.getData(), 0, proto.getLength()).trim();
                    System.out.println("Client uses TCP: " + protocol);

                    DatagramPacket getset = new DatagramPacket(receive, receive.length);
                    d.receive(getset);
                    String get_set = new String(getset.getData(), 0, getset.getLength()).trim();
                    System.out.println("Client wants to get time: " + get_set);

                    DatagramPacket newtime = new DatagramPacket(receive, receive.length);
                    d.receive(newtime);
                    String update_time = new String(newtime.getData(), 0, newtime.getLength()).trim();
                    System.out.println("Client wants to update time to: " + update_time);

                    DatagramPacket newpass = new DatagramPacket(receive, receive.length);
                    d.receive(newpass);
                    String user_pword = new String(newpass.getData(), 0, newpass.getLength()).trim();
                    System.out.println("Client password: " + user_pword + ", server password: " + pword);

                    DatagramPacket newuser = new DatagramPacket(receive, receive.length);
                    d.receive(newuser);
                    String user_uname = new String(newuser.getData(), 0, newuser.getLength()).trim();
                    System.out.println("Client username: " + user_uname + ", server username: " + uname);

                    DatagramPacket hopp = new DatagramPacket(receive, receive.length);
                    d.receive(hopp);
                    String hops = new String(hopp.getData(), 0, hopp.getLength()).trim();

                    DatagramPacket hoptime = new DatagramPacket(receive, receive.length);
                    d.receive(hoptime);
                    String t = new String(hoptime.getData(), 0, hoptime.getLength()).trim();
                    
                    long t_hop=Long.parseLong(t)-start_time;
                    hops = hops +t_hop+ "ms\tServer Machine: "+InetAddress.getLocalHost()+" at port: "+UDP_port;
                    System.out.println("hops updated");
                    System.out.println("");
                    //get client address and port
                    InetAddress address = getset.getAddress();
                    int port = getset.getPort();

                    //Client wants to get time using UDP
                    if (get_set.equals("true")) {
                        send = hops.getBytes();
                        DatagramPacket hop = new DatagramPacket(send, send.length, address, port);
                        d.send(hop);
                        send = T.getBytes();
                        DatagramPacket sendtime = new DatagramPacket(send, send.length, address, port);
                        d.send(sendtime);
                        System.out.println("Returned time: " + T);

                        //Client wants to set time using UDP 
                    } else {
                        if (change == false) {

                            send = hops.getBytes();
                            DatagramPacket hop = new DatagramPacket(send, send.length, address, port);
                            d.send(hop);
                            send = ("Time change feature is disabled at server.").getBytes();
                            DatagramPacket resp = new DatagramPacket(send, send.length, address, port);
                            d.send(resp);
                            System.out.println("Time change feature is disabled.");

                        } else {
                            if ((user_uname.equals(uname))
                                    && (user_pword.equals(pword))) {
                                T = update_time;

                                send = hops.getBytes();
                                DatagramPacket hop = new DatagramPacket(send, send.length, address, port);
                                d.send(hop);
                                send = ("Time change to: " + update_time).getBytes();
                                DatagramPacket response = new DatagramPacket(send,send.length, address, port);
                                d.send(response);
                                System.out.println("Time changed to: " + update_time);

                            } else {
                                send = hops.getBytes();
                                DatagramPacket hop = new DatagramPacket(send, send.length, address, port);
                                d.send(hop);
                                send = ("Invalid credentials.").getBytes();
                                DatagramPacket response = new DatagramPacket(send,send.length, address, port);
                                d.send(response);
                                System.out.println("Invalid credentials.");
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Starts The TCP and UDP threads at the server Machine.
     *
     * @throws IOException
     */
    public void connect() throws IOException {
        Thread s_tcp = new Thread(
                new server(
                        this.time,
                        this.uname,
                        this.pword,
                        this.UDP_port,
                        this.TCP_port,
                        this.change,
                        true)
        );
        Thread s_udp = new Thread(
                new server(
                        this.time,
                        this.uname,
                        this.pword,
                        this.UDP_port,
                        this.TCP_port,
                        this.change,
                        false)
        );
        s_udp.start();
        s_tcp.start();

    }
}
