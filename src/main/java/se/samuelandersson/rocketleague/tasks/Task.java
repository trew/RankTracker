package se.samuelandersson.rocketleague.tasks;

import java.io.File;

import se.samuelandersson.rocketleague.ScannedFiles;
import se.samuelandersson.rocketleague.utils.RocketLeagueUtils;

/**
 * A task performs something.
 * 
 * @author Samuel Andersson
 */
public interface Task
{
  /**
   * Executes the task.
   * 
   * @param scannedFiles the set of previously scanned files.
   * @param baseFolder the base folder of operations.
   * @param rlFolder the rocket league data folder. See {@link RocketLeagueUtils#getRLFolder()}.
   */
  public void execute(ScannedFiles scannedFiles, File baseFolder, File rlFolder);
}
