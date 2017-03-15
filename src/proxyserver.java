
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package tsapp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class proxyserver creates a multi-threaded instance for Proxy-server machine.
 * It has instance methods to get the time from the origin server and forward it
 * to the requesting client machine, using the TCP and UDP transport protocols.
 * It has instance methods to update server time as requested by an authorized
 * user, using the TCP and UDP transport protocols.
 *
 * @author Aditya Advani
 */
class proxyserver extends Thread {

    // instance variables
    String server_address;
    boolean udp_requested_on_proxy;
    boolean tcp_requested_on_proxy;
    int udp_port_for_client;
    int tcp_port_for_client;
    int toServer_udp_port;
    int toServer_tcp_port;

    /**
     * public constructor for initializing instance variables from tsapp class.
     *
     * @param p_server_address
     * @param udp
     * @param tcp
     * @param p_for_client_udp_port
     * @param p_for_client_tcp_port
     * @param p_to_server_udp_port
     * @param p_to_server_tcp_port
     * @throws IOException
     */
    public proxyserver(
            String p_server_address,
            boolean udp,
            boolean tcp,
            int p_for_client_udp_port,
            int p_for_client_tcp_port,
            int p_to_server_udp_port,
            int p_to_server_tcp_port
    ) throws IOException {
        this.server_address = p_server_address;
        this.udp_requested_on_proxy = udp;
        this.tcp_requested_on_proxy = tcp;
        this.udp_port_for_client = p_for_client_udp_port;
        this.tcp_port_for_client = p_for_client_tcp_port;
        this.toServer_tcp_port = p_to_server_tcp_port;
        this.toServer_udp_port = p_to_server_udp_port;

        proxyserverTCP proxy_TCP = new proxyserverTCP(server_address, udp_requested_on_proxy, tcp_requested_on_proxy, tcp_port_for_client, tcp_port_for_client, toServer_udp_port, toServer_tcp_port);
        proxy_TCP.start();

        proxyserverUDP proxy_UDP = new proxyserverUDP(server_address, udp_requested_on_proxy, tcp_requested_on_proxy, udp_port_for_client, tcp_port_for_client, toServer_udp_port, toServer_tcp_port);
        proxy_UDP.start();
    }
}

class proxyserverTCP extends Thread {

    //thread flags
    static boolean flag_for_TCP_first_run = true;

    //system variables
    static String server_address;
    static boolean udp_requested_on_proxy;
    static boolean tcp_requested_on_proxy;
    static int tcp_port_for_client;
    static int udp_port_for_client;
    static int toServer_udp_port;
    static int toServer_tcp_port;

    //communication variables
    String protocol;
    String get_set;
    String time;
    String uname;
    String pword;
    String hops;
    long start_time;

    //netwoorking variables
    ServerSocket ss;
    Socket proxy_acting_as_server;
    boolean tcp_udp_decision_flag;
    InetAddress client_address;
    int client_port;
    DatagramSocket d;

    proxyserverTCP() {
    }

    public proxyserverTCP(String server_address1, boolean udp_requested_on_proxy1, boolean tcp_requested_on_proxy1, int tcp_port_for_client1, int udp_port_for_client1, int toServer_udp_port1, int toServer_tcp_port1) {
        server_address = server_address1;
        tcp_port_for_client = tcp_port_for_client1;
        udp_port_for_client = udp_port_for_client1;
        toServer_tcp_port = toServer_tcp_port1;
        toServer_udp_port = toServer_udp_port1;
        tcp_requested_on_proxy = tcp_requested_on_proxy1;
        udp_requested_on_proxy = udp_requested_on_proxy1;
    }

    proxyserverTCP(Socket proxy_acting_as_server1, String protocol1, long time1) {
        this.proxy_acting_as_server = proxy_acting_as_server1;
        this.protocol = protocol1;
        this.start_time=time1;
    }

