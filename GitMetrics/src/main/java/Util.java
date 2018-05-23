import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringJoiner;

public class Util {

    public static String executeCommand(String ...args) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(args);

        Process p = pb.start();

        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringJoiner sj = new StringJoiner("\n");
        String line;
        while ((line = reader.readLine())!= null) {
            sj.add(line);
        }

        return sj.toString();
    }

}
