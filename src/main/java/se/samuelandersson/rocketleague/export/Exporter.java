package se.samuelandersson.rocketleague.export;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

import se.samuelandersson.rocketleague.MatchResultsWrapper;
import se.samuelandersson.rocketleague.MatchResult;

/**
 * An interface for exporting parsed results.
 * 
 * @author Samuel Andersson
 */
public interface Exporter
{
  /**
   * Exports a parser into a given file.
   * 
   * @param parser the parser to export.
   * @param file the file to export into.
   * @throws IOException if an error happens when writing the file.
   */
  public void export(MatchResultsWrapper parser, File file) throws IOException;

  /**
   * Exports a list of results into a given file.
   * 
   * @param results the results to export.
   * @param file the file to export into.
   * @throws IOException if an error happens when writing the file.
   */
  public void export(SortedSet<MatchResult> results, File file) throws IOException;

  /**
   * Returns a string of the results in the format this formatter provides.
   * 
   * @param results the results to create a string from.
   * @return a string of the results in the format this formatter provides.
   */
  public String toString(SortedSet<MatchResult> results);

  /**
   * Returns a string of the results from the parser in the format this formatter provides.
   * 
   * @param parser the parser whose results a string should be created from.
   * @return a string of the results from the parser in the format this formatter provides.
   */
  public String toString(MatchResultsWrapper parser);

  /**
   * Returns the preferred prefix of the files this parser exports to.
   * 
   * @return the preferred prefix of the files this parser exports to.
   */
  public String getPrefix();

  /**
   * Returns the preferred suffix of the files this parser exports to, without the dot.
   * <p>
   * Example:
   * 
   * <pre>
   * return &quot;csv&quot;;
   * </pre>
   * 
   * </p>
   * 
   * @return the preferred suffix of the files this parser exports to, without the dot.
   */
  public String getSuffix();
}
