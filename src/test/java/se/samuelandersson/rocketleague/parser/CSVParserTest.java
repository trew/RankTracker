package se.samuelandersson.rocketleague.parser;

import static org.testng.Assert.*;

import java.util.SortedSet;

import org.testng.annotations.Test;

import se.samuelandersson.rocketleague.LogFileHelper;
import se.samuelandersson.rocketleague.MatchResult;

public class CSVParserTest
{
  private final CSVParser parser = new CSVParser();

  @Test
  public void testParse() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getValidCsvLogFile("log.csv"));
    assertEquals(results.size(), 2);
    assertEquals(results.first().getSkillMean(), -1f);
    assertEquals(results.first().getSkillSigma(), -1f);
  }

  @Test
  public void testParseMu() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getValidCsvLogFile("logWithMu.csv"));
    assertEquals(results.size(), 2);
    assertEquals(results.first().getSkillMean(), 50f);
    assertEquals(results.first().getSkillSigma(), 2.5f);
    assertEquals(results.last().getSkillMean(), 49.9f);
    assertEquals(results.last().getSkillSigma(), 2.49f);
  }

  @Test
  public void testParsePlayListNumber() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getValidCsvLogFile("playlistAsNumber.csv"));
    assertEquals(results.size(), 1);
    assertEquals(results.first().getPlayList(), MatchResult.SOLO_RANKED_3V3);
  }

  @Test
  public void testParseBadFormat() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getInvalidCsvLogFile("badformat.csv"));
    assertEquals(results.size(), 0);
  }

  @Test
  public void testParseBadHeader() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getInvalidCsvLogFile("badheader.csv"));
    assertEquals(results.size(), 0);
  }

  @Test
  public void testParseMissingHeader() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getInvalidCsvLogFile("noheader.csv"));
    assertEquals(results.size(), 0);
  }

  @Test
  public void testParseBadValues() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getInvalidCsvLogFile("badvalues.csv"));
    assertEquals(results.size(), 0);
  }

  @Test
  public void testParseBadPlayListNumber() throws Exception
  {
    SortedSet<MatchResult> results = parser.parse(LogFileHelper.getInvalidCsvLogFile("playlistAsBadNumber.csv"));
    assertEquals(results.size(), 0);
  }
}
