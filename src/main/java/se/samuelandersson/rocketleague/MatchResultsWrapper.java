package se.samuelandersson.rocketleague;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

import se.samuelandersson.rocketleague.parser.Parser;

/**
 * A MatchResultsWrapper contains a parsed set of {@link MatchResult}s.
 * 
 * @author Samuel Andersson
 */
public class MatchResultsWrapper
{
  private SortedSet<MatchResult> results;

  /**
   * Create a new MatchResultsWrapper and parse the provided file. The file can be either a Rocket League log file or a
   * previously exported CSV file, depending on the parser provided.
   * 
   * @param file The file to be parsed.
   * @param parser The parser to use to parse the file.
   * @throws IOException If an error occurred when reading the file.
   */
  public MatchResultsWrapper(File file, Parser parser)
  {
    results = parser.parse(file);
  }

  /**
   * Returns true if this parser has any results.
   * 
   * @return true if this parser has any results, false otherwise.
   */
  public boolean hasResults()
  {
    return !results.isEmpty();
  }

  /**
   * Returns the parsed results. Can be an empty set.
   * 
   * @return the parsed results. Can be an empty set. Never null.
   */
  public SortedSet<MatchResult> getResults()
  {
    return results;
  }
}
