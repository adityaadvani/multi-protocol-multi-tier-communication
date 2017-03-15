//package tsapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import sun.security.krb5.internal.HostAddresses;

/**
 * class client creates an instance for client machine. It has instance methods
 * to get the time from server using the TCP and UDP transport protocols. It has
 * instance methods to update server time using the TCP and UDP transport
 * protocols.
 *
 * @author Aditya Advani
 */
class client {

    // instance variables
    String server_address;
    int server_port;
    boolean udp;
    boolean tcp;
    boolean utc;
    String time;
    String uname;
    String pword;
    private Socket c;
    private DatagramSocket d;

    /**
     * public constructor for initializing instance variables
     *
     * @param c_server_address
     * @param c_server_port
     * @param c_udp
     * @param c_tcp
     * @param c_utc
     * @param c_time
     * @param c_uname
     * @param c_pword
     * @throws IOException
     */
    public client(
            String c_server_address,
            int c_server_port,
            boolean c_udp,
            boolean c_tcp,
            boolean c_utc,
            String c_time,
            String c_uname,
            String c_pword
    ) throws IOException {
        this.server_address = c_server_address;
        this.server_port = c_server_port;
        this.udp = c_udp;
        this.tcp = c_tcp;
        this.utc = c_utc;
        this.time = c_time;
        this.uname = c_uname;
        this.pword = c_pword;

    }

