package se.samuelandersson.rocketleague;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class ScannedFilesTest
{
  @Test
  public void testEquals() throws Exception
  {
    ScannedFiles data1 = new ScannedFiles();
    assertEquals(data1, data1);

    ScannedFiles data2 = new ScannedFiles();
    assertEquals(data1, data2);

    data1.getLogFiles().add("log1.log");
    assertNotEquals(data1, data2);
    data2.getLogFiles().add("log1.log");
    assertEquals(data1, data2);
    assertEquals(data1, data1);
    assertFalse(data1.equals(""));
    assertFalse(data1.equals(null));
  }
}
