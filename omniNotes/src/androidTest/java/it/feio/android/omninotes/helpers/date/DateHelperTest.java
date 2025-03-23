package it.feio.android.omninotes.helpers.date;

import android.content.Context;
import android.text.format.DateUtils;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Locale;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DateHelperTest {

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testGetSortableDate() {
    String date = DateHelper.getSortableDate();
    assertEquals(19, date.length()); // yyyyMMdd_HHmmss_SSS
  }

  @Test
  public void testOnDateSet() {
    String formattedDate = DateHelper.onDateSet(2023, 9, 15, "yyyy-MM-dd");
    assertEquals("2023-10-15", formattedDate);
  }

  @Test
  public void testOnTimeSet() {
    String formattedTime = DateHelper.onTimeSet(14, 30, "HH:mm");
    assertEquals("14:30", formattedTime);
  }

  @Test
  public void testGetDateTimeShort() {
    long timestamp = System.currentTimeMillis();
    String formattedDateTime = DateHelper.getDateTimeShort(context, timestamp);
    String expected = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE) + " " + DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);
    assertEquals(expected, formattedDateTime);
  }

  @Test
  public void testGetTimeShort() {
    long timestamp = System.currentTimeMillis();
    String formattedTime = DateHelper.getTimeShort(context, timestamp);
    String expected = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);
    assertEquals(expected, formattedTime);
  }

  @Test
  public void testFormatShortTime() {
    long timeInMillis = 123456;
    String formattedTime = DateHelper.formatShortTime(context, timeInMillis);
    assertEquals("2:03", formattedTime);
  }

  @Test
  public void testGetFormattedDatePrettified() {
    long timestamp = System.currentTimeMillis();
    String formattedDate = DateHelper.getFormattedDate(timestamp, true);
    // Assuming prettyTime returns a non-null string
    assertEquals(it.feio.android.omninotes.utils.date.DateUtils.prettyTime(timestamp), formattedDate);
  }

  @Test
  public void testGetFormattedDateNotPrettified() {
    long timestamp = System.currentTimeMillis();
    String formattedDate = DateHelper.getFormattedDate(timestamp, false);
    String expected = DateHelper.getDateTimeShort(context, timestamp);
    assertEquals(expected, formattedDate);
  }
}