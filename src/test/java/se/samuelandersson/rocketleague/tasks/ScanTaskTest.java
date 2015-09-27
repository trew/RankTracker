package se.samuelandersson.rocketleague.tasks;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import se.samuelandersson.rocketleague.LogFileHelper;
import se.samuelandersson.rocketleague.MatchResult;
import se.samuelandersson.rocketleague.ScannedFiles;
import se.samuelandersson.rocketleague.utils.RocketLeagueUtils;

public class ScanTaskTest
{
  @Test
  public void testDefaultLogFile() throws Exception
  {
    assertEquals(ScanTask.DEFAULT_LOGFILE, "Launch.log");
  }

  @Test
  public void testRetainLogFiles() throws Exception
  {
    ScannedFiles scannedFiles = createScannedFiles();
    File[] logFiles = createLogFiles();

    ScanTask task = new ScanTask();
    task.retainLogFiles(scannedFiles, logFiles);

    assertEquals(scannedFiles.getLogFiles().size(), 2);
    assertTrue(scannedFiles.getLogFiles().contains("log1.log"));
    assertTrue(scannedFiles.getLogFiles().contains("log2.log"));
  }

  @Test
  public void testGetFilesToParse() throws Exception
  {
    ScannedFiles scannedFiles = createScannedFiles();
    scannedFiles.getLogFiles().add(ScanTask.DEFAULT_LOGFILE);
    File[] logFiles = createLogFiles();

    ScanTask task = new ScanTask();
    List<File> files = task.getFilesToParse(scannedFiles, logFiles);
    assertTrue(files.contains(new File(ScanTask.DEFAULT_LOGFILE)));
    assertTrue(files.contains(new File("log4.log")));
    assertFalse(scannedFiles.getLogFiles().contains(ScanTask.DEFAULT_LOGFILE));
    assertEquals(files.size(), 2);
  }

  @Test
  public void testGetMetaDataFile() throws Exception
  {
    File baseFolder = createBaseFolder();

    ScanTask task = new ScanTask();
    File scannedFilesFile = task.getScannedFilesFile(baseFolder);
    assertNotNull(scannedFilesFile);
    assertTrue(scannedFilesFile.exists());

    File scannedFilesFile2 = task.getScannedFilesFile(baseFolder);

    assertEquals(scannedFilesFile.getAbsoluteFile(), scannedFilesFile2.getAbsoluteFile());

    scannedFilesFile.delete();
    baseFolder.delete();
  }

  @Test
  public void testGetScannedFilesFileMissingBaseFolder() throws Exception
  {
    File baseFolder = createBaseFolder();
    baseFolder.delete();

    ScanTask task = new ScanTask();
    task.getScannedFilesFile(baseFolder);
  }

  @Test
  public void testGetCSVFolder() throws Exception
  {
    File baseFolder = createBaseFolder();
    File csv = ScanTask.getCSVFolder(baseFolder);
    assertTrue(csv.exists());
    assertEquals(csv.getName(), "csv");
    assertEquals(csv.getParentFile(), baseFolder);
    assertEquals(ScanTask.getCSVFolder(baseFolder), csv);
    csv.delete();
    baseFolder.delete();
    assertNull(ScanTask.getCSVFolder(baseFolder));
  }

  @Test
  public void testGetRLLogFolder()
  {
    assertNull(ScanTask.getRLLogFolder(null));

    File rlFolder = RocketLeagueUtils.getRLFolder();
    assertNotNull(rlFolder);

    File logFolder = ScanTask.getRLLogFolder(rlFolder);
    assertNotNull(logFolder);

    assertTrue(logFolder.isDirectory());
    assertEquals(logFolder.getName(), "Logs");
    assertEquals(logFolder.getParentFile(), rlFolder);
  }

  @Test
  public void testReadCsvFiles() throws Exception
  {
    // setup initial state
    File baseFolder = createBaseFolder();
    File folder = new File(baseFolder, "folder");
    Files.createDirectory(folder.toPath());
    File csv1 = LogFileHelper.getValidCsvLogFile("log.csv");
    File csv2 = LogFileHelper.getValidCsvLogFile("simple.csv");
    Path path1 = Files.copy(csv1.toPath(), new File(baseFolder, csv1.getName()).toPath());
    Path path2 = Files.copy(csv2.toPath(), new File(baseFolder, csv2.getName()).toPath());

    File[] files = baseFolder.listFiles();
    SortedSet<MatchResult> results = new TreeSet<>();

    ScanTask task = new ScanTask();
    task.readCsvFiles(files, results);

    assertEquals(results.size(), 3);

    // cleanup
    Files.delete(path1);
    Files.delete(path2);
    Files.delete(folder.toPath());
    Files.delete(baseFolder.toPath());
  }