    /**
     * Method connect contains the main business logic for requesting to get and
     * set time using the TCP and the UDP protocols.
     * @throws IOException
     */
    public void connect() throws IOException, ParseException {

        String hops =""; 
        boolean get_set;
        boolean protocol;
        if ("".equals(time)) {
            get_set = true;
        } else {
            get_set = false;
        }

// TCP requests follow this block
        if (tcp == true) {

            //initializing client protocol
            protocol = true;

            //establish connection
            this.c = new Socket(server_address, server_port);
            System.out.println("Just connected to " + c.getRemoteSocketAddress());

            //create input and output streams
            OutputStream outToServer = c.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            InputStream inFromServer = c.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            //start the time check for RTT calculation
            long rtt1 = System.currentTimeMillis();
            hops=hops+"Started from client machine: " + InetAddress.getLocalHost() + "xxx";

            //send the data to the server using TCP protocol
            out.writeUTF("" + protocol);
            out.writeUTF("" + get_set);
            out.writeUTF("" + time);
            out.writeUTF("" + pword);
            out.writeUTF("" + uname);
            out.writeUTF(hops);
            
            //get and send time just before hopping
            long hop_time = System.currentTimeMillis();            
            out.writeUTF(""+hop_time);

            //get the response from the server
            hops = in.readUTF();
            String response = in.readUTF();

            //split the hops string forming individual hop sequences
            String[] s = hops.split("xxx");

            //stop time check to find RTT
            long rtt2 = System.currentTimeMillis();
            long rtt = rtt2 - rtt1;

            //classify the server response to appropriate type
            if (get_set == true) {
                
                //response contains returned time
                int T = Integer.parseInt(response);
                if (utc == false) {
                    
                    //utc format disabled
                    System.out.println(get_calendar_time(T));
                    System.out.println("RTT: " + rtt + "ms");
                    System.out.println("\nhops:");
                    for (int i = 0; i < s.length; i++) {
                        System.out.println(s[i]);
                    }
                    System.out.println("\ntotal hops = " + (s.length - 1));
                } else {
                    
                    //utc format enabled
                    System.out.println("Time from server: " + T);
                    System.out.println("RTT: " + rtt + "ms");
                    System.out.println("\nhops:");
                    for (int i = 0; i < s.length; i++) {
                        System.out.println(s[i]);
                    }
                    System.out.println("\ntotal hops = " + (s.length - 1));
                }
            } else {
                //set time ACK/NAK response
                System.out.println(response);
                System.out.println("RTT: " + rtt + "ms");
                System.out.println("\nhops:");
                for (int i = 0; i < s.length; i++) {
                    System.out.println(s[i]);
                }
                System.out.println("\ntotal hops = " + (s.length - 1));
            }
        }

// UDP requests follow this block
        if (udp == true) {

            //initialize the client protocol
            protocol = false;
            hops=hops+"Started from client machine: " + InetAddress.getLocalHost() + "xxx";

            //establish connection
            d = new DatagramSocket();
            InetAddress address = InetAddress.getByName(server_address);
            byte[] send = new byte[256];
            byte[] receive = new byte[256];

            //start time check for RTT
            long rtt1 = System.currentTimeMillis();

            //send data to the server
            send = ("" + protocol).getBytes();
            DatagramPacket protocolPacket = new DatagramPacket(send, send.length, address, server_port);
            d.send(protocolPacket);

            send = ("" + get_set).getBytes();
            DatagramPacket getsetPacket = new DatagramPacket(send, send.length, address, server_port);
            d.send(getsetPacket);

            send = (time).getBytes();
            DatagramPacket newtime = new DatagramPacket(send, send.length, address, server_port);
            d.send(newtime);

            send = (uname).getBytes();
            DatagramPacket user_uname = new DatagramPacket(send, send.length, address, server_port);
            d.send(user_uname);

            send = (pword).getBytes();
            DatagramPacket user_pword = new DatagramPacket(send, send.length, address, server_port);
            d.send(user_pword);

            send = (hops).getBytes();
            DatagramPacket hop = new DatagramPacket(send, send.length, address, server_port);
            d.send(hop);
            
            //get and send time just before next hop
            long hop_time = System.currentTimeMillis();
            send = (""+hop_time).getBytes();
            DatagramPacket hoptime = new DatagramPacket(send, send.length, address, server_port);
            d.send(hoptime);

            // get response from the server
            DatagramPacket hopp = new DatagramPacket(receive, receive.length);
            d.receive(hopp);
            hops = new String(hopp.getData(), 0, hopp.getLength()).trim();

            DatagramPacket resp = new DatagramPacket(receive, receive.length);
            d.receive(resp);
            String response = new String(resp.getData(), 0, resp.getLength()).trim();

            //split the hops string forming individual hop sequences
            String[] s = hops.split("xxx");

            //stop time check for RTT
            long rtt2 = System.currentTimeMillis();
            long rtt = rtt2 - rtt1;

            //classify server response type
            if (get_set == true) {
                
                //response received is returned time
                int T = Integer.parseInt(response);

                if (utc == false) {
                    
                    //utc format is disabled
                    System.out.println(get_calendar_time(T));
                    System.out.println("RTT: " + rtt + "ms");
                    System.out.println("\nhops:");
                    for (int i = 0; i < s.length; i++) {
                        System.out.println(s[i]);
                    }
                    System.out.println("\ntotal hops = " + (s.length - 1));
                } else {
                    
                    //utc format is enabled
                    System.out.println("Time from server: " + T);
                    System.out.println("RTT: " + rtt + "ms");
                    System.out.println("\nhops:");
                    for (int i = 0; i < s.length; i++) {
                        System.out.println(s[i]);
                    }
                    System.out.println("\ntotal hops = " + (s.length - 1));
                }
            } else {
                
                //set time ACK/NAK response
                System.out.println(response);
                System.out.println("RTT: " + rtt + "ms");
                System.out.println("\nhops:");
                    for (int i = 0; i < s.length; i++) {
                        System.out.println(s[i]);
                    }
                    System.out.println("\ntotal hops = " + (s.length - 1));
            }
            d.close();
        }
    }

    /**
     * This method makes use of the inbuilt Calendar class to give the time in
     * calendar format when the input provided to it is in seconds.
     *
     * @param T
     * @return
     * @throws ParseException
     */
    public String get_calendar_time(int T) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = format.parse("01/01/1970 00:00:00");
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.SECOND, T);
        Date d = c.getTime();
        return "Time from server: " + d.toString();
    }
}