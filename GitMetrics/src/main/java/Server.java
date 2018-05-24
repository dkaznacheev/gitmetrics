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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Server {
    private String repoName;

    private List<CommitMetricInfo> commitInfos;
    private List<CommitMetricInfo> commitInfoDiffs;

    private Map<String, List<CommitMetricInfo>> commitDiffsByUser;
    private Map<String, List<String>> aliases;

    public Server(String repoName, List<CommitMetricInfo> commitInfos) {
        this.repoName = repoName;
        this.commitInfos = commitInfos;

        commitInfoDiffs = createDiffs();
        commitDiffsByUser = commitInfoDiffs.stream().collect(Collectors.groupingBy((info) -> info.committerName));
        aliases = commitDiffsByUser
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e->e.getValue()
                                .stream()
                                .map((info) -> info.committerEmail).distinct().collect(Collectors.toList())
                ));
    }

    private List<CommitMetricInfo> createDiffs() {
        Map<String, Double> previous = commitInfos.get(0).metrics;
        for (Map.Entry<String, Double> metric : previous.entrySet()) {
            previous.put(metric.getKey(), 0.0);
        }
        List<CommitMetricInfo> result = new LinkedList<>();
        for (CommitMetricInfo info : commitInfos) {
            Map<String, Double> diffs = new HashMap<>();
            for (Map.Entry<String, Double> metric : info.metrics.entrySet()) {
                diffs.put(metric.getKey(), metric.getValue() - previous.get(metric.getKey()));
            }
            previous = info.metrics;
            result.add(new CommitMetricInfo(
                    diffs,
                    info.committerName,
                    info.committerEmail,
                    info.commitMessage,
                    info.commitNumber
            ));
        }
        return result;
    }

    private String assembleData() {
        StringJoiner joiner = new StringJoiner(",\n");
        for (String metric : getMetricsNames()) {
            joiner.add("\"" + getAbbreviation(metric) + "\" : {" + makeUserDictForMetric(metric) + "}");
        }
        return joiner.toString();
    }

    private String getAbbreviation(String metric) {
        if (metric.equals("Lines of code"))
            return "LOC";
        return "LOCt";
    }

    private String makeUserDictForMetric(String metric) {
        StringJoiner joiner = new StringJoiner(",\n");
        joiner.add("\"total\" :[" + makeDatasetForMetric(commitInfos, metric) + "]");
        commitDiffsByUser.entrySet().forEach(e ->
                joiner.add("\"" + e.getKey() + "\" "+ ": [" + makeDatasetForMetric(e.getValue(), metric) + "]"));
        return joiner.toString();
    }

    private String makeDatasetForMetric(List<CommitMetricInfo> values, String metric) {
        StringJoiner joiner = new StringJoiner(",\n");
        values.forEach(e->joiner.add(
                "{x : "
                + e.commitNumber
                + ", y: "
                + e.metrics.get(metric)
                + ", message: "
                + "\"" + e.commitMessage + "\"}"
        ));
        return joiner.toString();
    }

    private List<String> getMetricsNames() {
        CommitMetricInfo info = commitInfos.get(0);
        return info.metrics.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
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

                html = html
                    .replace("%REPONAME%", repoName)
                    .replace("%TABS%", makeTabs())
                    .replace("%BUTTONS%", makeButtons());
                System.out.println(html);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byteBuffer.write(html.getBytes());
                fs = new ByteArrayInputStream(byteBuffer.toByteArray());
            } else if (path.equals("/script.js")) {
                String js = readFile("script.js", Charset.defaultCharset());
                js = js
                        .replace("%DATASET%", assembleData())
                        .replace("%CHARTS%", makeCharts())
                        .replace("%NAMETABLE%", makeNameTable())
                        .replace("%METRICNAME%", "\"" + getMetricsNames().get(0) + "\"");
                System.out.println(js);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byteBuffer.write(js.getBytes());
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

    private String makeNameTable() {
        StringJoiner joiner = new StringJoiner(",\n");
        int id = 1;
        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            joiner.add("\"button" + Integer.toString(id) + "\": \"" + entry.getKey() + "\"");
            id++;
        }
        return joiner.toString();
    }

    private String makeCharts() {
        final String pattern = "{\n" +
                "type: \"line\",\n" +
                "name: \"%NAME%\",\n" +
                "visible: %VIS%,\n" +
                "dataPoints: dataset[\"%METRIC%\"][\"%NAME%\"]\n" +
                "}";
        StringJoiner joiner = new StringJoiner(",\n");
        String firstMetricName = getAbbreviation(getMetricsNames().get(0));
        joiner.add(pattern
                .replaceAll("%NAME%", "total")
                .replace("%VIS%", "true")
                .replace("%METRIC%", firstMetricName)
        );
        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            joiner.add(pattern
                    .replaceAll("%NAME%", entry.getKey())
                    .replace("%VIS%", "false")
                    .replace("%METRIC%", firstMetricName)
            );
        }
        return joiner.toString();
    }

    private String makeButtons() {
        final String pattern = "<button id=\"%ID%\" value=\"Off\" class=\"tablinks\" onclick=\"onCommitterClick('%ID%')\" onmouseover=\"mouseOver('%ID%')\" onmouseout=\"mouseOut('%ID%')\"> %NAME% </button>";
        StringJoiner joiner = new StringJoiner("\n");
        int id = 1;
        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            joiner.add(pattern
                    .replaceAll("%ID%", "button" + Integer.toString(id))
                    .replaceAll("%NAME%", entry.getKey())
            );
            id++;
        }
        return joiner.toString();
    }

    private String makeTabs() {
        StringJoiner joiner = new StringJoiner("\n");
        for (String metric : getMetricsNames()) {
            joiner.add("<button class=\"tablinks\" onclick=\"selectChart(event, '"
                    + getAbbreviation(metric) + "', '" + metric + "')\">" + metric + "</button>");
        }
        return joiner.toString();
    }

}
