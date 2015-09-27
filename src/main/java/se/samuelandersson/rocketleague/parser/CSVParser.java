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
import se.samuelandersson.rocketleague.export.CSVExporter;

/**
 * A CSV parser accepts CSV files that were exported using {@link CSVExporter}.
 * 
 * @author Samuel Andersson
 */
public class CSVParser implements Parser
{
  private static final Logger log = LoggerFactory.getLogger(CSVParser.class);

  @Override
  public SortedSet<MatchResult> parse(final File file)
  {
    SortedSet<MatchResult> matchResult = new TreeSet<>();
    Pattern csvPattern = CSVExporter.ROW_PATTERN;
    Pattern headerPattern = CSVExporter.HEADER_PATTERN;
    try (BufferedReader br = new BufferedReader(new FileReader(file)))
    {
      String str = br.readLine();
      if (!headerPattern.matcher(str).matches())
      {
        log.warn(String.format("Invalid header in %s. File not parsed. \"%s\"", file.getName(), str));
        return matchResult;
      }

      while ((str = br.readLine()) != null)
      {
        Matcher match = csvPattern.matcher(str);
        if (match.matches())
        {
          String date = match.group("date");
          String time = match.group("time");
          try
          {
            DateTime dateTime = new DateTime(date + "T" + time);
            String playlistStr = match.group("playlist");
            try
            {
              int playlist = Integer.parseInt(playlistStr);
              if (MatchResult.isRankedPlayList(playlist))
              {
                playlistStr = MatchResult.getPlaylistName(playlist);
              }
            }
            catch (NumberFormatException e)
            { //Ignore
            }
            int delta = Integer.parseInt(match.group("delta"));
            int points = Integer.parseInt(match.group("points"));

            MatchResult result = new MatchResult(dateTime, playlistStr, delta, points);
            matchResult.add(result);
          }
          catch (Exception e)
          {
            log.warn(String.format("Could not parse row in %s. \"%s\"", file.getName(), str));
          }
        }
        else
        {
          log.warn(String.format("Invalid format in %s. Entry not used. \"%s\"", file.getName(), str));
        }
      }
    }
    catch (IOException e)
    {
      log.error(String.format("An error occured when parsing CSV file: [%s]", file.getName()), e);
    }

    return matchResult;
  }
}
