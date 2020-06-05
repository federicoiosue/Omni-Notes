/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.utils.date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DateUtils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class DateUtilsTest {

  @Test
  public void prettyTime () {
    long now = Calendar.getInstance().getTimeInMillis();

    String prettyTime = DateUtils.prettyTime(now, Locale.ENGLISH);
    Assert.assertEquals("moments ago", prettyTime.toLowerCase());

    prettyTime = DateUtils.prettyTime(now + 10 * 60 * 1000, Locale.ENGLISH);
    Assert.assertEquals("10 minutes from now", prettyTime.toLowerCase());

    prettyTime = DateUtils.prettyTime(now + 24 * 60 * 60 * 1000, Locale.ITALIAN);
    Assert.assertEquals("fra 24 ore", prettyTime.toLowerCase());

    prettyTime = DateUtils.prettyTime(now + 25 * 60 * 60 * 1000, Locale.ITALIAN);
    Assert.assertEquals("fra 1 giorno", prettyTime.toLowerCase());

    prettyTime = DateUtils.prettyTime(null, Locale.JAPANESE);
    Assert.assertNotNull(prettyTime.toLowerCase());
    Assert.assertEquals(0, prettyTime.toLowerCase().length());
  }

  @Test
  public void getPresetReminder () {
    long mockedNextMinute = 1497315847L;
    Long testReminder = null;
    PowerMockito.stub(PowerMockito.method(DateUtils.class, "getNextMinute")).toReturn(mockedNextMinute);
    assertEquals(mockedNextMinute, DateUtils.getPresetReminder(testReminder));
  }

  @Test
  public void isFuture () {
    String nextMinute = String.valueOf(Calendar.getInstance().getTimeInMillis() + 60000);
    String previousMinute = String.valueOf(Calendar.getInstance().getTimeInMillis() - 60000);
    assertTrue(DateUtils.isFuture(nextMinute));
    assertFalse(DateUtils.isFuture(previousMinute));
  }

  @Test
  public void isFutureManagesNullValues () {
    Long longValue = null;
    assertFalse(DateUtils.isFuture(longValue));
  }

  @Test
  public void isSameDay () {
    long today = Calendar.getInstance().getTimeInMillis();
    long tomorrow = today + (1000 * 60 * 60 * 24);
    assertTrue(DateUtils.isSameDay(today, today));
    assertFalse(DateUtils.isSameDay(today, tomorrow));
  }

  @Test
  public void getString_dateSignature() throws ParseException {
    String expectedDate = "15/05/2012";
    String format = "dd/MM/yyyy";
    Date date = new SimpleDateFormat(format).parse(expectedDate);
    assertEquals(expectedDate, DateUtils.getString(date, format));
  }

  @Test
  public void getString_longSignature() {
    String expectedDateAsString = "16/03/2020";
    long expectedDate = 1584371565000L;
    String format = "dd/MM/yyyy";
    assertEquals(expectedDateAsString, DateUtils.getString(expectedDate, format));
  }

  @Test
  public void getDateFromStringTest(){
    String date="2020/03/12 15:15:15";
    String dateformat="yyyy/MM/dd HH:mm:ss";
    Calendar calendar=DateUtils.getDateFromString(date,dateformat);
    assertEquals(calendar.get(Calendar.YEAR),2020);
    assertEquals(calendar.get(Calendar.MONTH),2);
    assertEquals(calendar.get(Calendar.DATE),12);
    assertEquals(calendar.get(Calendar.HOUR_OF_DAY),15);
    assertEquals(calendar.get(Calendar.MINUTE),15);
    assertEquals(calendar.get(Calendar.SECOND),15);
  }

  @Test
  public void getLongFromDateTimeTest(){
    String date="2020-03-12";
    String dateformat="yyyy-MM-dd";
    String time="15:15:15";
    String timeformat="HH:mm:ss";
    Calendar calendar=DateUtils.getLongFromDateTime(date,dateformat,time,timeformat);
    assertEquals(calendar.get(Calendar.YEAR),2020);
    assertEquals(calendar.get(Calendar.MONTH),2);
    assertEquals(calendar.get(Calendar.DATE),12);
    assertEquals(calendar.get(Calendar.HOUR_OF_DAY),15);
    assertEquals(calendar.get(Calendar.MINUTE),15);
    assertEquals(calendar.get(Calendar.SECOND),0);
  }
}
