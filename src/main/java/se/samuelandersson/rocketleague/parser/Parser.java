package se.samuelandersson.rocketleague.parser;

import java.io.File;
import java.util.SortedSet;

import se.samuelandersson.rocketleague.MatchResult;

/**
 * An interface for parsing various kinds of files containing data about match results. This could be previously parsed
 * CSV files or Rocket League log files.
 * 
 * @author Samuel Andersson
 */
public interface Parser
{
  /**
   * Parses a file and returns a {@link SortedSet} containing the {@link MatchResult}s that was extracted from the file.
   * 
   * @param file the file to parse.
   * @return a set of MatchResults.
   */
  SortedSet<MatchResult> parse(File file);
}
