import org.apache.commons.cli.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("u", true, "path to repository");
        options.addOption("r", true, "the remote repository URI");
        options.addOption("p", true, "relative project path");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);

            String path = System.getProperty("user.dir");
            if (commandLine.hasOption("r")) {
                System.out.println("Downloading...");
                Git.cloneRepository()
                    .setURI(commandLine.getOptionValue("r"))
                    .setDirectory(new File(System.getProperty("user.dir")))
                    .call();
            } else {
                if (commandLine.hasOption("u")) {
                    path = commandLine.getOptionValue("u");
                }
            }

            String projectPath = "";
            if (commandLine.hasOption("p")) {
                projectPath = commandLine.getOptionValue("p");
            }

            CommitMetricCounter counter = CommitMetricCounter.openCommitMetricCounter(
                    path, projectPath);

            if (counter == null) {
                System.err.println("Unable to count metrics!");
                return;
            }

            List<CommitMetricInfo> metricValues = counter.getMetricsHistory();

            Server localHTTPServer = new Server(path + File.separator + projectPath, metricValues);
            localHTTPServer.start();


        }  catch (IOException e) {
            System.err.println("Could not get the repository, download it first");
        } catch (ParseException e) {
            System.err.println("Invalid arguments!");
        } catch (GitAPIException e) {
            System.err.println("Could not clone the repository!");
        } catch (URISyntaxException e) {
            System.err.println("Server error!");
        }
    }
}
