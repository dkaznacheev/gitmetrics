import javafx.util.Pair;
import org.apache.commons.cli.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    /**
     * Prints the stats about every committer from the commit list.
     * @param list list of commits
     */
    private static void printStatsByCommitter(List<CommitInfo> list) {
        Map<String, List<CommitInfo>> commitInfoMap =
                list
                .stream()
                .collect(Collectors.groupingBy(CommitInfo::getCommitterName));
        for (Map.Entry<String, List<CommitInfo>> entry : commitInfoMap.entrySet()) {
            System.out.println("Name: " + entry.getKey());
            System.out.println("Number of commits: " + entry.getValue().size());
            System.out.println("Average time of commit: " + new SimpleDateFormat("HH:mm:ss")
                    .format(Commits.averageCommitTime(entry.getValue())));
            Optional<Pair<Double, Double>> average = Commits.averageDiff(entry.getValue());
            if (average.isPresent()) {
                DecimalFormat format = new DecimalFormat("#.#");
                System.out.println("Average lines added: " + format.format(average.get().getKey()));
                System.out.println("Average lines deleted: " + format.format(average.get().getValue()));
            }
            System.out.println();
        }
    }


    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("q", false, "hide the info about the committers");
        options.addOption("u", true, "the repository URI");
        options.addOption("g", false, "save the lines of code chart");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("u")) {
                System.out.println("Downloading...");
                Git.cloneRepository()
                    .setURI(commandLine.getOptionValue("u"))
                    .setDirectory(new File(System.getProperty("user.dir")))
                    .call();
            }

            CommitCounter commitCounter = new CommitCounter(
                    System.getProperty("user.dir") + File.separator + ".git");

            List<CommitInfo> commits = commitCounter.getCommitHistoryInfo();
            if (!commandLine.hasOption("q")) {
                printStatsByCommitter(commits);
            }

            if (commandLine.hasOption("g")) {
                List<Integer> lines = Commits.countLinesByDiff(commits);
                Plotter.makePlot(lines);
            }
        } catch (IOException e) {
            System.err.println("Could not get the repository, download it first");
        } catch (ParseException e) {
            System.err.println("Invalid arguments!");
        } catch (GitAPIException e) {
            System.err.println("Could not clone the repository!");
        }
    }
}
