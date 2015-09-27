package se.samuelandersson.rocketleague.utils;

import static org.testng.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.samuelandersson.rocketleague.MatchResult;
import se.samuelandersson.rocketleague.MatchResultsWrapper;
import se.samuelandersson.rocketleague.ScannedFiles;
import se.samuelandersson.rocketleague.export.Exporter;

public class RankTrackerUtilsTest
{
  @Test
  public void testSeparateResults() throws Exception
  {
    SortedSet<MatchResult> results = createResults();

    Map<Integer, SortedSet<MatchResult>> separated = RankTrackerUtils.separateResults(results);

    assertEquals(separated.size(), 4);
    assertTrue(separated.keySet().contains(MatchResult.RANKED_1V1));
    assertTrue(separated.keySet().contains(MatchResult.RANKED_2V2));
    assertTrue(separated.keySet().contains(MatchResult.RANKED_3V3));
    assertTrue(separated.keySet().contains(MatchResult.SOLO_RANKED_3V3));
    for (Entry<Integer, SortedSet<MatchResult>> entry : separated.entrySet())
    {
      assertEquals(entry.getValue().size(), 2);
    }
  }

  private SortedSet<MatchResult> createResults()
  {
    SortedSet<MatchResult> results = new TreeSet<>();

    results.add(new MatchResult(new DateTime(0), MatchResult.RANKED_1V1, 1, 10));
    results.add(new MatchResult(new DateTime(1000), MatchResult.RANKED_1V1, 1, 11));

    results.add(new MatchResult(new DateTime(2000), MatchResult.RANKED_2V2, 2, 10));
    results.add(new MatchResult(new DateTime(3000), MatchResult.RANKED_2V2, 2, 12));

    results.add(new MatchResult(new DateTime(4000), MatchResult.RANKED_3V3, 3, 10));
    results.add(new MatchResult(new DateTime(5000), MatchResult.RANKED_3V3, 3, 13));

    results.add(new MatchResult(new DateTime(6000), MatchResult.SOLO_RANKED_3V3, 4, 10));
    results.add(new MatchResult(new DateTime(7000), MatchResult.SOLO_RANKED_3V3, 4, 14));

    return results;
  }

  @Test
  public void testExportFiles() throws Exception
  {
    DummyExporter exporter = new DummyExporter();
    SortedSet<MatchResult> results = createResults();
    File folder = Files.createTempDirectory("ranktracker").toFile();

    Map<Integer, SortedSet<MatchResult>> separated = RankTrackerUtils.separateResults(results);

    RankTrackerUtils.exportFiles(separated, exporter, folder);

    List<String> files = Lists.newArrayList(folder.list());
    assertEquals(files.size(), 4);
    assertTrue(files.contains("results-1v1.txt"));
    assertTrue(files.contains("results-2v2.txt"));
    assertTrue(files.contains("results-3v3.txt"));
    assertTrue(files.contains("results-solo-3v3.txt"));
  }

  @Test
  public void testExportFilesBadFolder() throws Exception
  {
    DummyExporter exporter = new DummyExporter();
    SortedSet<MatchResult> results = createResults();
    File folder = Files.createTempDirectory("ranktracker").toFile();
    Files.delete(folder.toPath());

    Map<Integer, SortedSet<MatchResult>> separated = RankTrackerUtils.separateResults(results);
    RankTrackerUtils.exportFiles(separated, exporter, folder);
  }

  @Test
  public void testWriteMetaDataToFileNullArgs() throws Exception
  {
    RankTrackerUtils.writeScannedFilesToFile(null, null);
    RankTrackerUtils.writeScannedFilesToFile(new ScannedFiles(), null);
    RankTrackerUtils.writeScannedFilesToFile(null, Files.createTempFile(null, null).toFile());
  }

  @Test
  public void testWriteMetaDataToFile() throws Exception
  {
    ScannedFiles metaData = new ScannedFiles();
    metaData.getLogFiles().add("log1.log");
    File baseFolder = Files.createTempDirectory(null).toFile();
    File scannedFilesFile = new File(baseFolder, RankTrackerUtils.SCANNEDFILES_FILENAME);
    assertFalse(scannedFilesFile.exists());

    RankTrackerUtils.writeScannedFilesToFile(metaData, baseFolder);

    assertTrue(scannedFilesFile.exists());
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ScannedFiles readData = gson.fromJson(new FileReader(scannedFilesFile), ScannedFiles.class);
    assertEquals(readData, metaData);
  }

  @Test
  public void testScannedFiles() throws Exception
  {
    {
      File baseFolder = Files.createTempDirectory(null).toFile();
      ScannedFiles data = RankTrackerUtils.getScannedFiles(baseFolder);
      assertNotNull(data);
      assertTrue(data.getLogFiles().isEmpty());
    }
    {
      File baseFolder = Files.createTempDirectory(null).toFile();
      ScannedFiles scannedFiles = new ScannedFiles();
      scannedFiles.getLogFiles().add("log1.log");

      RankTrackerUtils.writeScannedFilesToFile(scannedFiles, baseFolder);

      ScannedFiles data = RankTrackerUtils.getScannedFiles(baseFolder);
      assertEquals(data, scannedFiles);
    }
    {
      File baseFolder = Files.createTempDirectory(null).toFile();
      File file = Files.createTempFile(null, null).toFile();

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
      {
        writer.write("{ badformat:: }");
      }

      Files.move(file.toPath(), new File(baseFolder, RankTrackerUtils.SCANNEDFILES_FILENAME).toPath());

      ScannedFiles data = RankTrackerUtils.getScannedFiles(baseFolder);
      assertNotNull(data);
      assertTrue(data.getLogFiles().isEmpty());
    }
  }

  private static class DummyExporter implements Exporter
  {
    @Override
    public void export(MatchResultsWrapper parser, File file) throws IOException
    { // Do nothing
    }

    @Override
    public void export(SortedSet<MatchResult> results, File file) throws IOException
    { // Do nothing
    }

    @Override
    public String toString(SortedSet<MatchResult> results)
    {
      return null;
    }

    @Override
    public String toString(MatchResultsWrapper parser)
    {
      return null;
    }

    @Override
    public String getPrefix()
    {
      return "results-";
    }

    @Override
    public String getSuffix()
    {
      return "txt";
    }
  }
}
