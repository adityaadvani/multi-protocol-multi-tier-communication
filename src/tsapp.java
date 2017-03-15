//package tsapp;

import java.io.*;
import java.text.ParseException;

/**
 * class tsapp is the main class of this application. It validates and processes
 * the mandatory and optional command line arguments and instantiates the
 * requested machine instances, with the desired parameters entered using the
 * command line, from amongst an option between the client, proxy-server and
 * origin server machines.
 *
 * @author Aditya Advani
 */
public class tsapp {

    /**
     * parameters are categorized depending on the instance to be created.
     *
     * for client instance:
     *
     * @param -c create client machine instance.
     * @param server address of origin server to communicate with.
     * @param port destination port for communicating with server.
     * @param -u (optional)client will use UDP to communicate with server.
     * @param -t (optional)client will use TCP to communicate with server.
     * @param -z (optional)use UTC time format to display to user.
     * @param -T (optional)set server time(UTC). requires username and password.
     * @param --user (optional)credentials to use for authenticating user.
     * @param --pass (optional)credentials to use for authenticating user.
     * @param -n (optional)number of consecutive times to query the server.
     *
     * for proxy-server instance:
     * @param -p create proxy-server machine instance.
     * @param server address of origin server to communicate with.
     * @param UDP_Port UDP listening port to service client connections.
     * @param TCP_Port TCP listening port to service client connections.
     * @param -u (optional)use UDP to communicate with server.
     * @param -t (optional)use TCP to communicate with server.
     * @param --proxy-udp (optional)UDP port to communicate with server.
     * @param --proxy-tcp (optional)TCP port to communicate with server.
     *
     * for server instance:
     * @param -s create server machine instance.
     * @param -T initial value of server time.
     * @param UDP_Port UDP listening port to service client connections.
     * @param TCP_Port TCP listening port to service client connections.
     * @param --user (optional)credentials required by client to update time.
     * @param --pass (optional)credentials required by client to update time.
     */
    public static void main(String[] args) throws IOException, ParseException {

// Command Line Argument Validation.
        int arglen = args.length;
        int i = 0; // argument iterator.

// Client argument validation.
        if (args[0].equals("-c")) {

            // Validate minimum and maximum argument length bounds
            if (arglen < 3 || arglen > 13) {
                client_usage();
            }

            // declare variables
            boolean c_udp = false; // udp = true => tcp = false
            boolean c_tcp = false; // tcp = true => udp = false
            boolean c_utc = false; // utc = false => calendar format
            String c_time = ""; // time from user to update on server
            String c_uname = ""; // username from user for authentication
            String c_pword = ""; // password from user for authentication
            int c_loops = 1; // number of times to query for time from server
            i = 2; // beginning index of optional arguments

            // scan for optional arguments
            while (i < arglen - 1) {

                // scan for udp transport protocol
                if (args[i].equals("-u")) {
                    c_udp = true;
                    i++;
                    if (c_tcp == true) {
                        System.err.println("\nCannot run both transport "
                                + "protocols. Please select only one.\n");
                        client_usage();
                    }
                }

                // scan for tcp transport protocol
                if (args[i].equals("-t")) {
                    c_tcp = true;
                    i++;
                    if (c_udp == true) {
                        System.err.println("\nCannot run both transport "
                                + "protocols. Please select only one.\n");
                        client_usage();
                    }
                }

                // scan for time format
                if (args[i].equals("-z")) {
                    c_utc = true;
                    i++;
                }

                // scan for time and validate format
                if (args[i].equals("-T")) {
                    i++;
                    if (i == (arglen - 1)) {
                        System.err.println("\nInvalid arguments.\n");
                        client_usage();
                    }
                    c_time = args[i];
                    i++;
                    if (c_time.length() > 25) {
                        System.err.println("\nWrong time format. Please"
                                + " enter proper format. Enter only positive "
                                + "integers for seconds.");
                        client_usage();
                    } else {
                        for (int z = 0; z < c_time.length(); z++) {
                            if ((int) c_time.charAt(z) < 48
                                    || (int) c_time.charAt(z) > 57) {
                                    System.err.println("\nWrong time format. "
                                            + "Please enter proper format. "
                                            + "Enter only positive integers for "
                                            + "seconds.");
                                    client_usage();
                                
                            }
                        }
                    }

                }

                // scan for username for authentication
                if (args[i].equals("--user")) {
                    i++;
                    if (i == (arglen - 1)) {
                        System.err.println("\nInvalid arguments.\n");
                        client_usage();
                    }
                    c_uname = args[i];
                    i++;
                    if (c_uname.equals("--pass")
                            || c_uname.equals("--user")
                            || c_uname.equals("-u")
                            || c_uname.equals("-t")
                            || c_uname.equals("-z")
                            || c_uname.equals("-T")
                            || c_uname.equals("-n")) {
                        System.err.println("\nInvalid user credentials.\n");
                        client_usage();
                    }
                }

                // scan for password for authentication
                if (args[i].equals("--pass")) {
                    i++;
                    if (i == (arglen - 1)) {
                        System.err.println("\nInvalid arguments.\n");
                        client_usage();
                    }
                    c_pword = args[i];
                    i++;
                    if (c_pword.equals("--pass")
                            || c_pword.equals("--user")
                            || c_pword.equals("-u")
                            || c_pword.equals("-t")
                            || c_pword.equals("-z")
                            || c_pword.equals("-T")
                            || c_pword.equals("-n")) {
                        System.err.println("\nInvalid user credentials.\n");
                        client_usage();
                    }
                }

                // scan for times to query
                if (args[i].equals("-n")) {
                    i++;
                    if (i == (arglen - 1)) {
                        System.err.println("\nInvalid arguments.\n");
                        client_usage();
                    }
                    for (int z = 0; z < args[i].length(); z++) {
                        if ((int) args[i].charAt(z) < 48
                                || (int) args[i].charAt(z) > 57) {
                            System.err.println("\nInvalid argument. Loop "
                                    + "count can be only a decimal integer."
                                    + "\n");
                            client_usage();
                        }
                    }
                    c_loops = Integer.parseInt(args[i]);
                    i++;
                }

                // scan for invalid arguments
                if (!(args[i].equals("-u"))
                        && !(args[i].equals("-t"))
                        && !(args[i].equals("-z"))
                        && !(args[i].equals("-T"))
                        && !(args[i].equals("--user"))
                        && !(args[i].equals("--pass"))
                        && !(args[i].equals("-n"))
                        && (i != arglen - 1)) {
                    System.err.println("\nIllegal argument entered. Please "
                            + "enter only valid arguments.\n");
                    client_usage();
                }

            }

            // validate for authentication requirements
            if (!("".equals(c_time))
                    && ("".equals(c_pword) || "".equals(c_uname))) {
                System.err.println("\nChanging time requires username and "
                        + "password.\n");
                client_usage();
            }

            // fallback transport protocol
            if (c_udp == false && c_tcp == false) {
                c_udp = true;
            }

            // address of server to connect to
            String c_server_address = args[1];

            // validate user input port number
            if (args[arglen - 1].length() != 4) {
                System.err.println("\nInvalid port number.\n");
                client_usage();
            }
            for (int z = 0; z < 4; z++) {
                if ((int) args[arglen - 1].charAt(z) < 48
                        || (int) args[arglen - 1].charAt(z) > 57) {
                    System.err.println("\nInvalid port number.\n");
                    client_usage();
                }
            }
            int c_port = Integer.parseInt(args[arglen - 1]);

            // Cross checking arguments
            // -c server --user aditya -n 50 --pass advani -u -T 55:43:11 -z 5000
            System.out.println("");
            //display processed values
            String client_str="CLIENT MACHINE:\nServer address: "
                    + c_server_address + "\nc_udp: " + c_udp + "\nc_tcp: "
                    + c_tcp + "\nc_utc: " + c_utc + "\ntime: " + c_time
                    + "\nusernme: " + c_uname + "\npassword: " + c_pword
                    + "\nloops: " + c_loops + "\nport: " + c_port;
            System.out.println(client_str);

            for (int j = 0; j < c_loops; j++) {
                //instantialte client object.
                client c = new client(
                        c_server_address,
                        c_port,
                        c_udp,
                        c_tcp,
                        c_utc,
                        c_time,
                        c_uname,
                        c_pword
                );

                System.out.println("\n");
                System.out.println("for loop call: " + (j + 1));
                c.connect();
            }
        }

// Proxy argument validation.
        if (args[0].equals("-p")) {

            // Validate minimum and maximum argument length bounds
            if (arglen < 4 || arglen > 9) {
                proxy_usage();
            }

            // declare variables
            String p_server_address = args[1]; // server address to connect to
            boolean p_udp = false; // override to udp transport protocol
            boolean p_tcp = false; // override to tcp transport protocol
            int p_udp_toserver_port = 0; // udp port for connecting to server
            int p_tcp_toserver_port = 1; // tcp port for connecting to server
            String protocol; // to state which protocol will be used.
            i = 2; // beginning index for optional arguments

            //scan for optional arguments
            while (i < (arglen - 2)) {

                // scan for udp transport protocol override
                if (args[i].equals("-u")) {
                    p_udp = true;
                    i++;
                    if (p_tcp == true) {
                        System.err.println("\nCannot run both transport "
                                + "protocols. Please select only one.\n");
                        proxy_usage();
                    }
                }

                // scan for tcp transport protocol override
                if (args[i].equals("-t")) {
                    p_tcp = true;
                    i++;
                    if (p_udp == true) {
                        System.err.println("\nCannot run both transport "
                                + "protocols. Please select only one.\n");
                        proxy_usage();
                    }
                }

                // scan for and validate udp port connecting to server
                if (args[i].equals("--proxy-udp")) {
                    i++;
                    if (i == (arglen - 2)) {
                        System.err.println("\nInvalid arguments.\n");
                        proxy_usage();
                    }
                    if (args[i].length() != 4) {
                        System.err.println("\nInvalid UDP port number for "
                                + "connecting to server.\n");
                        proxy_usage();
                    }
                    for (int z = 0; z < 4; z++) {
                        if ((int) args[arglen - 2].charAt(z) < 48
                                || (int) args[arglen - 2].charAt(z) > 57) {
                            System.err.println("\nInvalid UDP port number for "
                                    + "connecting to server.\n");
                            proxy_usage();
                        }
                    }
                    p_udp_toserver_port = Integer.parseInt(args[i]);
                    i++;
                }

                // scan for and validate tcp port connecting to server
                if (args[i].equals("--proxy-tcp")) {
                    i++;
                    if (i == (arglen - 2)) {
                        System.err.println("\nInvalid arguments.\n");
                        proxy_usage();
                    }
                    if (args[i].length() != 4) {
                        System.err.println("\nInvalid TCP port number for "
                                + "connecting to server.\n");
                        proxy_usage();
                    }
                    for (int z = 0; z < 4; z++) {
                        if ((int) args[arglen - 2].charAt(z) < 48
                                || (int) args[arglen - 2].charAt(z) > 57) {
                            System.err.println("\nInvalid TCP port number for "
                                    + "connecting to server.\n");
                            proxy_usage();
                        }
                    }
                    p_tcp_toserver_port = Integer.parseInt(args[i]);
                    i++;
                }

                // scan for invalid arguments
                if (!(args[i].equals("-u"))
                        && !(args[i].equals("-t"))
                        && !(args[i].equals("--proxy-udp"))
                        && !(args[i].equals("--proxy-tcp"))
                        && !(args[i].equals("-t"))
                        && (i != (arglen - 2))) {
                    System.err.println("\nIllegal argument entered. Please "
                            + "enter only valid arguments.\n");
                    proxy_usage();
                }
            }

            // validate transport protocol override and connection ports
            if (p_tcp == true && p_tcp_toserver_port == 1) {
                System.err.println("\nSince -t is specified, "
                        + "proxy-to-server TCP port is required\n");
                proxy_usage();
            }
            if (p_udp == true && p_udp_toserver_port == 0) {
                System.err.println("\nSince -u is specified, "
                        + "proxy-to-server UDP port is required\n");
                proxy_usage();
            }
            if (p_tcp == false && p_udp_toserver_port == 0) {
                System.err.println("\nSince -t is not specified, "
                        + "proxy-to-server UDP port is required\n");
                proxy_usage();
            }
            if (p_udp == false && p_tcp_toserver_port == 1) {
                System.err.println("\nSince -u is not specified, "
                        + "proxy-to-server TCP port is required\n");
                proxy_usage();
            }
            if (p_tcp == false && p_udp == false) {
                System.out.println("We will use client's transport protocol.");
            }

            // identify protocol to be used.
            if (p_udp == true) {
                protocol = "UDP";
            } else if (p_tcp == true) {
                protocol = "TCP";
            } else {
                protocol = "Client";
            }

            // validate udp client service port
            if (args[arglen - 2].length() != 4) {
                System.err.println("\nInvalid UDP port number.\n");
                proxy_usage();
            }
            for (int z = 0; z < 4; z++) {
                if ((int) args[arglen - 2].charAt(z) < 48
                        || (int) args[arglen - 2].charAt(z) > 57) {
                    System.err.println("\nInvalid UDP port number.\n");
                    proxy_usage();
                }
            }
            int p_udp_port = Integer.parseInt(args[arglen - 2]);

            // validate tcp client service port
            if (args[arglen - 1].length() != 4) {
                System.err.println("\nInvalid TCP port number.\n");
                proxy_usage();
            }
            for (int z = 0; z < 4; z++) {
                if ((int) args[arglen - 1].charAt(z) < 48
                        || (int) args[arglen - 1].charAt(z) > 57) {
                    System.err.println("\nInvalid TCP port number.\n");
                    proxy_usage();
                }
            }
            int p_tcp_port = Integer.parseInt(args[arglen - 1]);

            // validate activated ports for conflicts
            if (p_udp_port == p_tcp_port
                    || p_udp_port == p_udp_toserver_port
                    || p_udp_port == p_tcp_toserver_port
                    || p_tcp_port == p_udp_toserver_port
                    || p_tcp_port == p_tcp_toserver_port
                    || p_udp_toserver_port == p_tcp_toserver_port) {
                System.err.println("\nTwo or more ports cannot be the same."
                        + "\n");
                proxy_usage();
            }

            // Cross checking arguments
            // -p server --proxy-tcp 5001 --proxy-udp 5000 5002 5003
            System.out.println("");
            // display processed values
            String proxy_str="PROXY MACHINE:\nserver address: "
                    + p_server_address + "\nUDP to server: " + p_udp + "\nTCP "
                    + "to server: " + p_tcp + "\nUDP to server port: "
                    + p_udp_toserver_port + "\nTCP to server port: "
                    + p_tcp_toserver_port + "\nUDP listening port: "
                    + p_udp_port + "\nTCP listening port: " + p_tcp_port;
            System.out.println(proxy_str);

            //Instanciate proxy object
            proxyserver p = new proxyserver(
                    p_server_address,
                    p_udp,
                    p_tcp,
                    p_udp_port,
                    p_tcp_port,
                    p_udp_toserver_port,
                    p_tcp_toserver_port
            );

        }

// Server argument validation.
        if (args[0].equals("-s")) {

            // Validate minimum and maximum argument length bounds
            if (arglen < 5 || arglen > 9) {
                server_usage();
            }

            // Validate for time key position
            if (!(args[1].equals("-T"))) {
                System.err.println("\nInvalid arguments.\n");
                server_usage();
            }

            // declare variables
            String s_uname = ""; // authentication key for username
            String s_pword = ""; // authentication key for password
            boolean s_changeable = false; // 'true' if time can be updated
            i = 3; // benning index of optional arguments

            // scan for optional arguments
            while (i < arglen - 2) {

                // scan for username authentication key
                if (args[i].equals("--user")) {
                    i++;
                    if (i == (arglen - 2)) {
                        System.err.println("\nInvalid arguments.\n");
                        server_usage();
                    }
                    s_uname = args[i];
                    i++;
                    if (s_uname.equals("--pass")) {
                        System.err.println("\nInvalid user credentials.\n");
                        server_usage();
                    }
                }

                // scan for password authentication key
                if (args[i].equals("--pass")) {
                    i++;
                    if (i == (arglen - 2)) {
                        System.err.println("\nInvalid arguments.\n");
                        server_usage();
                    }
                    s_pword = args[i];
                    i++;
                    if (s_pword.equals("--user")) {
                        System.err.println("\nInvalid user credentials.\n");
                        server_usage();
                    }
                }

                // scan for invalid arguments
                if (!(args[i].equals("--user"))
                        && !(args[i].equals("--pass"))
                        && (i != (arglen - 2))) {
                    System.err.println("\nIllegal argument entered. Please "
                            + "enter only valid arguments.\n");
                    server_usage();
                }
            }

            // determine if time updation is permitted
            if (!("".equals(s_uname)) && !("".equals(s_pword))) {
                s_changeable = true;
            }

            // scan and vaidate server initial time
            String s_time = args[2];
            if (s_time.length() > 25) {
                System.err.println("\nWrong time format. Please"
                        + " enter proper format. Enter only positive integers "
                        + "for seconds.");
                server_usage();
            } else {
                for (int z = 0; z < s_time.length(); z++) {
                    if ((int) s_time.charAt(z) < 48
                            || (int) s_time.charAt(z) > 57) {
                            System.err.println("\nWrong time format. "
                                    + "Please enter proper format. Enter only "
                                    + "positive integers for seconds");
                            server_usage();
                    }
                }
            }

            // validate udp client service port
            if (args[arglen - 2].length() != 4) {
                System.err.println("\nInvalid UDP port number.\n");
                server_usage();
            }
            for (int z = 0; z < 4; z++) {
                if ((int) args[arglen - 2].charAt(z) < 48
                        || (int) args[arglen - 2].charAt(z) > 57) {
                    System.err.println("\nInvalid UDP port number.\n");
                    server_usage();
                }
            }
            int s_udp_port = Integer.parseInt(args[arglen - 2]);

            //validate tcp client service port
            if (args[arglen - 1].length() != 4) {
                System.err.println("\nInvalid TCP port number.\n");
                server_usage();
            }
            for (int z = 0; z < 4; z++) {
                if ((int) args[arglen - 1].charAt(z) < 48
                        || (int) args[arglen - 1].charAt(z) > 57) {
                    System.err.println("\nInvalid TCP port number.\n");
                    server_usage();
                }
            }
            int s_tcp_port = Integer.parseInt(args[arglen - 1]);

            // scan for port conflicts
            if (s_udp_port == s_tcp_port) {
                System.err.println("\nUDP port and TCP port cannot be the same."
                        + "\n");
                server_usage();
            }

            // Cross checking arguments
            // -s -T 20 --user aditya --pass advani 5000 5001
            System.out.println("");

            //display processed values
            String server_str="SERVER MACHINE:\ntime: " + s_time + "\nusername"
                    + ": " + s_uname + "\npassword: " + s_pword + "\nchangeable"
                    + ": " + s_changeable + "\nUDP port: " + s_udp_port + "\n"
                    + "TCP port: " + s_tcp_port;
            System.out.println(server_str);

            // instanciate server object
            server s = new server(
                    s_time,
                    s_uname,
                    s_pword,
                    s_udp_port,
                    s_tcp_port,
                    s_changeable
            );
            server t = s;
            t.mode = true;
            server u = s;
            u.mode = false;
            s.connect();
        }

    }

// Hidden operations
    /**
     * method client_usage displays the usage instruction message to run client
     * instance and follows it with error stack.
     */
    public static void client_usage() {
        System.err.println("Usage: java tsapp -c server [options] port");
        System.err.println("\nserver = server address\nport = server port"
                + "\n\noptions:\n-u = uses UDP to communicate with server"
                + "\n-t = uses TCP to communicate with server"
                + "\n-z = uses UTC time format instead of default calendar "
                + "format\n-T <time> = set time on server(Requires --user and "
                + "--pass)\n--user <username> = credentials required to "
                + "modify (set) server time\n--pass <password> = credentials "
                + "required to modify (set) server time\n-n <#> = number of "
                + "consecutive queries\n");
        throw new IllegalArgumentException();
    }

