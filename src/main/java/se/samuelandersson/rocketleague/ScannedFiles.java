package se.samuelandersson.rocketleague;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This file contains a list of files that are available in the Rocket League log folder but have previously been
 * scanned by the tracker. They are written to a json file and read back every time a scan occurs.
 * 
 * @author Samuel Andersson
 */
public class ScannedFiles
{
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + logFiles.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ScannedFiles other = (ScannedFiles) obj;
    if (!logFiles.equals(other.logFiles)) return false;
    return true;
  }

  private SortedSet<String> logFiles = new TreeSet<>();

  /**
   * Returns the set of log files this instance contains.
   * 
   * @return the set of log files this instance contains.
   */
  public SortedSet<String> getLogFiles()
  {
    return logFiles;
  }
}