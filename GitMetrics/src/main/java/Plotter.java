import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class for plotting the number of lines of code with each commit.
 */
public class Plotter {

    /**
     * The title of graph.
     */
    private static final String title = "Number of lines by commit";

    /**
     * X label.
     */
    private static final String xAxisLabel = "Commit number";

    /**
     * Y label.
     */
    private static final String yAxisLabel = "Number of lines";

    /**
     * Saves a chart of given dataset as a JPEG image.
     * @param list the dataset
     * @throws IOException if it could not save the image
     */
    public static void makePlot(List<Integer> list) {
        XYSeries commits = new XYSeries("Commits");
        int i = 1;
        for (int count : list) {
            commits.add(i++, count);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(commits);

        JFreeChart xyLineChart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        int width = 640;   /* Width of the image */
        int height = 480;  /* Height of the image */
        File XYChart = new File( "commit_chart.jpeg" );

        try {
            ChartUtilities.saveChartAsJPEG( XYChart, xyLineChart, width, height);
        } catch (IOException e) {
            System.err.println("Could not save the chart!");
        }
    }
}
