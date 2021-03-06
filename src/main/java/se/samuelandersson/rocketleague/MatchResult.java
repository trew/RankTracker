package se.samuelandersson.rocketleague;

import org.joda.time.DateTime;

/**
 * This class provides data about the conclusion of a match, such as points gained/lossed, which playlist and the points
 * at the time of the match. It implements the {@link Comparable} interface, which allows it to be sorted
 * using the {@link DateTime} property of the result. Worth noting regarding the unranked playlist is that it is the
 * same playlist for all four modes and rating changes applies across all of them.
 * 
 * @author Samuel Andersson
 */
public class MatchResult implements Comparable<MatchResult>
{
  public static final int UNRANKED = 0;
  public static final int RANKED_1V1 = 10;
  public static final int RANKED_2V2 = 11;
  public static final int SOLO_RANKED_3V3 = 12;
  public static final int RANKED_3V3 = 13;

  private int playList;
  private int rankPreGame;
  private int deltaPoints;
  private float skillMean;
  private float skillSigma;
  private DateTime time;

  public MatchResult(DateTime time, int playList, int deltaPoints, int rankPreGame)
  {
    init(time, playList, deltaPoints, rankPreGame, -1, -1);
  }

  public MatchResult(DateTime time, String playList, int deltaPoints, int rankPreGame)
  {
    if (playList == null)
    {
      throw new NullPointerException("playList");
    }

    init(time, MatchResult.getPlaylist(playList), deltaPoints, rankPreGame, -1, -1);
  }

  public MatchResult(DateTime time, int playList, int deltaPoints, int rankPreGame, float skillMean, float skillSigma)
  {
    init(time, playList, deltaPoints, rankPreGame, skillMean, skillSigma);
  }

  public MatchResult(DateTime time, String playList, int deltaPoints, int rankPreGame, float skillMean, float skillSigma)
  {
    if (playList == null)
    {
      throw new NullPointerException("playList");
    }

    init(time, MatchResult.getPlaylist(playList), deltaPoints, rankPreGame, skillMean, skillSigma);
  }

  private void init(DateTime time, int playList, int deltaPoints, int rankPreGame, float skillMean, float skillSigma)
  {
    if (time == null)
    {
      throw new NullPointerException("Time cannot be null");
    }
    // Exclude millis
    this.time = new DateTime(time.getYear(),
                             time.getMonthOfYear(),
                             time.getDayOfMonth(),
                             time.getHourOfDay(),
                             time.getMinuteOfHour(),
                             time.getSecondOfMinute());

    if (!isValidPlayList(playList))
    {
      throw new IllegalArgumentException(String.format("playlist must be a valid playlist: %s", playList));
    }

    this.playList = playList;
    this.deltaPoints = deltaPoints;
    this.rankPreGame = rankPreGame;
    this.skillMean = skillMean;
    this.skillSigma = skillSigma;
  }

  /**
   * Returns the time when the match was concluded.
   * 
   * @return the time when the match was concluded.
   */
  public DateTime getTime()
  {
    return time;
  }

  /**
   * Returns the playlist for the match.
   * 
   * @return the playlist for the match.
   * @see {@link MatchResult#getPlaylistName(int)}
   */
  public int getPlayList()
  {
    return playList;
  }

  /**
   * Returns a short readable value for the provided playlist, suitable for filenames.
   * 
   * <pre>
   * 10 = 1v1
   * 11 = 2v2
   * 12 = solo-3v3
   * 13 = 3v3
   *  0 = unranked
   * </pre>
   * 
   * @param playlist the playlist to get a readable value from.
   * @return a readable value for the provided playlist, or "unknown" if playlist value wasn't recognized.
   */
  public static String getPlaylistName(final int playList)
  {
    switch (playList)
    {
      case RANKED_1V1:
        return "1v1";
      case RANKED_2V2:
        return "2v2";
      case SOLO_RANKED_3V3:
        return "solo-3v3";
      case RANKED_3V3:
        return "3v3";
      case UNRANKED:
        return "unranked";
    }

    return String.valueOf(playList);
  }