    /**
     * method proxy_usage displays the usage instruction message to run
     * proxy-server instance and follows it with error stack.
     */
    public static void proxy_usage() {
        System.err.println("Usage: java tsapp -p server [options] UDP_Port "
                + "TCP_Port");
        System.err.println("\nserver = server address\nUDP_Port = UDP listening"
                + " port to service client\nTCP_Port = TCP listening port to "
                + "service client"
                + "\n\noptions:\n-u = uses UDP to communicate with server "
                + "regardless of client transport protocol\n-t = uses TCP to "
                + "communicate with server regardless of client transport "
                + "protocol\n--proxy-udp <#> = UDP port to communicate with "
                + "origin server (required if -t is not specified)\n--proxy-tcp"
                + " <#> = TCP port to communicate with origin server (required"
                + " if -u is not specified)\n");
        throw new IllegalArgumentException();
    }

    /**
     * method server_usage displays the usage instruction message to run origin
     * server instance and follows it with error stack.
     */
    public static void server_usage() {
        System.err.println("Usage: java tsapp -s -T <time> [options] UDP_Port "
                + "TCP_Port");
        System.err.println("\n-T <time> = current server time\nUDP_Port = UDP "
                + "listening port to service client\nTCP_Port = TCP listening "
                + "port to service client"
                + "\n\noptions:\n--user <username> =  credentials required to "
                + "modify (set) server time via client call. Absence implies no"
                + " client is authorized to modify the server time\n--pass"
                + " <password> =  credentials required to modify (set) server"
                + " time via client call. Absence implies no client is"
                + " authorized to modify the server time\n");
        throw new IllegalArgumentException();
    }

}