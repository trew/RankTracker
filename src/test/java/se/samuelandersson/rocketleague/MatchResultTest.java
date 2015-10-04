package se.samuelandersson.rocketleague;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MatchResultTest
{
  @Test
  public void testCreate() throws Exception
  {
    assertNotNull(new MatchResult(new DateTime(), MatchResult.RANKED_1V1, 0, 0));
  }

  @Test
  public void testCreateWithStringPlaylist() throws Exception
  {
    assertNotNull(new MatchResult(new DateTime(), MatchResult.getPlaylistName(MatchResult.RANKED_1V1), 0, 0));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testCreateNullPlayList() throws Exception
  {
    assertNotNull(new MatchResult(new DateTime(), (String) null, 0, 0));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testCreateNullTime() throws Exception
  {
    assertNotNull(new MatchResult(null, 0, 0, 0));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateNotRankedPlaylist() throws Exception
  {
    assertNotNull(new MatchResult(new DateTime(), 1, 0, 0));
  }

  @Test
  public void testValues() throws Exception
  {
    DateTime dt = new DateTime("2015-01-02T10:11:12");
    MatchResult mr = new MatchResult(dt, MatchResult.RANKED_1V1, 10, 800);

    assertEquals(mr.getDeltaPoints(), 10);
    assertEquals(mr.getPlayList(), MatchResult.RANKED_1V1);
    assertEquals(mr.getRankPreGame(), 800);
    assertEquals(mr.getRankPostGame(), 810);
    assertEquals(mr.getTime(), dt);
    assertEquals(mr.isWin(), true);

    mr = new MatchResult(dt, MatchResult.RANKED_1V1, -10, 800);
    assertEquals(mr.isWin(), false);
    assertEquals(mr.getRankPostGame(), 790);
  }

  @Test
  public void testTimeMillis() throws Exception
  {
    DateTime dt = new DateTime("2015-01-02T10:11:12.123");
    assertEquals(dt.getMillisOfSecond(), 123);
    DateTime dt2 = new DateTime("2015-01-02T10:11:12");
    assertEquals(dt2.getMillisOfSecond(), 0);

    MatchResult mr1 = new MatchResult(dt, MatchResult.RANKED_1V1, 0, 0);
    MatchResult mr2 = new MatchResult(dt2, MatchResult.RANKED_1V1, 0, 0);

    assertEquals(mr1, mr2);
  }

  @Test
  public void testEquals() throws Exception
  {
    DateTime dt = new DateTime("2015-01-02T10:11:12");
    MatchResult mr1 = new MatchResult(dt, MatchResult.RANKED_1V1, 10, 800);
    MatchResult mr2 = new MatchResult(dt, MatchResult.RANKED_1V1, 10, 800);
    assertTrue(mr1.equals(mr1));
    assertTrue(mr1.equals(mr2));
    assertEquals(mr1.hashCode(), mr2.hashCode());

    DateTime dt2 = new DateTime("2015-01-02T11:12:13");
    MatchResult mr3 = new MatchResult(dt2, MatchResult.RANKED_1V1, 10, 800);
    MatchResult mr4 = new MatchResult(dt, MatchResult.RANKED_2V2, 10, 800);
    MatchResult mr5 = new MatchResult(dt, MatchResult.RANKED_1V1, 11, 800);
    MatchResult mr6 = new MatchResult(dt, MatchResult.RANKED_1V1, 10, 900);
    Object obj = new Object();
    assertFalse(mr1.equals(obj));
    assertFalse(mr1.equals(null));
    assertFalse(mr1.equals(mr3));
    assertFalse(mr1.equals(mr4));
    assertFalse(mr1.equals(mr5));
    assertFalse(mr1.equals(mr6));
    assertNotEquals(mr1.hashCode(), mr3.hashCode());
  }

  @Test
  public void testToString() throws Exception
  {
    DateTime dt = new DateTime("2015-01-01T10:11:12");
    MatchResult mr = new MatchResult(dt, MatchResult.RANKED_1V1, 10, 100);
    String expected = "2015-01-01 10:11:12,1v1,10,100";

    assertEquals(mr.toString(), expected);
  }

  @Test
  public void testCompare() throws Exception
  {
    DateTime firstDt = new DateTime("2015-01-01T10:11:12");
    DateTime secondDt = new DateTime("2015-01-02T10:11:12");
    MatchResult first = new MatchResult(firstDt, MatchResult.RANKED_1V1, 10, 100);
    MatchResult second = new MatchResult(secondDt, MatchResult.RANKED_1V1, 10, 100);

    assertTrue(first.compareTo(first) == 0);

    assertTrue(first.compareTo(second) < 0);
    assertTrue(second.compareTo(first) > 0);
  }

  @Test
  public void testPlaylistName() throws Exception
  {
    assertEquals(MatchResult.getPlaylistName(MatchResult.RANKED_1V1), "1v1");
    assertEquals(MatchResult.getPlaylistName(MatchResult.RANKED_2V2), "2v2");
    assertEquals(MatchResult.getPlaylistName(MatchResult.RANKED_3V3), "3v3");
    assertEquals(MatchResult.getPlaylistName(MatchResult.SOLO_RANKED_3V3), "solo-3v3");
    assertEquals(MatchResult.getPlaylistName(MatchResult.UNRANKED), "unranked");
    assertEquals(MatchResult.getPlaylistName(1), "1");
    assertEquals(MatchResult.getPlaylistName(-1), "-1");
  }

  @Test
  public void testPlaylist() throws Exception
  {
    assertEquals(MatchResult.getPlaylist("1v1"), MatchResult.RANKED_1V1);
    assertEquals(MatchResult.getPlaylist("Ranked 1v1"), MatchResult.RANKED_1V1);

    assertEquals(MatchResult.getPlaylist("2v2"), MatchResult.RANKED_2V2);
    assertEquals(MatchResult.getPlaylist("Ranked 2v2"), MatchResult.RANKED_2V2);

    assertEquals(MatchResult.getPlaylist("3v3"), MatchResult.RANKED_3V3);
    assertEquals(MatchResult.getPlaylist("Ranked 3v3"), MatchResult.RANKED_3V3);

    assertEquals(MatchResult.getPlaylist("solo-3v3"), MatchResult.SOLO_RANKED_3V3);
    assertEquals(MatchResult.getPlaylist("Solo Ranked 3v3"), MatchResult.SOLO_RANKED_3V3);

    assertEquals(MatchResult.getPlaylist("unranked"), MatchResult.UNRANKED);

    assertEquals(MatchResult.getPlaylist("50"), 50);
    assertEquals(MatchResult.getPlaylist("wtf"), -1);
    assertEquals(MatchResult.getPlaylist(null), -1);
  }

  @Test
  public void testIsRankedPlayList()
  {
    assertTrue(MatchResult.isValidPlayList(MatchResult.RANKED_1V1));
    assertTrue(MatchResult.isValidPlayList(MatchResult.RANKED_2V2));
    assertTrue(MatchResult.isValidPlayList(MatchResult.SOLO_RANKED_3V3));
    assertTrue(MatchResult.isValidPlayList(MatchResult.RANKED_3V3));
    assertTrue(MatchResult.isValidPlayList(MatchResult.UNRANKED));

    assertFalse(MatchResult.isValidPlayList(9));
    assertFalse(MatchResult.isValidPlayList(-1));
    assertFalse(MatchResult.isValidPlayList(15));
  }
}
