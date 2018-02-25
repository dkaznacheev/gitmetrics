import org.eclipse.jgit.lib.PersonIdent;

import java.util.Date;
import java.util.TimeZone;

public class CommitInfo {

    /**
     * Number of added lines
     */
    private int linesAdded;

    /**
     * Number of deleted lines
     */
    private int linesDeleted;

    /**
     * The committer
     */
    private PersonIdent committer;

    /**
     * Date of commit
     */
    private Date commitDate;

    public void setCommitter(PersonIdent committer) {
        this.committer = committer;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public void setLinesAdded(int linesAdded) {
        this.linesAdded = linesAdded;
    }

    public int getLinesDeleted() {
        return linesDeleted;
    }

    public void setLinesDeleted(int linesDeleted) {
        this.linesDeleted = linesDeleted;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitterName() {
        return committer.getName();
    }
}