  /**
   * Returns true if the provided playlist number is a valid playlist.
   * 
   * @param playlist the playlist to check.
   * @return true if the provided playlist number is a valid playlist.
   */
  public static boolean isValidPlayList(final int playlist)
  {
    return (playlist >= RANKED_1V1 && playlist <= RANKED_3V3) || playlist == UNRANKED;
  }

  /**
   * Reverse lookup for {@link #getPlaylistName(int)} and {@link #getPlaylistName(int)}
   * 
   * @param name the Name to look up
   * @return The playlist ID corresponding to the given name, or -1 if {@code null} or not recognized.
   */
  public static int getPlaylist(final String name)
  {
    if (name == null)
    {
      return -1;
    }

    switch (name)
    {
      case "1v1":
      case "Ranked 1v1":
        return RANKED_1V1;
      case "2v2":
      case "Ranked 2v2":
        return RANKED_2V2;
      case "solo-3v3":
      case "Solo Ranked 3v3":
        return SOLO_RANKED_3V3;
      case "3v3":
      case "Ranked 3v3":
        return RANKED_3V3;
      case "unranked":
        return UNRANKED;
      default:
        try
        {
          return Integer.parseInt(name);
        }
        catch (NumberFormatException e)
        {
          return -1;
        }

    }
  }

  /**
   * Returns the rank value before the match was concluded.
   * 
   * @return the rank value before the match was concluded.
   */
  public int getRankPreGame()
  {
    return rankPreGame;
  }

  /**
   * Returns the amount of points gained or lossed when the match was concluded.
   * 
   * @return the amount of points gained or lossed when the match was concluded.
   */
  public int getDeltaPoints()
  {
    return deltaPoints;
  }

  /**
   * Returns the rank value after the match was concluded.
   * 
   * @return the rank value after the match was concluded.
   */
  public int getRankPostGame()
  {
    return rankPreGame + deltaPoints;
  }

  /**
   * Returns the skill mean value for the player in the match.
   * 
   * @return the skill mean value for the player in the match.
   */
  public float getSkillMean()
  {
    return skillMean;
  }

  /**
   * Returns the skill standard deviation (or sigma) for the player in the match.
   * 
   * @return the skill standard deviation (or sigma) for the player in the match.
   */
  public float getSkillSigma()
  {
    return skillSigma;
  }

  /**
   * Returns true if {@link #getDeltaPoints()} is over 0. This means it's not possible to detect a win where the player
   * got 0 points.
   * 
   * @return true if {@link #getDeltaPoints()} is over 0.
   */
  public boolean isWin()
  {
    return deltaPoints > 0;
  }

  @Override
  public String toString()
  {
    return String.format("%s,%s,%s,%s,%s,%s",
                         getTime().toString("YYYY-MM-dd HH:mm:ss"),
                         getPlaylistName(getPlayList()),
                         getSkillMean(),
                         getSkillSigma(),
                         getDeltaPoints(),
                         getRankPreGame());
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + deltaPoints;
    result = prime * result + playList;
    result = prime * result + rankPreGame;
    result = prime * result + Float.floatToIntBits(skillMean);
    result = prime * result + Float.floatToIntBits(skillSigma);
    result = prime * result + ((time == null) ? 0 : time.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MatchResult other = (MatchResult) obj;
    if (deltaPoints != other.deltaPoints) return false;
    if (playList != other.playList) return false;
    if (rankPreGame != other.rankPreGame) return false;
    if (Float.floatToIntBits(skillMean) != Float.floatToIntBits(other.skillMean)) return false;
    if (Float.floatToIntBits(skillSigma) != Float.floatToIntBits(other.skillSigma)) return false;
    if (time == null)
    {
      if (other.time != null) return false;
    }
    else if (!time.equals(other.time)) return false;
    return true;
  }

  @Override
  public int compareTo(final MatchResult other)
  {
    if (getTime().isBefore(other.getTime()))
    {
      return -1;
    }
    if (getTime().isAfter(other.getTime()))
    {
      return 1;
    }

    return 0;
  }

}