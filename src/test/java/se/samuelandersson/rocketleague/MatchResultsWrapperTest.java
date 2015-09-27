package se.samuelandersson.rocketleague;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import se.samuelandersson.rocketleague.parser.CSVParser;
import se.samuelandersson.rocketleague.parser.Parser;

public class MatchResultsWrapperTest
{

  private final Parser parser = new CSVParser();

  @Test
  public void testCreate() throws Exception
  {
    MatchResultsWrapper wrapper = new MatchResultsWrapper(LogFileHelper.getValidCsvLogFile("simple.csv"), parser);
    assertNotNull(wrapper);
  }

  @Test
  public void testResults() throws Exception
  {
    MatchResultsWrapper empty = new MatchResultsWrapper(LogFileHelper.getValidCsvLogFile("empty.csv"), parser);
    assertFalse(empty.hasResults());
    assertNotNull(empty.getResults());
    assertEquals(empty.getResults().size(), 0);

    MatchResultsWrapper simple = new MatchResultsWrapper(LogFileHelper.getValidCsvLogFile("simple.csv"), parser);
    assertTrue(simple.hasResults());
    assertNotNull(simple.getResults());
    assertEquals(simple.getResults().size(), 1);
  }
}
