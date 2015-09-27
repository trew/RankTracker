package se.samuelandersson.rocketleague;

import java.io.File;

public class LogFileHelper
{
  public static File getValidCsvLogFile(String fileName)
  {
    return new File(LogFileHelper.class.getResource("csv/validLogs/" + fileName).getFile());
  }

  public static File getInvalidCsvLogFile(String fileName)
  {
    return new File(LogFileHelper.class.getResource("csv/invalidLogs/" + fileName).getFile());
  }

  public static File getValidRLLogFile(String fileName)
  {
    return new File(LogFileHelper.class.getResource("rllog/validLogs/" + fileName).getFile());
  }

  public static File getInvalidRLLogFile(String fileName)
  {
    return new File(LogFileHelper.class.getResource("rllog/invalidLogs/" + fileName).getFile());
  }
}
