package se.samuelandersson.rocketleague.parser;

import static org.testng.Assert.*;

import java.io.File;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MutableDateTime;
import org.testng.annotations.Test;

import se.samuelandersson.rocketleague.LogFileHelper;
import se.samuelandersson.rocketleague.MatchResult;

public class LogFileParserTest
{
  private LogFileParser getParser()
  {
    Pattern pattern = LogFileParser.LOGSTART_PATTERN;
    String logFileOpenRow = "Log: Log file open, 09/24/15 19:34:25";

    Matcher matcher = pattern.matcher(logFileOpenRow);
    assertTrue(matcher.matches());

    LogFileParser parser = new LogFileParser();
    parser.determineLogStart(matcher);

    return parser;
  }

  private LogFileParser getParserWithoutLogStart()
  {
    return new LogFileParser();
  }

  private DateTime getLogStart()
  {
    return new DateTime("2015-09-24T19:34:25");
  }

  @Test
  public void testDetermineLogStart() throws Exception
  {
    LogFileParser parser = getParserWithoutLogStart();

    Pattern pattern = LogFileParser.LOGSTART_PATTERN;
    String logFileOpenRow = "Log: Log file open, 09/24/15 19:34:25";

    Matcher matcher = pattern.matcher(logFileOpenRow);
    assertTrue(matcher.matches());

    assertNull(parser.getLogStart());

    parser.determineLogStart(matcher);

    DateTime expected = getLogStart();
    assertEquals(parser.getLogStart(), expected);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testDetermineLogStartNullMatcher() throws Exception
  {
    getParserWithoutLogStart().determineLogStart(null);
  }

  @Test(expectedExceptions = IllegalFieldValueException.class)
  public void testDetermineLogStartIllegalField() throws Exception
  {
    LogFileParser parser = getParserWithoutLogStart();

    Pattern pattern = LogFileParser.LOGSTART_PATTERN;
    String logFileOpenRow = "Log: Log file open, 09/24/15 219:34:25";

    Matcher matcher = pattern.matcher(logFileOpenRow);
    assertTrue(matcher.matches());

    assertNull(parser.getLogStart());

    parser.determineLogStart(matcher);
  }

  @Test(expectedExceptions = NumberFormatException.class)
  public void testDetermineLogStartBadInt() throws Exception
  {
    LogFileParser parser = getParserWithoutLogStart();

    String bigInt = String.valueOf(Integer.MAX_VALUE) + String.valueOf(Integer.MAX_VALUE);

    Pattern pattern = LogFileParser.LOGSTART_PATTERN;
    String logFileOpenRow = String.format("Log: Log file open, 09/%s/15 19:34:25", bigInt);

    Matcher matcher = pattern.matcher(logFileOpenRow);
    assertTrue(matcher.matches());

    assertNull(parser.getLogStart());

    parser.determineLogStart(matcher);
  }

  @Test(groups = "dateTimeFromLogDeltaTime")
  public void testDateTimeFromLogDeltaTime()
  {
    LogFileParser parser = getParser();

    DateTime logStart = getLogStart();

    String deltaTime = "1004.89";

    assertEquals(parser.getDateTimeFromLogDeltaTime(deltaTime), logStart.plusSeconds(1004));
    assertNotEquals(parser.getDateTimeFromLogDeltaTime(deltaTime), logStart.plusSeconds(1004).plusMillis(89));
  }

  @Test(groups = "dateTimeFromLogDeltaTime")
  public void testDateTimeFromLogDeltaTimeNoMillis()
  {
    LogFileParser parser = getParser();
    DateTime logStart = getLogStart();

    String deltaTime = "60";
    parser.getDateTimeFromLogDeltaTime(deltaTime);

    assertEquals(parser.getDateTimeFromLogDeltaTime(deltaTime), logStart.plusSeconds(60));
  }

  @Test(groups = "dateTimeFromLogDeltaTime")
  public void testDateTimeFromLogDeltaTimeBadArgs()
  {
    LogFileParser parser = getParser();
    String deltaTime = "wtf";
    assertEquals(parser.getDateTimeFromLogDeltaTime(deltaTime), new DateTime(0));
    assertNotEquals(parser.getDateTimeFromLogDeltaTime(deltaTime), getLogStart());
  }

  @Test(groups = "dateTimeFromLogDeltaTime")
  public void testDateTimeFromLogDeltaTimeNullArgs()
  {
    LogFileParser parser = getParser();
    assertEquals(parser.getDateTimeFromLogDeltaTime(null), new DateTime(0));
    assertNotEquals(parser.getDateTimeFromLogDeltaTime(null), getLogStart());
  }

  @Test
  public void testCreateMatchResultUnrankedPlayList() throws Exception
  {
    LogFileParser parser = getParser();
    String matchResultRow = "[1004.89] RankPoints: ClientSetSkill Playlist=0 Mu=28.6374 Sigma=2.4856 DeltaRankPoints=-10 RankPoints=735";
    Matcher matcher = getMatcher(matchResultRow);
    assertEquals(parser.createMatchResult(matcher).getPlayList(), MatchResult.UNRANKED);
  }

  @Test(groups = "createMatchResult", expectedExceptions = NullPointerException.class)
  public void testCreateMatchResultNullMatcher() throws Exception
  {
    LogFileParser parser = getParser();
    parser.createMatchResult(null);
  }

  @Test(groups = "createMatchResult")
  public void testCreateMatchResult() throws Exception
  {
    {
      LogFileParser parser = getParser();
      String matchResultRow = "[1004.89] RankPoints: ClientSetSkill Playlist=10 Mu=28.6374 Sigma=2.4856 DeltaRankPoints=-10 RankPoints=735";
      Matcher matcher = getMatcher(matchResultRow);

      MatchResult result = parser.createMatchResult(matcher);
      assertEquals(result.getPlayList(), 10);
      assertEquals(result.getDeltaPoints(), -10);
      assertEquals(result.getRankPreGame(), 735);
      MutableDateTime expected = getLogStart().toMutableDateTime();
      expected.addSeconds(1004);
      expected.addMillis(89);
      // millis is filtered out, which is why this is not equal
      assertNotEquals(result.getTime(), expected.toDateTime());
      expected.setMillisOfSecond(0);
      assertEquals(result.getTime(), expected.toDateTime());
    }
    {
      LogFileParser parser = getParser();
      String matchResultRow = "[1000.49] RankPoints: ClientSetSkill Playlist=11 Mu=28.6374 Sigma=2.4856 DeltaRankPoints=10 RankPoints=745";
      Matcher matcher = getMatcher(matchResultRow);

      MatchResult result = parser.createMatchResult(matcher);
      assertEquals(result.getPlayList(), 11);
      assertEquals(result.getDeltaPoints(), 10);
      assertEquals(result.getRankPreGame(), 745);
      MutableDateTime expected = getLogStart().toMutableDateTime();
      expected.addSeconds(1000);
      expected.addMillis(49);
      // millis is filtered out, which is why this is not equal
      assertNotEquals(result.getTime(), expected.toDateTime());
      expected.setMillisOfSecond(0);
      assertEquals(result.getTime(), expected.toDateTime());
    }
  }

  @Test(groups = "createMatchResult")
  public void testCreateMatchResultInvalid() throws Exception
  {
    LogFileParser parser = getParser();
    String bigInt = String.valueOf(Integer.MAX_VALUE) + String.valueOf(Integer.MAX_VALUE);
    String badPlaylist = String.format("[1004.89] RankPoints: ClientSetSkill Playlist=%s Mu=28.6374 Sigma=2.4856 DeltaRankPoints=10 RankPoints=100",
                                       bigInt);
    String badDelta = String.format("[1004.89] RankPoints: ClientSetSkill Playlist=10 Mu=28.6374 Sigma=2.4856 DeltaRankPoints=%s RankPoints=100",
                                    bigInt);
    String badRank = String.format("[1004.89] RankPoints: ClientSetSkill Playlist=10 Mu=28.6374 Sigma=2.4856 DeltaRankPoints=10 RankPoints=%s",
                                   bigInt);
    assertNull(parser.createMatchResult(getMatcher(badPlaylist)));
    assertNull(parser.createMatchResult(getMatcher(badDelta)));
    assertNull(parser.createMatchResult(getMatcher(badRank)));
  }

  @Test(groups = "parse")
  public void testParse() throws Exception
  {
    LogFileParser parser = getParserWithoutLogStart();
    File ranked = LogFileHelper.getValidRLLogFile("ranked.log");
    SortedSet<MatchResult> result = parser.parse(ranked);
    assertEquals(result.size(), 8);

    parser = getParserWithoutLogStart();
    File unranked = LogFileHelper.getValidRLLogFile("unranked.log");
    result = parser.parse(unranked);
    assertEquals(result.size(), 4);

    parser = getParserWithoutLogStart();
    File mixed = LogFileHelper.getValidRLLogFile("mixed.log");
    result = parser.parse(mixed);
    assertEquals(result.size(), 7);

    parser = getParserWithoutLogStart();
    File empty = LogFileHelper.getValidRLLogFile("empty.log");
    result = parser.parse(empty);
    assertEquals(result.size(), 0);
  }

  @Test(groups = "parse")
  public void testParseBadLogStart() throws Exception
  {
    LogFileParser parser = getParserWithoutLogStart();
    File badLogStart = LogFileHelper.getInvalidRLLogFile("badlogstart.log");
    SortedSet<MatchResult> result = parser.parse(badLogStart);
    assertEquals(result.size(), 0);

    parser = getParserWithoutLogStart();
    File missinglogstart = LogFileHelper.getInvalidRLLogFile("missinglogstart.log");
    result = parser.parse(missinglogstart);
    assertEquals(result.size(), 0);
  }

  @Test(groups = "parse")
  public void testParseBadResults() throws Exception
  {
    LogFileParser parser = getParserWithoutLogStart();
    File badResults = LogFileHelper.getInvalidRLLogFile("badranked.log");
    SortedSet<MatchResult> result = parser.parse(badResults);
    assertEquals(result.size(), 0);
  }

  private Matcher getMatcher(String row)
  {
    Pattern pattern = LogFileParser.RANKPOINTS_PATTERN;
    Matcher matcher = pattern.matcher(row);
    assertTrue(matcher.matches());

    return matcher;
  }
}