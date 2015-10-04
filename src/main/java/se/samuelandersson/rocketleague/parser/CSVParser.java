package se.samuelandersson.rocketleague.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

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

  private File file;

  @Override
  public SortedSet<MatchResult> parse(final File file)
  {
    this.file = file;

    SortedSet<MatchResult> matchResult = new TreeSet<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file)))
    {
      String str = br.readLine();
      if (!parseHeader(str))
      {
        return matchResult;
      }
      while ((str = br.readLine()) != null)
      {
        parseResult(matchResult, str);
      }
    }
    catch (IOException e)
    {
      log.error(String.format("An error occured when parsing CSV file: [%s]", file.getName()), e);
    }

    return matchResult;
  }

  private boolean parseHeader(final String line)
  {
    if (!CSVExporter.HEADER_PATTERN.matcher(line).matches())
    {
      if (!CSVExporter.HEADER_WITH_MU_PATTERN.matcher(line).matches())
      {
        log.warn(String.format("Invalid header in %s. File not parsed. \"%s\"", file.getName(), line));
        return false;
      }
    }

    return true;
  }

  private void parseResult(SortedSet<MatchResult> matchResult, final String line)
  {
    Matcher regularMatch = CSVExporter.ROW_PATTERN.matcher(line);
    Matcher muMatch = CSVExporter.ROW_WITH_MU_PATTERN.matcher(line);

    if (regularMatch.matches())
    {
      parseResultWithoutMu(regularMatch, matchResult, line);
    }
    else if (muMatch.matches())
    {
      parseResultWithMu(muMatch, matchResult, line);
    }
    else
    {
      log.warn(String.format("Invalid format in %s. Entry not used. \"%s\"", file.getName(), line));
    }
  }

  private void parseResultWithoutMu(Matcher match, SortedSet<MatchResult> matchResult, final String line)
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
        if (MatchResult.isValidPlayList(playlist))
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
      log.warn(String.format("Could not parse row in %s. \"%s\"", file.getName(), line));
    }
  }

  private void parseResultWithMu(Matcher match, SortedSet<MatchResult> matchResult, final String line)
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
        if (MatchResult.isValidPlayList(playlist))
        {
          playlistStr = MatchResult.getPlaylistName(playlist);
        }
      }
      catch (NumberFormatException e)
      { //Ignore
      }
      float mu = match.group("mu") == null ? -1 : Float.parseFloat(match.group("mu"));
      float sigma = match.group("sigma") == null ? -1 : Float.parseFloat(match.group("sigma"));
      int delta = Integer.parseInt(match.group("delta"));
      int points = Integer.parseInt(match.group("points"));

      MatchResult result = new MatchResult(dateTime, playlistStr, delta, points, mu, sigma);
      matchResult.add(result);
    }
    catch (Exception e)
    {
      log.warn(String.format("Could not parse row in %s. \"%s\"", file.getName(), line));
    }
  }
}
