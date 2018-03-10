import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Server {
    private String repoName;

    private List<Integer> lines;

    public Server(String repoName, List<Integer> lines) {
        this.repoName = repoName;
        this.lines = lines;
    }

    static String readFile(String path, Charset encoding) throws IOException  {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(path).toURI()));
            return new String(encoded, encoding);
        } catch (URISyntaxException e) {
            throw new IOException();
        }
    }

    public void start() throws IOException, URISyntaxException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI("http://localhost:8000"));
        }
    }

    private class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, 0);

            String path = t.getRequestURI().getPath();

            InputStream fs;
            if (path.equals("/")) {
                path = "chart.html";
                String html = readFile(path, Charset.defaultCharset());

                html = html.replace("%REPONAME%", repoName);
                List<Integer> labels = IntStream.range(1, lines.size() + 1).boxed().collect(Collectors.toList());
                html = html.replace("%LABELS%", intListToString(labels));
                html = html.replace("%DATA%", intListToString(lines));

                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byteBuffer.write(html.getBytes());
                fs = new ByteArrayInputStream(byteBuffer.toByteArray());
            } else {
                File htmlPage = new File(getClass().getResource(path).getFile());
                fs = new FileInputStream(htmlPage);
            }

            OutputStream os = t.getResponseBody();
            final byte[] buffer = new byte[0x10000];
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer,0,count);
            }
            fs.close();

            os.close();
        }
    }

    private static String intListToString(List<Integer> labels) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.print(labels);
        return writer.toString();
    }
}
