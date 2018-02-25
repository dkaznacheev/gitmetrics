import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Utility class for manipulating commit information.
 */
public class Commits {
    /**
     * Counts the average commit time.
     * @param list list of commits
     * @return average commit time
     */
    public static Date averageCommitTime(List<CommitInfo> list) {
        return list.stream()
                .map(CommitInfo::getCommitDate)
                .map(Date::getTime)
                .collect(Collectors.collectingAndThen(
                        Collectors.averagingLong(Long::longValue),
                        it -> new Date(
                                (it.longValue() - TimeUnit.DAYS.toMillis(1))
                                        / TimeUnit.SECONDS.toMillis(1)
                                        * TimeUnit.SECONDS.toMillis(1)
                        )
                ));
    }

    /**
     * Counts the average added and deleted lines in commits.
     * Will return Optional.empty() if the list is empty.
     * @param list list of commits
     * @return pair of average added lines(as the key) and deleted lines(as the value)
     */
    public static Optional<Pair<Double, Double>> averageDiff(List<CommitInfo> list) {
        OptionalDouble averageAdded = list.stream().mapToInt(CommitInfo::getLinesAdded).average();
        OptionalDouble averageDeleted = list.stream().mapToInt(CommitInfo::getLinesDeleted).average();
        if (averageAdded.isPresent() && averageDeleted.isPresent()) {
            return Optional.of(new Pair<>(averageAdded.getAsDouble(), averageDeleted.getAsDouble()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Counts the number of lines in repository with each commit using the differences in each commit.
     * @param diff list of commits
     * @return list of number of lines in repository with each commit
     */
    public static List<Integer> countLinesByDiff(List<CommitInfo> diff) {
        List<Integer> lines = new ArrayList<>();
        lines.add(0);
        Collections.reverse(diff);
        Iterator<CommitInfo> iterator = diff.iterator();
        for (int i = 0; i < diff.size(); i++) {
            CommitInfo info = iterator.next();
            lines.add(lines.get(i) + info.getLinesAdded() - info.getLinesDeleted());
        }
        return lines;
    }
}
