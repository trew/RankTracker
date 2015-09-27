package se.samuelandersson.rocketleague;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.samuelandersson.rocketleague.tasks.ScanTask;
import se.samuelandersson.rocketleague.utils.RankTrackerUtils;
import se.samuelandersson.rocketleague.utils.RocketLeagueUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * The main class of the RankTracker. The run method executes a scan of the Rocket League log folder and creates CSV
 * files according to what is found. Previous scans are included in the final files.
 * 
 * @author Samuel Andersson
 */
public class RankTracker
{
  private static final Logger log = LoggerFactory.getLogger(RankTracker.class);

  @Parameter(names = { "-b", "--base" }, description = "Base folder for operations.")
  public String base = "";

  @Parameter(names = { "-h", "--help" })
  public boolean help = false;

  public void run()
  {
    File baseFolder = new File(base).getAbsoluteFile();
    if (!baseFolder.exists())
    {
      log.error("Base folder {} does not exist. Aborting scan.", baseFolder.getAbsolutePath());
      return;
    }

    log.info("Executing task [scan]");
    new ScanTask().execute(RankTrackerUtils.getScannedFiles(baseFolder), baseFolder, RocketLeagueUtils.getRLFolder());
  }

  public static void main(String[] args) throws Exception
  {
    RankTracker tracker = new RankTracker();
    JCommander command = new JCommander(tracker);
    try
    {
      command.parse(args);
      if (tracker.help)
      {
        command.usage();
        return;
      }
    }
    catch (ParameterException e)
    {
      command.usage();
      return;
    }

    tracker.run();
  }
}
