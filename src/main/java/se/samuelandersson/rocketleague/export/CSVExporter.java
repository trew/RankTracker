package se.samuelandersson.rocketleague.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.regex.Pattern;

import se.samuelandersson.rocketleague.MatchResultsWrapper;
import se.samuelandersson.rocketleague.MatchResult;

/**
 * This exporter provides a way to export results to CSV format. The header of the CSV file looks like this:
 * <p>
 * 
 * <pre>
 * Date,Time,PlayList,DeltaPoints,RankPoints
 * </pre>
 * <p>
 * 
 * <pre>
 *        Date = Date of match.
 *        Time = Time of match.
 *    PlayList = A number indicating the Playlist searched. A value between 10-13. 
 *                 10 = Ranked 1v1
 *                 11 = Ranked 2v2
 *                 12 = Solo Ranked 3v3
 *                 13 = Ranked 3v3
 * DeltaPoints = Points gained or lossed from the match.
 *  RankPoints = Rank Points *before* the match concluded.
 * </pre>
 * 
 * @author Samuel Andersson
 */
public class CSVExporter implements Exporter
{
  public static final String PREFIX = "results-";
  public static final String SUFFIX = "csv";

  public static final String HEADER = "Date,Time,PlayList,DeltaPoints,RankPoints";
  public static final Pattern ROW_PATTERN = Pattern.compile("(?<date>[^,]+),(?<time>[^,]+),(?<playlist>[^,]+),(?<delta>[^,]+),(?<points>[^,]+)");
  public static final Pattern HEADER_PATTERN = Pattern.compile(HEADER);

  @Override
  public void export(final MatchResultsWrapper parser, final File file) throws IOException
  {
    export(parser.getResults(), file);
  }

  @Override
  public void export(final SortedSet<MatchResult> results, final File file) throws IOException
  {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
    {
      writer.append(toCSVString(results));
    }
  }

  @Override
  public String toString(final SortedSet<MatchResult> results)
  {
    if (results == null)
    {
      throw new NullPointerException("results");
    }

    return toCSVString(results);
  }

  @Override
  public String toString(final MatchResultsWrapper parser)
  {
    return toCSVString(parser.getResults());
  }

  /**
   * Creates a CSV string from a list of {@link MatchResult}s. See {@link CSVExporter} for information about the
   * CSV format.
   * 
   * @param results The results to export.
   * @return a CSV string containing the provided results.
   */
  private static String toCSVString(final SortedSet<MatchResult> results)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(HEADER);
    for (MatchResult result : results)
    {
      sb.append(System.lineSeparator());
      String date = result.getTime().toString("YYYY-MM-dd");
      String time = result.getTime().toString("HH:mm:ss");
      sb.append(String.format("%s,%s,%s,%s,%s",
                              date,
                              time,
                              MatchResult.getPlaylistName(result.getPlayList()),
                              result.getDeltaPoints(),
                              result.getRankPreGame()));
    }

    return sb.toString();
  }

  @Override
  public String getPrefix()
  {
    return PREFIX;
  }

  @Override
  public String getSuffix()
  {
    return SUFFIX;
  }
}
