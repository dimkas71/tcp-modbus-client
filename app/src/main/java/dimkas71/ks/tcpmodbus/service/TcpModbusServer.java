package dimkas71.ks.tcpmodbus.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dimkas71 on 5/24/15.
 */
public class TcpModbusServer {

    private static final int PORT = 502;

    private static final long PAUSE_BETWEEN_MSGS = 10; //milliseconds
    private static ByteBuffer echoBuffer = ByteBuffer.allocate(1024);
    private static ConcurrentHashMap<Integer, SocketChannel> chm = new ConcurrentHashMap<>();

    private static int msg = 0;

    public static void start() throws IOException {

        //Create new selector
        final Selector selector = Selector.open();

        //Open a listener on each port, and register each one
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        final ServerSocket ss = ssc.socket();

        final InetSocketAddress address = new InetSocketAddress(PORT);

        ss.bind(address);

        //registers ACCEPT
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        sendMessagesToRandomClients();

        while (true) {
            selector.select();

            final Set<SelectionKey> selectedKeys = selector.selectedKeys();

            final Iterator<SelectionKey> it = selectedKeys.iterator();

            String msg = "";

            while (it.hasNext()) {
                final SelectionKey key = (SelectionKey) it.next();

                if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

                    //Accept new connection
                    final ServerSocketChannel newSSC = (ServerSocketChannel) key.channel();
                    final SocketChannel sc = newSSC.accept();
                    sc.configureBlocking(false);

                    //Add the new connection to the selector
                    sc.register(selector, SelectionKey.OP_READ);

                    //Add the socket channel to the list
                    chm.put(sc.hashCode(), sc);
                    it.remove();
                } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    //Read the data
                    final SocketChannel sc = (SocketChannel) key.channel();
                    int code = 0;

                    while ((code = sc.read(echoBuffer)) > 0) {
                        byte[] b = new byte[echoBuffer.position()];

                        echoBuffer.flip();
                        echoBuffer.get(b);
                        msg += new String(b, "UTF-8");

                    }

                    //removes the new line
                    if (msg.length() > 1) {
                        msg = msg.substring(0, msg.length() - 2);
                    }

                    if (code == -1 ||
                            (msg.toUpperCase().indexOf("BYE") > -1)) {
                        chm.remove(sc.hashCode());
                        sc.close();
                    } else {
                        echoBuffer.clear();
                    }

                    //TODO: process the message

                    it.remove();

                }
            }
        }

    }

    private static void sendMessagesToRandomClients() {
        new Thread("Send-to-Clients") {
            @Override
            public void run() {

                try {
                    while (true) {
                        final Random generator = new Random();

                        if (chm.keySet().size() > 0) {
                            final Integer randomKey = new ArrayList<Integer>(chm.keySet()).get(generator.nextInt(chm.keySet().size()));

                            final SocketChannel sc = chm.get(randomKey);

                            try {
                                msg++;
                                final ByteBuffer buf = ByteBuffer.wrap(("From server to Client msg no - " + msg + "\n").getBytes());
                                sc.write(buf);
                            } catch (IOException e) {
                                e.printStackTrace();
                                chm.remove(randomKey);
                            }


                        }
                        Thread.sleep(PAUSE_BETWEEN_MSGS);


                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }.start();

    }

}
