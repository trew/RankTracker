package se.samuelandersson.rocketleague.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility file for various Rocket League related tasks.
 * 
 * @author Samuel Andersson
 */
public class RocketLeagueUtils
{
  private static final Logger log = LoggerFactory.getLogger(RocketLeagueUtils.class);

  /**
   * Returns the Rocket League data folder. It's located under
   * "%USERPROFILE%/My Documents/My Games/Rocket League/TAGame"
   * 
   * @return the Rocket League data folder, or {@code null} if something went wrong.
   */
  public static File getRLFolder()
  {
    try
    {
      String myDocuments = null;
      Process p = Runtime.getRuntime()
                         .exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
      p.waitFor();

      InputStream in = p.getInputStream();
      byte[] b;
      b = new byte[in.available()];
      in.read(b);
      in.close();

      myDocuments = new String(b);
      myDocuments = myDocuments.split("\\s\\s+")[4];

      return new File(myDocuments + "\\my games\\Rocket League\\TAGame");
    }
    catch (IOException | InterruptedException e)
    {
      log.error("Error when finding Rocket League folder.", e);
    }

    return null;
  }
}