    /**
     * Method to spawn the parent thread for all TCP connection requests.
     */
    public void startTCPproxy() {
        try {
            ss = new ServerSocket(tcp_port_for_client);
            System.out.println("Proxy Server waiting for a client at "
                    + ss.getLocalSocketAddress() + " on port " + tcp_port_for_client);
            ss.setSoTimeout(100000);
            while (true) {
                proxy_acting_as_server = ss.accept();
                long time_entry = System.currentTimeMillis();
                System.out.println("\n\naccepted connection");
                DataInputStream proxy_as_server_in = new DataInputStream(proxy_acting_as_server.getInputStream());
                flag_for_TCP_first_run = false;
                protocol = proxy_as_server_in.readUTF();
                System.out.println();
                System.out.println("Protocol from client: " + protocol);
                new Thread(new proxyserverTCP(proxy_acting_as_server, protocol, time_entry)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // Fot TCP
        if (flag_for_TCP_first_run == true) {
            startTCPproxy();
        } else {
            // Get time
            Socket proxy_acting_as_client;
            try {
                System.out.println("created new thread for tcp");
                sleep(1000);
                if ((udp_requested_on_proxy == false && tcp_requested_on_proxy == false && protocol.equals("true")) || tcp_requested_on_proxy == true) {
                    System.out.println("client protocol = TCP OR Proxy Selection = TCP");

                    proxy_acting_as_client = new Socket(server_address, toServer_tcp_port);
                    DataOutputStream proxy_as_client_out = new DataOutputStream(proxy_acting_as_client.getOutputStream());
                    DataInputStream proxy_as_client_in = new DataInputStream(proxy_acting_as_client.getInputStream());

                    DataOutputStream proxy_as_server_out = new DataOutputStream(proxy_acting_as_server.getOutputStream());
                    DataInputStream proxy_as_server_in = new DataInputStream(proxy_acting_as_server.getInputStream());                

                    System.out.println("Client uses TCP: " + protocol);
                    get_set = proxy_as_server_in.readUTF();
                    System.out.println("Client wants to get time: " + get_set);
                    time = proxy_as_server_in.readUTF();
                    System.out.println("Client wants to update time to: " + time);
                    pword = proxy_as_server_in.readUTF();
                    System.out.println("Client password: " + pword);
                    uname = proxy_as_server_in.readUTF();
                    System.out.println("Client username: " + uname);
                    hops = proxy_as_server_in.readUTF();
                    String t = proxy_as_server_in.readUTF();
                    long t_hop=Long.parseLong(t)-start_time;
                    hops = hops +t_hop+ "ms\tProxy-Server Machine: " + InetAddress.getLocalHost() + " at port: " + tcp_port_for_client + "xxx";
                    System.out.println("hop updated");
                    System.out.println();

                    System.out.println("Sending data to Server's TCP service");
                    proxy_as_client_out.writeUTF("" + protocol);
                    proxy_as_client_out.writeUTF("" + get_set);
                    proxy_as_client_out.writeUTF("" + time);
                    proxy_as_client_out.writeUTF("" + pword);
                    proxy_as_client_out.writeUTF("" + uname);
                    proxy_as_client_out.writeUTF(hops);
                    long hop_time = System.currentTimeMillis(); 
                    proxy_as_client_out.writeUTF(""+hop_time);

                    System.out.println("Receiving data from Server's TCP service");
                    hops = proxy_as_client_in.readUTF();
                    String response = proxy_as_client_in.readUTF();

                    System.out.println("Forwarding responses to client and terminating connection");
                    proxy_as_server_out.writeUTF(hops);
                    proxy_as_server_out.writeUTF(response);

                } else {

                    System.out.println("\"client protocol = UDP OR Proxy Selection = UDP\"");

                    DataOutputStream proxy_as_server_out = new DataOutputStream(proxy_acting_as_server.getOutputStream());
                    DataInputStream proxy_as_server_in = new DataInputStream(proxy_acting_as_server.getInputStream());

                    System.out.println("Client uses TCP: " + protocol);
                    get_set = proxy_as_server_in.readUTF();
                    System.out.println("Client wants to get time: " + get_set);
                    time = proxy_as_server_in.readUTF();
                    System.out.println("Client wants to update time to: " + time);
                    pword = proxy_as_server_in.readUTF();
                    System.out.println("Client password: " + pword);
                    uname = proxy_as_server_in.readUTF();
                    System.out.println("Client username: " + uname);
                    hops = proxy_as_server_in.readUTF();
                    String t = proxy_as_server_in.readUTF();
                    long t_hop=Long.parseLong(t)-start_time;
                    hops = hops +t_hop+ "ms\tProxy-Server Machine: " + InetAddress.getLocalHost() + " at port: " + tcp_port_for_client + "xxx";
                    System.out.println("hop updated");
                    System.out.println();

                    System.out.println("Sending data to Server's UDP service");
                    d = new DatagramSocket();
                    InetAddress address = InetAddress.getByName(server_address);
                    byte[] send = new byte[256];
                    byte[] receive = new byte[256];

                    send = ("" + protocol).getBytes();
                    DatagramPacket protocolPacket = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(protocolPacket);

                    send = ("" + get_set).getBytes();
                    DatagramPacket getsetPacket = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(getsetPacket);

                    send = (time).getBytes();
                    DatagramPacket newtime = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(newtime);

                    send = (pword).getBytes();
                    DatagramPacket user_uname = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(user_uname);

                    send = (uname).getBytes();
                    DatagramPacket user_pword = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(user_pword);

                    send = (hops).getBytes();
                    DatagramPacket hop = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(hop);
                    
                    long hop_time = System.currentTimeMillis();
                    send = (""+hop_time).getBytes();
                    DatagramPacket hoptime = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    d.send(hoptime);

                    System.out.println("Receiving data from Server's TCP service");
                    DatagramPacket hopp = new DatagramPacket(receive, receive.length);
                    d.receive(hopp);
                    hops = new String(hopp.getData(), 0, hopp.getLength()).trim();

                    DatagramPacket resp = new DatagramPacket(receive, receive.length);
                    d.receive(resp);
                    String response = new String(resp.getData(), 0, resp.getLength()).trim();

                    System.out.println("Forwarding responses to client and terminating connection");
                    proxy_as_server_out.writeUTF(hops);
                    proxy_as_server_out.writeUTF(response);

                }
            } catch (IOException ex) {
                Logger.getLogger(proxyserverTCP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(proxyserverTCP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class proxyserverUDP extends Thread {

    //thread flags
    static boolean flag_for_UDP_first_run = true;

    //system variables
    static String server_address;
    static boolean udp_requested_on_proxy;
    static boolean tcp_requested_on_proxy;
    static int udp_port_for_client;
    static int tcp_port_for_client;
    static int toServer_udp_port;
    static int toServer_tcp_port;

    //communication variables
    String protocol;
    String get_set;
    String time;
    String uname;
    String pword;
    String hops;
    String hop_time;
    long new_hop_time;

    //network variables
    ServerSocket ss;
    Socket proxy_acting_as_server;
    boolean tcp_udp_decision_flag;
    InetAddress client_address;
    int client_port;
    DatagramSocket d;

    proxyserverUDP() {
    }

    public proxyserverUDP(String server_address1, boolean udp_requested_on_proxy1, boolean tcp_requested_on_proxy1, int udp_port_for_client1, int tcp_port_for_client1, int toServer_udp_port1, int toServer_tcp_port1) {
        server_address = server_address1;
        tcp_port_for_client = tcp_port_for_client1;
        udp_port_for_client = udp_port_for_client1;
        toServer_tcp_port = toServer_tcp_port1;
        toServer_udp_port = toServer_udp_port1;
        tcp_requested_on_proxy = tcp_requested_on_proxy1;
        udp_requested_on_proxy = udp_requested_on_proxy1;
    }

    public proxyserverUDP(DatagramSocket d1,String protocol1,String get_set1,String time1,String uname1,String pword1,String hops1,String hop_t,InetAddress client_address1, int client_port1) {
        this.d = d1;
        this.protocol = protocol1;
        this.get_set=get_set1;
        this.time=time1;
        this.uname=uname1;
        this.pword=pword1;
        this.hops=hops1;
        this.hop_time=hop_t;
        this.client_address = client_address1;
        this.client_port = client_port1;
    }

    public void startUDPproxy() throws InterruptedException {
        try {
            System.out.println("started udp thread");
            d = new DatagramSocket(udp_port_for_client);
            byte[] receive = new byte[256];
            byte[] send = new byte[256];
            while (true) {
                sleep(1000);
                System.out.println("\nconnected to a client");
                flag_for_UDP_first_run = false;
                DatagramPacket proto = new DatagramPacket(
                        receive, receive.length);
                d.receive(proto);
                new_hop_time = System.currentTimeMillis();
                protocol = new String(proto.getData(), 0, proto.getLength()).trim();
                System.out.println("Client uses TCP Xprotocol: " + protocol);
                client_address = proto.getAddress();
                client_port = proto.getPort();
                
                DatagramPacket getset = new DatagramPacket(receive, receive.length);
                d.receive(getset);
                get_set = new String(getset.getData(), 0, getset.getLength()).trim();
                System.out.println("Client wants to get time: " + get_set);

                DatagramPacket newtime = new DatagramPacket(receive, receive.length);
                d.receive(newtime);
                time = new String(newtime.getData(), 0, newtime.getLength()).trim();
                System.out.println("Client wants to update time to: " + time);

                DatagramPacket newpass = new DatagramPacket(receive, receive.length);
                d.receive(newpass);
                pword = new String(newpass.getData(), 0, newpass.getLength()).trim();
                System.out.println("Client password: " + pword);

                DatagramPacket newuser = new DatagramPacket(receive, receive.length);
                d.receive(newuser);
                uname = new String(newuser.getData(), 0, newuser.getLength()).trim();
                System.out.println("Client username: " + uname);

                DatagramPacket hopp = new DatagramPacket(receive, receive.length);
                d.receive(hopp);
                hops = new String(hopp.getData(), 0, hopp.getLength()).trim();
                
                DatagramPacket hoptime = new DatagramPacket(receive, receive.length);
                d.receive(hoptime);
                String hopp_time = new String(hoptime.getData(), 0, hoptime.getLength()).trim();
                
                hop_time=""+(Long.parseLong(hopp_time)-new_hop_time);
                
                
                new Thread(new proxyserverUDP(d, protocol, get_set,time,uname,pword,hops,hop_time, client_address, client_port)).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (flag_for_UDP_first_run == true) {
            try {
                startUDPproxy();
            } catch (InterruptedException ex) {
                Logger.getLogger(proxyserverUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // Get time
            if ((udp_requested_on_proxy == false && tcp_requested_on_proxy == false && protocol.equals("false")) || udp_requested_on_proxy == true) {
                try {
                    sleep(1000);
                    DatagramSocket e;
                    byte[] receive = new byte[256];
                    byte[] send = new byte[256];

                    hops = hops + hop_time+"ms\tProxy-Server Machine: " + InetAddress.getLocalHost() + " at port: " + tcp_port_for_client + "xxx";
                    System.out.println("\n");
                    System.out.println("hop updated");
                    System.out.println();

                    System.out.println("Sending data to Server's UDP service");
                    e = new DatagramSocket();
                    InetAddress address = InetAddress.getByName(server_address);
                                        
                    send = ("" + protocol).getBytes();
                    DatagramPacket protocolPacket1 = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(protocolPacket1);
                    
                    send = ("" + get_set).getBytes();
                    DatagramPacket getsetPacket1 = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(getsetPacket1);

                    send = (time).getBytes();
                    DatagramPacket newtime1 = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(newtime1);

                    send = (pword).getBytes();
                    DatagramPacket user_pword1 = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(user_pword1);
                    
                    send = (uname).getBytes();
                    DatagramPacket user_uname1 = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(user_uname1);

                    send = (hops).getBytes();
                    DatagramPacket hop1 = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(hop1);
                    
                    long t = System.currentTimeMillis();
                    send = (""+t).getBytes();
                    DatagramPacket hoptime = new DatagramPacket(send, send.length, address, toServer_udp_port);
                    e.send(hoptime);
                    
                    System.out.println("Receiving data from Server's UDP service");
                    DatagramPacket hopp1 = new DatagramPacket(receive, receive.length);
                    e.receive(hopp1);
                    hops = new String(hopp1.getData(), 0, hopp1.getLength()).trim();

                    DatagramPacket resp = new DatagramPacket(receive, receive.length);
                    e.receive(resp);
                    String response = new String(resp.getData(), 0, resp.getLength()).trim();

                    System.out.println("Forwarding responses to client and terminating connection");
                    send = hops.getBytes();
                    DatagramPacket hop2 = new DatagramPacket(send, send.length, client_address, client_port);
                    d.send(hop2);
                    send = response.getBytes();
                    DatagramPacket resp1 = new DatagramPacket(send, send.length, client_address, client_port);
                    d.send(resp1);
                } catch (IOException ex) {
                    Logger.getLogger(proxyserverUDP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(proxyserverUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {

                try {
                    sleep(1000);
                    byte[] receive = new byte[256];
                    byte[] send = new byte[256];

                    hops = hops + hop_time+"ms\tProxy-Server Machine: " + InetAddress.getLocalHost() + " at port: " + tcp_port_for_client + "xxx";
                    System.out.println("\n");
                    System.out.println("hop updated");
                    System.out.println();

                    System.out.println("Sending data to Server's UDP service");
                    Socket proxy_acting_as_client;
                    proxy_acting_as_client = new Socket(server_address, toServer_tcp_port);
                    DataOutputStream proxy_as_client_out = new DataOutputStream(proxy_acting_as_client.getOutputStream());
                    DataInputStream proxy_as_client_in = new DataInputStream(proxy_acting_as_client.getInputStream());

                    proxy_as_client_out.writeUTF("" + protocol);
                    proxy_as_client_out.writeUTF("" + get_set);
                    proxy_as_client_out.writeUTF("" + time);
                    proxy_as_client_out.writeUTF("" + pword);
                    proxy_as_client_out.writeUTF("" + uname);
                    proxy_as_client_out.writeUTF(hops);
                    long t = System.currentTimeMillis();
                    proxy_as_client_out.writeUTF(""+t);
                    
                    System.out.println(hops);

                    System.out.println("Receiving data from Server's TCP service");
                    hops = proxy_as_client_in.readUTF();
                    String response = proxy_as_client_in.readUTF();

                    System.out.println("Forwarding responses to client and terminating connection");
                    send = hops.getBytes();
                    DatagramPacket hop2 = new DatagramPacket(send, send.length, client_address, client_port);
                    d.send(hop2);
                    send = response.getBytes();
                    DatagramPacket resp1 = new DatagramPacket(send, send.length, client_address, client_port);
                    d.send(resp1);

                } catch (IOException ex) {
                    Logger.getLogger(proxyserverUDP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(proxyserverUDP.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
