import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Random;

public class Fly {
  static String[] args;

  public static void main(String[] args) throws Exception {
    Fly.args = args;

    Random r = new Random();
    int port = args.length > 1 ? Integer.parseInt(args[1]) : (r.nextInt(1000) + 8000);

    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", new FlyHandler());
    server.setExecutor(null); // creates a default executor
    server.start();

    System.out.println("http://localhost:" + port);
  }

  static class FlyHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      String filePath = Fly.args[0];

      if (!"/".equals(t.getRequestURI().getPath())) {
        filePath = t.getRequestURI().getPath().substring(1);
      }

      File f = new File(filePath);

      if (!f.exists()) {
        t.sendResponseHeaders(404, -1);
        return;
      }

      try (FileChannel in = new FileInputStream(f).getChannel();
          WritableByteChannel out = Channels.newChannel(t.getResponseBody())) {

        t.sendResponseHeaders(200, f.length());

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (in.read(buffer) > 0) {
          buffer.flip();
          out.write(buffer);
          buffer.clear();
        }
      }
    }
  }
}
