package se.samuelandersson.rocketleague.export;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import se.samuelandersson.rocketleague.LogFileHelper;
import se.samuelandersson.rocketleague.MatchResult;
import se.samuelandersson.rocketleague.MatchResultsWrapper;
import se.samuelandersson.rocketleague.parser.CSVParser;
import se.samuelandersson.rocketleague.parser.Parser;

import com.google.common.base.Joiner;

public class CSVExporterTest
{
  private CSVExporter export = new CSVExporter();
  private Parser parser = new CSVParser();

  @Test
  public void testListToString()
  {
    SortedSet<MatchResult> results = new TreeSet<>();

    String date = "2015-01-02";
    String time = "10:11:12";
    results.add(new MatchResult(new DateTime(date + "T" + time),
                                MatchResult.getPlaylistName(MatchResult.SOLO_RANKED_3V3),
                                10,
                                800));
    String exported = export.toString(results);

    String expected = CSVExporter.HEADER + System.lineSeparator();
    expected += "2015-01-02,10:11:12,solo-3v3,10,800";

    assertEquals(exported, expected);
  }

  @Test
  public void testWrapperToString() throws Exception
  {
    MatchResultsWrapper wrapper = new MatchResultsWrapper(LogFileHelper.getValidCsvLogFile("simple.csv"), parser);

    String expected = CSVExporter.HEADER + System.lineSeparator();
    expected += "2015-01-01,00:00:00,solo-3v3,0,1000";

    assertEquals(export.toString(wrapper), expected);
  }

  @Test
  public void testHeaderPatterns()
  {
    String header = "Date,Time,PlayList,DeltaPoints,RankPoints";
    assertTrue(CSVExporter.HEADER_PATTERN.matcher(header).matches());
  }

  @Test
  public void testRowPatterns()
  {
    Pattern pattern = CSVExporter.ROW_PATTERN;
    String newRow = "2015-01-01,00:00:00,1v1,1,1000";
    assertTrue(pattern.matcher(newRow).matches());
  }

  @Test
  public void testWrapperToFile() throws Exception
  {
    File file = File.createTempFile("test-parser", "csv");
    MatchResultsWrapper wrapper = new MatchResultsWrapper(LogFileHelper.getValidCsvLogFile("simple.csv"), parser);

    String expected = CSVExporter.HEADER + System.lineSeparator();
    expected += "2015-01-01,00:00:00,solo-3v3,0,1000";

    export.export(wrapper, file);

    String actual = Joiner.on(System.lineSeparator()).join(Files.readAllLines(file.toPath(), Charset.defaultCharset()));

    assertEquals(actual, expected);
    file.delete();
  }

  @Test
  public void testListToFile() throws Exception
  {
    File file = File.createTempFile("test-list", "csv");
    SortedSet<MatchResult> results = new TreeSet<>();

    String date = "2015-01-02";
    String time = "10:11:12";
    results.add(new MatchResult(new DateTime(date + "T" + time),
                                MatchResult.getPlaylistName(MatchResult.SOLO_RANKED_3V3),
                                10,
                                800));
    String expected = CSVExporter.HEADER + System.lineSeparator();
    expected += "2015-01-02,10:11:12,solo-3v3,10,800";

    export.export(results, file);
    String actual = Joiner.on(System.lineSeparator()).join(Files.readAllLines(file.toPath(), Charset.defaultCharset()));
    assertEquals(actual, expected);
    file.delete();
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullResultsToFile() throws Exception
  {
    File file = File.createTempFile("test-list", "csv");
    try
    {
      export.export((SortedSet<MatchResult>) null, file);
    }
    finally
    {
      file.delete();
    }
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullFile() throws Exception
  {
    SortedSet<MatchResult> results = new TreeSet<>();
    export.export(results, (File) null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullWrapper() throws Exception
  {
    export.toString((MatchResultsWrapper) null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullList() throws Exception
  {
    export.toString((SortedSet<MatchResult>) null);
  }

}