  @Test
  public void testParseLogFiles() throws Exception
  {
    // setup initial state
    File baseFolder = createBaseFolder();
    File folder = new File(baseFolder, "folder");
    Files.createDirectory(folder.toPath());
    File log1 = LogFileHelper.getValidRLLogFile("ranked.log");
    File log2 = LogFileHelper.getValidRLLogFile("mixed.log");
    Path path1 = Files.copy(log1.toPath(), new File(baseFolder, log1.getName()).toPath());
    Path path2 = Files.copy(log2.toPath(), new File(baseFolder, log2.getName()).toPath());

    List<File> files = Lists.newArrayList(baseFolder.listFiles());
    SortedSet<MatchResult> results = new TreeSet<>();

    ScanTask.parseLogFiles(files, results);

    assertEquals(results.size(), 11);

    Files.delete(path1);
    Files.delete(path2);
    Files.delete(folder.toPath());
    Files.delete(baseFolder.toPath());
  }

  @Test
  public void testExecute() throws Exception
  {
    File baseFolder = createBaseFolder();
    try
    {
      File rlFolder = createBaseFolder();
      File logFolder = new File(rlFolder, "Logs");
      Files.createDirectory(logFolder.toPath());
      File log1 = LogFileHelper.getValidRLLogFile("ranked.log");
      File log2 = LogFileHelper.getValidRLLogFile("mixed.log");
      Files.copy(log1.toPath(), new File(logFolder, log1.getName()).toPath());
      Files.copy(log2.toPath(), new File(logFolder, log2.getName()).toPath());

      File csvFolder = new File(baseFolder, "csv");
      Files.createDirectory(csvFolder.toPath());
      File csv1 = LogFileHelper.getValidCsvLogFile("log.csv");
      File csv2 = LogFileHelper.getValidCsvLogFile("simple.csv");
      Files.copy(csv1.toPath(), new File(csvFolder, csv1.getName()).toPath());
      Files.copy(csv2.toPath(), new File(csvFolder, csv2.getName()).toPath());

      ScanTask task = new ScanTask();
      ScannedFiles scannedFiles = new ScannedFiles();
      scannedFiles.getLogFiles().add("mixed.log");

      task.execute(scannedFiles, baseFolder, rlFolder);

      List<File> csvFiles = Lists.newArrayList(csvFolder.listFiles());
      assertTrue(csvFiles.contains(new File(csvFolder, "results-solo-3v3.csv")));
      assertTrue(csvFiles.contains(new File(csvFolder, "results-1v1.csv")));
      assertTrue(csvFiles.contains(new File(csvFolder, "results-2v2.csv")));
      assertTrue(csvFiles.contains(new File(csvFolder, "results-3v3.csv")));
      assertEquals(csvFiles.size(), 6);

    }
    finally
    {
      deleteFolderTree(baseFolder);
    }
  }

  @Test
  public void testExecuteBadLogFolder() throws Exception
  {
    File baseFolder = createBaseFolder();
    ScanTask task = new ScanTask();
    task.execute(new ScannedFiles(), baseFolder, null);

    deleteFolderTree(baseFolder);
  }

  private void deleteFolderTree(File folder) throws Exception
  {
    Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>()
    {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
      {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
      {
        if (exc == null)
        {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }

        throw exc;
      }
    });

  }

  private File createBaseFolder() throws Exception
  {
    return Files.createTempDirectory("temp").toFile();
  }

  private File[] createLogFiles()
  {
    return new File[] { new File(ScanTask.DEFAULT_LOGFILE), new File("log1.log"), new File("log2.log"),
        new File("log4.log") };
  }

  private ScannedFiles createScannedFiles()
  {
    ScannedFiles scannedFiles = new ScannedFiles();
    scannedFiles.getLogFiles().add("log1.log");
    scannedFiles.getLogFiles().add("log2.log");
    scannedFiles.getLogFiles().add("log3.log");
    return scannedFiles;
  }
}
