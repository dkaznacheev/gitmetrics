import java.util.Map;

public class CommitMetricInfo {

    public final Map<String, Double> metrics;
    public final String committerName;
    public final String committerEmail;
    public final int commitNumber;
    public final String commitMessage;

    public CommitMetricInfo(Map<String, Double> metrics,
                            String committerName,
                            String committerEmail,
                            String commitMessage,
                            int commitNumber) {
        this.metrics = metrics;
        this.committerName = committerName;
        this.committerEmail = committerEmail;
        this.commitMessage = commitMessage;
        this.commitNumber = commitNumber;
    }
}
