import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class CommitMetricCounter {
    private final Repository repository;
    private final RevWalk walk;
    private final String pathname;
    private final Git git;

    private static final String IDEA_PATH =
            "/home/dk/.local/share/JetBrains/Toolbox/apps/IDEA-C/ch-0/181.4203.550/bin/idea.sh";

    private CommitMetricCounter(Repository repository, RevWalk walk, String pathname, Git git){
        this.repository = repository;
        this.walk = walk;
        this.pathname = pathname;
        this.git = git;
    }

    public static CommitMetricCounter openCommitMetricCounter(String pathname, String relativeProjectPath) {
        pathname = normalize(pathname);
        relativeProjectPath = normalize(relativeProjectPath);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            String gitPath = pathname + File.separator + ".git";
            System.out.println(gitPath + "\n" + pathname + File.separator + relativeProjectPath);
            Repository repository = builder.setGitDir(new File(gitPath))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            if (repository == null)
                return null;
            Git git = Git.open(new File(gitPath));
            RevWalk walk = new RevWalk(repository);
            walk.markStart(walk.parseCommit(repository.resolve("HEAD")));

            return new CommitMetricCounter(repository, walk, pathname + File.separator + relativeProjectPath, git);
        } catch (IOException e) {
            return null;
        }
    }

    private static String normalize(String pathname) {
        if (pathname.endsWith("/")) {
            return pathname.substring(0, pathname.length() - 1);
        }
        return pathname;
    }

    public List<CommitMetricInfo> getMetricsHistory() throws GitAPIException {
        List<CommitMetricInfo> metricValues = new LinkedList<>();
        git.checkout().setName("master").call();
        int index = 0;
        for (RevCommit commit : walk) {
            git.checkout().setName(commit.getName()).call();
            System.out.println("Counting metrics on commit " + commit.getName());

            Map<String, Double> metricResults = countMetrics("Lines of code metrics");

            PersonIdent committer = commit.getCommitterIdent();

            metricValues.add(new CommitMetricInfo(
                    metricResults,
                    committer.getName(),
                    committer.getEmailAddress(),
                    commit.getShortMessage(),
                    index++
            ));
        }
        git.checkout().setName("master").call();

        return metricValues;

    }

    private Random random = new Random();

    private Map<String,Double> stubMetricResults() {
        Map<String, Double> metricResults = new HashMap<>();
        metricResults.put("Lines of code", random.nextDouble() * 100);
        metricResults.put("Lines of test code", random.nextDouble() * 100);
        return metricResults;
    }

    public Map<String, Double> countMetrics(String metricProfileName) {
        try {
            String xml = Util.executeCommand(IDEA_PATH, "metrics", normalize(pathname), metricProfileName);
            return parseXML(cleanOutput(xml));
        } catch (InterruptedException | IOException e) {
            return null;
        }
    }

    private String cleanOutput(String xml) {
        String s = xml;
        for (int i = 0; i < 4; i++) {
            s = s.substring(s.indexOf('\n') + 1);
        }
        s = s.substring(0, s.lastIndexOf('\n'));
        return s;
    }

    private Map<String, Double> parseXML(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            Map<String, Double> metricResults = new HashMap<>();
            Double linesOfCode =  getValueFromXML(doc, "LOC");
            Double linesOfTestCode = getValueFromXML(doc, "LOCt");
            metricResults.put("Lines of code", linesOfCode);
            metricResults.put("Lines of test code", linesOfTestCode);

            return metricResults;
        } catch (Exception e) {
            return null;
        }
    }

    private Double getValueFromXML(Document doc, String abbreviation) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        String pattern = "/METRICS/METRIC[@category=\"Project\" and @abbreviation=\"%ABBR%\"]/VALUE";
        Element valueElement = (Element) xpath.evaluate(
                pattern.replace("%ABBR%", abbreviation)
                , doc, XPathConstants.NODE);
        return Double.parseDouble(valueElement.getAttribute("value"));
    }
}
