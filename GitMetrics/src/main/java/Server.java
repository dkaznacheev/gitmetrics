import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI("http://localhost:8000"));
        }
    }

    static String readFile(String path, Charset encoding) throws IOException  {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(path).toURI()));
            return new String(encoded, encoding);
        } catch (URISyntaxException e) {
            throw new IOException();
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, 0);

            String path = t.getRequestURI().getPath();

            if (path.equals("/")) {
                path = "chart.html";
                String html = readFile(path, Charset.defaultCharset());
                System.out.println(html);
            }

            OutputStream os = t.getResponseBody();
            File htmlPage = new File(getClass().getResource(path).getFile());
            FileInputStream fs = new FileInputStream(htmlPage);
            final byte[] buffer = new byte[0x10000];
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer,0,count);
            }
            fs.close();

            os.close();
        }
    }
}
