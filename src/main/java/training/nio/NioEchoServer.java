package training.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.logging.Logger;


public class NioEchoServer {

    private static Logger logger = Logger.getLogger("NioServer");

    private static int round = 0;
    private static final int PORT = 8080;
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;

    public static void main(String[] args) throws IOException {
        // open a nio server socket and bind to port
        initServerSocketChannel();
        // open a selector
        initSelector();
        // register a server socket to the selector
        registerServerSocketIntoSelector();

        while (true) {
            // get selected channels to serve them
            Set<SelectionKey> selectedKeys = getSelectedKeys();
            // serve selected channels
            checkSelectedKeys(selectedKeys);
        }
    }

    private static Set<SelectionKey> getSelectedKeys() throws IOException {
        // Selects a set of keys whose corresponding channels are ready for I/O operations.
        int select = selector.select();
        logger.info("number of selected keys for round " + (round++) + " is " + select);
        // Returns this selector's selected-key set.
        return selector.selectedKeys();
    }

    private static void checkSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
        for (SelectionKey selectedKey : selectedKeys) {
            // check if the server socket is trying to accept connection
            if (selectedKey.isAcceptable()) {
                ServerSocketChannel server = (ServerSocketChannel) selectedKey.channel();
                SocketChannel client = server.accept();
                // register the client socket to the selector for read events
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
            }else if (selectedKey.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) selectedKey.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int bytesRead = clientChannel.read(byteBuffer);

                if (bytesRead == -1) {
                    clientChannel.close();
                }else {
                    byteBuffer.flip();
                    clientChannel.write(byteBuffer);
                    byteBuffer.clear();
                }
            }
            selectedKeys.remove(selectedKey);
        }
    }

    private static void registerServerSocketIntoSelector() throws ClosedChannelException {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private static void initSelector() throws IOException {
        selector = Selector.open();
    }

    private static void initServerSocketChannel() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(PORT));
    }
}
