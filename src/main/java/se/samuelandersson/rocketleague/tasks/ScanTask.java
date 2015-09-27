package se.samuelandersson.rocketleague.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.samuelandersson.rocketleague.MatchResult;
import se.samuelandersson.rocketleague.MatchResultsWrapper;
import se.samuelandersson.rocketleague.ScannedFiles;
import se.samuelandersson.rocketleague.export.CSVExporter;
import se.samuelandersson.rocketleague.parser.CSVParser;
import se.samuelandersson.rocketleague.parser.LogFileParser;
import se.samuelandersson.rocketleague.utils.RankTrackerUtils;

import com.beust.jcommander.internal.Lists;

/**
 * Essentially, a ScanTask performs a scan of the Rocket League log folder and adds any new MatchResults to a master
 * list of MatchResults. It does so in a few steps.
 * 
 * <p>
 * <ol>
 * <li>Import the match results from the CSV files that were created in previous scans.
 * <li>Scan the Rocket League log folder for new log files that has not yet been parsed.
 * <li>Parse the log files and add any new match results to the list of results.
 * <li>Export the results into different files, depending on the playlist(1v1, solo-3v3, etc)
 * </ol>
 *
 * Worth noting is that the Launch.log file will always be parsed.
 * 
 * @author Samuel Andersson
 */
public class ScanTask implements Task
{
  private static final Logger log = LoggerFactory.getLogger(ScanTask.class);

  public static final String DEFAULT_LOGFILE = "Launch.log";

  @Override
  public void execute(final ScannedFiles scannedFiles, final File baseFolder, final File rlFolder)
  {
    File csvFolder = getCSVFolder(baseFolder);
    File logFolder = getRLLogFolder(rlFolder);
    if (logFolder == null)
    {
      log.error("Error getting folder for log files. Aborting scan.");
      return;
    }

    // read all existing match results from csv files
    File[] csvFiles = csvFolder.listFiles();
    final SortedSet<MatchResult> results = new TreeSet<>();
    readCsvFiles(csvFiles, results);

    // determine which log files that should be parsed.
    File[] logFiles = logFolder.listFiles();
    final List<File> filesToParse = getFilesToParse(scannedFiles, logFiles);

    // Set of files determined, parse them and add the results to the master list.
    parseLogFiles(filesToParse, results);

    // Split the list into multiple list and export them to different files
    Map<Integer, SortedSet<MatchResult>> splitResults = RankTrackerUtils.separateResults(results);

    // Export the results into the various files
    RankTrackerUtils.exportFiles(splitResults, new CSVExporter(), csvFolder);

    // write list of scanned files to file
    RankTrackerUtils.writeScannedFilesToFile(scannedFiles, baseFolder);
  }

  /**
   * Removes any files from the {@link ScannedFiles} instance that are not present in the provided list of log files.
   * 
   * @param scannedFiles the scannedFiles instance to remove files from.
   * @param logFiles the log files from the Rocket League log folder.
   */
  protected void retainLogFiles(final ScannedFiles scannedFiles, final File[] logFiles)
  {
    SortedSet<String> retained = new TreeSet<>();
    for (File file : logFiles)
    {
      if (!file.getName().equals(DEFAULT_LOGFILE))
      {
        retained.add(file.getName());
      }
    }
    scannedFiles.getLogFiles().retainAll(retained);
  }

  /**
   * Parses a set of CSV files and adds the results to the provided {@link SortedSet}.
   * 
   * @param files the files to parse.
   * @param results the result set to add the {@link MatchResult}s to.
   */
  protected void readCsvFiles(final File[] files, final SortedSet<MatchResult> results)
  {
    for (File csvFile : files)
    {
      if (!csvFile.isFile())
      {
        continue;
      }

      MatchResultsWrapper parser = new MatchResultsWrapper(csvFile, new CSVParser());
      results.addAll(parser.getResults());
    }
  }

  /**
   * Based on the previously parsed files and the provided log files, this method determines which new files are to be
   * parsed. Generally, "Launch.log" will always be scanned.
   * 
   * @param scannedFiles the {@link ScannedFiles} instance.
   * @param logFiles the Rocket League log files available.
   * @return a list of files to parse.
   */
  protected List<File> getFilesToParse(final ScannedFiles scannedFiles, final File[] logFiles)
  {
    retainLogFiles(scannedFiles, logFiles);
    final List<File> filesToParse = Lists.newArrayList();
    for (File file : logFiles)
    {
      if (file.getName().equals(DEFAULT_LOGFILE))
      {
        filesToParse.add(file);
      }
      else if (!scannedFiles.getLogFiles().contains(file.getName()))
      {
        filesToParse.add(file);
        scannedFiles.getLogFiles().add(file.getName());
      }
    }
    return filesToParse;
  }

  /**
   * Returns the file that contains the ScannedFiles object.
   * 
   * @param baseFolder the folder to locate or create the file in.
   * @return the previously created file for this purpose, or a newly created one.
   */
  protected File getScannedFilesFile(final File baseFolder)
  {
    File scannedFilesFile = new File(baseFolder, RankTrackerUtils.SCANNEDFILES_FILENAME);
    if (!scannedFilesFile.exists())
    {
      log.info("Creating file for list of scanned files: {}", scannedFilesFile.getAbsolutePath());
      try
      {
        Files.createFile(scannedFilesFile.toPath());
      }
      catch (IOException e)
      {
        log.error(String.format("Error creating file for list of scanned files: %s", scannedFilesFile.getAbsolutePath()),
                  e);
        return null;
      }
    }

    return scannedFilesFile;
  }

  /**
   * Parses a list of Rocket League log files and adds the found {@link MatchResult}s to the provided set of results.
   * 
   * @param files the files to parse
   * @param results the result set that any MatchResult is added to.
   */
  protected static void parseLogFiles(final List<File> files, final SortedSet<MatchResult> results)
  {
    for (File file : files)
    {
      if (!file.isFile())
      {
        continue;
      }

      log.info("Parsing {}", file);

      results.addAll(new MatchResultsWrapper(file, new LogFileParser()).getResults());
    }
  }

  /**
   * Returns the folder for exported CSV files.
   * 
   * @param baseFolder the folder which the CSV folder will be found or created.
   * @return the folder for exported CSV files.
   */
  protected static File getCSVFolder(final File baseFolder)
  {
    if (!baseFolder.exists())
    {
      log.error("Base folder does not exist. [%s]", baseFolder.getAbsolutePath());
      return null;
    }

    final File csvFolder = new File(baseFolder, "csv");
    if (!csvFolder.exists())
    {
      log.info("Creating csv folder: {}", csvFolder.getAbsolutePath());
      try
      {
        Files.createDirectories(csvFolder.toPath());
      }
      catch (IOException e)
      {
        log.error("Error creating directories to CSV folder");
        return null;
      }
    }

    return csvFolder;
  }

  /**
   * Returns the Log folder for Rocket League.
   * 
   * @param rlFolder the Rocket League data folder
   * @return the Log folder for Rocket League, or {@code null} if the provided folder is null.
   */
  protected static File getRLLogFolder(final File rlFolder)
  {
    if (rlFolder == null)
    {
      return null;
    }

    return new File(rlFolder, "Logs");
  }

}
