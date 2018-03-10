import javafx.util.Pair;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for working with JGit that fetches the list of commits from repository.
 */
public class CommitCounter {

    private String repoName;

    /**
     * The repository we're working in.
     */
    private Repository repository;

    /**
     * Walk object of the repository.
     */
    private RevWalk walk;

    /**
     * Creates a CommitCounter of a repository by pathname.
     * @param pathname path to repository
     */
    public CommitCounter(String pathname) throws IOException {
        repoName = pathname;
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder.setGitDir(new File(pathname))
                .readEnvironment()
                .findGitDir()
                .build();
        walk = new RevWalk(repository);
        walk.markStart(walk.parseCommit(repository.resolve("HEAD")));
    }

    /**
     * Counts the added and deleted lines in a commit.
     * @param commit commit to count lines in
     * @return pair of added lines(as the key) and deleted lines(as the value)
     */
    private Pair<Integer, Integer> countLinesDiff(RevCommit commit) throws IOException {
        int linesAdded = 0;
        int linesDeleted = 0;

        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs;
        AbstractTreeIterator parentTreeIterator;
        if (commit.getParents().length == 0) {
            parentTreeIterator = new EmptyTreeIterator();
        } else {
            RevCommit parent = walk.parseCommit(commit.getParent(0).getId());
            parentTreeIterator = getIteratorByCommit(parent);
        }
        diffs = df.scan(parentTreeIterator, getIteratorByCommit(commit));
        for (DiffEntry diff : diffs) {
            for (Edit edit : df.toFileHeader(diff).toEditList()) {
                linesDeleted += edit.getEndA() - edit.getBeginA();
                linesAdded += edit.getEndB() - edit.getBeginB();
            }
        }
        return new Pair<>(linesAdded, linesDeleted);
    }

    /**
     * Gets an AbstractTreeIterator from given commit.
     * @param commit commit
     * @return iterator
     */
    private  AbstractTreeIterator getIteratorByCommit(RevCommit commit) throws IOException {
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            treeParser.reset(reader, tree.getId());
        }
        return treeParser;
    }

    /**
     * Walks the commit history and extracts the commit info.
     * @return list of CommitInfo
     */
    public List<CommitInfo> getCommitHistoryInfo() throws IOException{
        List<CommitInfo> list = new ArrayList<>();

        for (RevCommit commit : walk)  {
            CommitInfo info = new CommitInfo();

            info.setCommitter(commit.getCommitterIdent());

            Pair<Integer, Integer> linesDiff = countLinesDiff(commit);
            info.setLinesAdded(linesDiff.getKey());
            info.setLinesDeleted(linesDiff.getValue());

            PersonIdent ident = commit.getCommitterIdent();
            info.setCommitDate(ident.getWhen());

            list.add(info);
        }

        return list;
    }

    public String getRepositoryName() {
        return repoName;
    }
}
