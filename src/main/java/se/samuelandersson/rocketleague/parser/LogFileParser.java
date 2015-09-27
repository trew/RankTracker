package se.samuelandersson.rocketleague.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.samuelandersson.rocketleague.MatchResult;

/**
 * A LogFileParser accepts Rocket League log files for extracting {@link MatchResult}s.
 * 
 * @author Samuel Andersson
 */
public class LogFileParser implements Parser
{
  private static final Logger log = LoggerFactory.getLogger(LogFileParser.class);

  public static final Pattern RANKPOINTS_PATTERN = Pattern.compile("\\[(?<time>\\d+\\.\\d+)\\] RankPoints\\: ClientSetSkill Playlist=(?<playlist>\\d+) Mu=(?<mu>\\d+\\.\\d+) Sigma=(?<sigma>\\d+\\.\\d+) DeltaRankPoints=(?<minus>-?)(?<delta>\\d+) RankPoints=(?<points>\\d+)");
  public static final Pattern LOGSTART_PATTERN = Pattern.compile("Log\\: Log file open, (?<month>\\d+)/(?<day>\\d+)/(?<year>\\d+) (?<hour>\\d+)\\:(?<minute>\\d+)\\:(?<second>\\d+)");

  private DateTime logStart;

  @Override
  public SortedSet<MatchResult> parse(File file)
  {
    SortedSet<MatchResult> matchResults = new TreeSet<>();

    try (BufferedReader br = new BufferedReader(new FileReader(file)))
    {
      String str;
      while ((str = br.readLine()) != null)
      {
        if (logStart == null)
        {
          Matcher logStartMatch = LOGSTART_PATTERN.matcher(str);
          if (logStartMatch.matches())
          {
            try
            {
              determineLogStart(logStartMatch);
            }
            catch (Exception e)
            {
              log.error("Error determining log start: {}", e.getMessage());
              return matchResults;
            }
          }
        }

        Matcher matchRank = RANKPOINTS_PATTERN.matcher(str);
        if (matchRank.matches())
        {
          if (logStart == null)
          {
            log.error("Log start was not determined before encountering match result.");
            return matchResults;
          }

          MatchResult result = createMatchResult(matchRank);
          if (result != null)
          {
            matchResults.add(result);
          }
        }
      }
    }
    catch (IOException e)
    {
      log.error(String.format("An error occured when parsing log file: [%s]", file.getName()), e);
    }

    return matchResults;
  }

  /**
   * Creates a {@link MatchResult} based on the given {@link Matcher}. The matcher were created using the
   * {@link #RANKPOINTS_PATTERN} pattern, which contains the necessary groups.
   * 
   * @param match the Matcher created when matching the row.
   * @return a MatchResult based on the given matcher, or null, if some values in the matcher is invalid or if the
   *         playlist is not of the ranked variety.
   */
  protected MatchResult createMatchResult(Matcher match)
  {
    DateTime time = getDateTimeFromLogDeltaTime(match.group("time"));
    boolean minus = !match.group("minus").isEmpty();

    int deltaPoints = 0, rankPoints = 0, playList = 0;
    try
    {
      deltaPoints = Integer.parseInt(match.group("delta")) * (minus ? -1 : 1);
      rankPoints = Integer.parseInt(match.group("points"));
      playList = Integer.parseInt(match.group("playlist"));
    }
    catch (NumberFormatException e)
    {
      log.warn("Error when parsing number for match result: {}", e.getMessage());
      return null;
    }

    if (!MatchResult.isRankedPlayList(playList))
    {
      return null;
    }

    return new MatchResult(time, playList, deltaPoints, rankPoints);
  }

  /**
   * Processes a {@link Matcher} that matched against the first line of the log file. That line contains when the
   * logfile were created. All other lines in the logfile have a delta time based on when the Rocket League logger were
   * initialized. The value is saved into an instance variable and used for determining the correct time for the
   * {@link MatchResult}s.
   * 
   * @param logStartMatch a Matcher that matched against the first line in the log file.
   */
  protected void determineLogStart(Matcher logStartMatch)
  {
    if (logStartMatch == null)
    {
      throw new NullPointerException("logStartMatch");
    }

    String year = "20" + logStartMatch.group("year");
    String month = logStartMatch.group("month");
    String day = logStartMatch.group("day");
    String hour = logStartMatch.group("hour");
    String minute = logStartMatch.group("minute");
    String second = logStartMatch.group("second");

    logStart = new DateTime(Integer.parseInt(year),
                            Integer.parseInt(month),
                            Integer.parseInt(day),
                            Integer.parseInt(hour),
                            Integer.parseInt(minute),
                            Integer.parseInt(second));
  }

  /**
   * Returns a {@link DateTime} based on the deltatime provided. This time is in "[seconds].[milliseconds]" format, but
   * the milliseconds are discarded.
   * 
   * @param deltaTimeInSeconds the delta time in seconds.
   * @return Time of logger initialization + the deltatime provided.
   * @see LogFileParser#determineLogStart(Matcher)
   */
  protected DateTime getDateTimeFromLogDeltaTime(final String deltaTimeInSeconds)
  {
    if (deltaTimeInSeconds == null)
    {
      return new DateTime(0);
    }

    String[] split = deltaTimeInSeconds.split("\\.");
    try
    {
      int seconds = Integer.parseInt(split[0]);
      return logStart.plusSeconds(seconds);
    }
    catch (NumberFormatException e)
    {
      return new DateTime(0);
    }
  }

  /**
   * Returns the time which the logger where initialized at.
   * 
   * @return the time which the logger where initialized at. {@code null} if {@link #determineLogStart(Matcher)} has not
   *         yet been called.
   */
  protected DateTime getLogStart()
  {
    return logStart;
  }
}
