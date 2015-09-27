package se.samuelandersson.rocketleague.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.samuelandersson.rocketleague.MatchResult;
import se.samuelandersson.rocketleague.ScannedFiles;
import se.samuelandersson.rocketleague.export.Exporter;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A utility file for various tasks.
 * 
 * @author Samuel Andersson
 */
public class RankTrackerUtils
{
  private static final Logger log = LoggerFactory.getLogger(RankTrackerUtils.class);

  public static final String SCANNEDFILES_FILENAME = "scannedfiles.json";

  /**
   * Separates the set of results into parts based on the {@link MatchResult#getPlayList()} value.
   * 
   * @param results the set of results to separate.
   * @return a map with playlist integers pointing to sets of match results.
   */
  public static Map<Integer, SortedSet<MatchResult>> separateResults(final SortedSet<MatchResult> results)
  {
    Map<Integer, SortedSet<MatchResult>> returnResults = new TreeMap<>();

    for (MatchResult result : results)
    {
      final int playList = result.getPlayList();
      if (!returnResults.containsKey(playList))
      {
        returnResults.put(playList, new TreeSet<MatchResult>());
      }

      returnResults.get(playList).add(result);
    }

    return returnResults;
  }

  /**
   * Exports the map created in {@link #separateResults(SortedSet)} to files in the provided folder.
   */
  public static void exportFiles(Map<Integer, SortedSet<MatchResult>> separated, Exporter exporter, File folder)
  {
    if (!folder.exists())
    {
      log.error("Folder to export to does not exist: [{}]", folder.getAbsolutePath());
      return;
    }

    for (Entry<Integer, SortedSet<MatchResult>> entry : separated.entrySet())
    {
      try
      {
        File outTemp = File.createTempFile("logparser-result-" + MatchResult.getPlaylistName(entry.getKey()), null);
        exporter.export(entry.getValue(), outTemp);

        File out = new File(folder, String.format("%s%s.%s",
                                                  exporter.getPrefix(),
                                                  MatchResult.getPlaylistName(entry.getKey()),
                                                  exporter.getSuffix()));
        Files.move(outTemp, out);
      }
      catch (IOException e)
      {
        log.error(String.format("Error when exporting results to file. %s"), e);
      }
    }
  }

  /**
   * Writes the provided ScannedFiles object to a file in the provided folder.
   * 
   * @param scannedFiles the instance to save to file.
   * @param baseFolder the folder which the file will be located in.
   */
  public static void writeScannedFilesToFile(final ScannedFiles scannedFiles, final File baseFolder)
  {
    if (scannedFiles == null || baseFolder == null)
    {
      return;
    }
    File scannedFilesFile = new File(baseFolder, SCANNEDFILES_FILENAME);

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(scannedFilesFile)))
    {
      log.info("Writing list of scanned files to {}", scannedFilesFile.getAbsolutePath());
      String json = gson.toJson(scannedFiles, ScannedFiles.class);
      writer.write(json);
    }
    catch (IOException e)
    {
      log.error(String.format("Error writing list of scanned files to file: %s", scannedFilesFile.getAbsolutePath()), e);
    }
  }

  /**
   * Reads the provided folder for the scanned files file, parses it and returns it.
   * 
   * @param baseFolder the folder to look for the scanned files file in.
   * @return a {@link ScannedFiles} instance from the read file, or a new instance of the file was not found or could
   *         not be read.
   */
  public static ScannedFiles getScannedFiles(final File baseFolder)
  {
    ScannedFiles scannedFiles = null;
    File scannedFilesFile = new File(baseFolder, SCANNEDFILES_FILENAME);
    Gson gson = new GsonBuilder().create();
    if (!scannedFilesFile.exists())
    {
      scannedFiles = new ScannedFiles();
    }
    else
    {
      try
      {
        scannedFiles = gson.fromJson(new FileReader(scannedFilesFile), ScannedFiles.class);
      }
      catch (final Exception e)
      {
        log.error(String.format("Error parsing file for list of scanned files: %s", scannedFilesFile.getAbsolutePath()),
                  e);
      }

      if (scannedFiles == null)
      {
        return new ScannedFiles();
      }
    }

    return scannedFiles;
  }

}
