/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.fastforward.calendar;

import com.percussion.services.assembly.IPSAssemblyResult;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/*
 * Test class for com.percussion.fastforward.calendar.PSCalendarMonthModel
 */
public class PSCalendarMonthModelTest
{

   private static final Logger log = LogManager.getLogger(PSCalendarMonthModelTest.class);

    Mockery context = new Mockery();

    @Before
   public void setUp() throws Exception
   {
      m_info = new PSCalendarMonthModel();
   }

   /*
    * Test method for 'assign()' with illegal args
    */
   @Test
   public void testIllegalAssign() throws Exception
   {
      try
      {
         m_info.assign((Date) null);
         fail("invalid args must throw IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         m_info.assign((Calendar) null);
         fail("invalid args must throw IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         m_info.assign(null, null);
         fail("invalid args must throw IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }

      try
      {
         m_info.assign("", "");
         fail("invalid args must throw IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   /*
    * Test method for 'assign()'
    */
   @Test
   public void testAssign() throws Exception
   {
      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");
      assertNotNull(m_info.assign(df.parse("2006-04-05")));

      assertNotNull(m_info.assign("yyyy-MM-dd", "2006-04-05"));

      assertNotNull(m_info.assign(Calendar.getInstance()));

      // bad formats are silently ignored
      assertNotNull(m_info.assign("yyyy-MM-dd", "April 5th 2006"));
   }

   /*
    * Test method for 'lastDay()'
    */
   @Test
   public void testLastDay() throws Exception
   {
      // without calling assign, should use current date
      assert (m_info.getLastDay() > 0);
      assert (m_info.getLastDay() >= 28);
      assert (m_info.getLastDay() <= 31);

      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");

      m_info.assign(df.parse("2006-04-05"));
      assertEquals(30, m_info.getLastDay());

      m_info.assign(df.parse("2006-12-05"));
      assertEquals(31, m_info.getLastDay());

      m_info.assign(df.parse("2006-02-05"));
      assertEquals(28, m_info.getLastDay());

      // leap year
      m_info.assign(df.parse("2004-02-05"));
      assertEquals(29, m_info.getLastDay());

      // month is zero-based (9 = october)
      m_info.assign(new GregorianCalendar(2003, 9, 7));
      assertEquals(31, m_info.getLastDay());

   }

   /*
    * Test method for 'weeks()'
    */
   @Test
   public void testWeeks() throws Exception
   {
      // without calling assign, should use current date
      assert (m_info.getWeeks() > 0);

      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");

      m_info.assign(df.parse("2006-04-05"));
      assertEquals(6, m_info.getWeeks());

      m_info.assign(df.parse("2006-02-05"));
      assertEquals(5, m_info.getWeeks());

      m_info.assign(df.parse("1998-02-05"));
      assertEquals(4, m_info.getWeeks());
   }

   /*
    * Test method for 'firstDayOfWeek()' and 'lastDayOfWeek()'
    */
   @Test
   public void testDaysOfWeek() throws Exception
   {
      // without calling assign, should use current date
      assert (m_info.getFirstDayOfWeek() >= 1);
      assert (m_info.getFirstDayOfWeek() <= 7);
      assert (m_info.getLastDayOfWeek() >= 1);
      assert (m_info.getLastDayOfWeek() <= 7);

      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");

      m_info.assign(df.parse("2006-04-05"));
      assertEquals(Calendar.SATURDAY, m_info.getFirstDayOfWeek());
      assertEquals(Calendar.SUNDAY, m_info.getLastDayOfWeek());

      m_info.assign(df.parse("2006-05-05"));
      assertEquals(Calendar.MONDAY, m_info.getFirstDayOfWeek());
      assertEquals(Calendar.WEDNESDAY, m_info.getLastDayOfWeek());

      // month is zero-based (9 = october)
      m_info.assign(new GregorianCalendar(2003, 9, 7));
      assertEquals(Calendar.WEDNESDAY, m_info.getFirstDayOfWeek());
      assertEquals(Calendar.FRIDAY, m_info.getLastDayOfWeek());

   }

   /*
    * Test method for 'start()' and 'end()'
    */
   @Test
   public void testStartEnd() throws Exception
   {
      // without calling assign, should use current date
      assertNotNull(m_info.getStartDate());
      assertNotNull(m_info.getEndDate());

      Calendar c = Calendar.getInstance();
      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");

      m_info.assign(df.parse("2006-04-05T09:52:37.013"));
      c.setTime(df.parse("2006-04-01T00:00:00.000"));
      assertEquals(c, m_info.getStartDate());
      c.setTime(df.parse("2006-04-30T23:59:59.999"));
      assertEquals(c, m_info.getEndDate());

      // leap year
      m_info.assign(df.parse("2004-02-05T09:52:37.013"));
      c.setTime(df.parse("2004-02-01T00:00:00.000"));
      assertEquals(c, m_info.getStartDate());
      c.setTime(df.parse("2004-02-29T23:59:59.999"));
      assertEquals(c, m_info.getEndDate());
   }

   /**
    * Tests calling methods through Velocity templates
    */
   @Test
   public void testVelocity() throws Exception
   {
      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");
      m_info.assign(df.parse("2006-04-05"));
      VelocityContext ctx = getVelocityContext();
      doVelocityTest(ctx, "$rx.month.LastDay", "30");
      doVelocityTest(ctx, "$rx.month.FirstDayOfWeek", String
            .valueOf(Calendar.SATURDAY));
      doVelocityTest(ctx, "$rx.month.LastDayOfWeek", String
            .valueOf(Calendar.SUNDAY));
      doVelocityTest(ctx, "$rx.month.Weeks", "6");
      doVelocityTest(ctx, "$rx.month.Start", "2006-04-01");
      doVelocityTest(ctx, "$rx.month.End", "2006-04-30");
     
      doVelocityTest(ctx, "$rx.month.StartDate.Time.Time",
            "1143867600000");
      doVelocityTest(ctx, "$rx.month.EndDate.Time.Time",
            "1146455999999");

      doVelocityTest(ctx, "$!rx.month.getEvents(1)", "");
      doVelocityTest(ctx, "$!rx.month.setEvents()", "");
   }
   @Test
   public void testEmptyEvents()
   {
      assertNull(m_info.getEvents(1));
      assertNull(m_info.getEvents(2));
      assertNull(m_info.getEvents(15));

      m_info.setEvents(null);
      assertNull(m_info.getEvents(1));
      assertNull(m_info.getEvents(2));
      assertNull(m_info.getEvents(15));

      m_info.setEvents(new ArrayList<>());
      assertNull(m_info.getEvents(1));
      assertNull(m_info.getEvents(2));
      assertNull(m_info.getEvents(15));
   }

   /**
    * Tests assigning and retrieving events through 'setEvents' and
    * 'getEventsForDay'
    */
   @Test
   @Ignore //TODO: Fix me - test is failing
   public void testEvents() throws Exception
   {
      FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");
      Collection eventsForDay;


      IPSAssemblyResult mockResult1 = context.mock(IPSAssemblyResult.class, "result1");
       IPSAssemblyResult mockResult2 = context.mock(IPSAssemblyResult.class, "result2");
       Node mockNode1 = context.mock(Node.class, "node1");
       Node mockNode2 = context.mock(Node.class, "node2");
       Property mockProperty1 = context.mock(Property.class, "property1");
       Property mockProperty2 = context.mock(Property.class, "property2");

      List<IPSAssemblyResult> events = new ArrayList<>(2);
      events.add((IPSAssemblyResult) mockResult1);
      events.add((IPSAssemblyResult) mockResult2);

      Calendar feb_1_2006 = Calendar.getInstance();
      feb_1_2006.setTime(df.parse("2006-02-01"));
      Calendar mar_2_2006 = Calendar.getInstance();
      mar_2_2006.setTime(df.parse("2006-03-02"));

      // #1 events lack nodes, so cannot be processed
       context.checking(new Expectations() {{
           oneOf(mockResult1).getNode();
           oneOf(mockResult1).getId();
           oneOf(mockResult2.getNode());
           oneOf(mockResult2.getId());
       }});

      assertNull(m_info.getEvents(1));
      context.assertIsSatisfied();

       context.checking(new Expectations() {{
           oneOf(mockResult1).getNode(); will(returnValue(mockNode1));
           oneOf(mockResult1).getId();
           oneOf(mockNode1).getProperty(with(equal(PSCalendarMonthModel.EVENT_START_PROP_NAME)));
           oneOf(mockResult2.getNode());
           oneOf(mockResult2.getId());
       }});



      assertNull(m_info.getEvents(1));
      context.assertIsSatisfied();


       context.checking(new Expectations() {{
           oneOf(mockResult1).getNode(); will(returnValue(mockNode1));
           oneOf(mockNode1).getProperty(with(equal(PSCalendarMonthModel.EVENT_START_PROP_NAME))); will(returnValue(mockProperty1));
           oneOf(mockProperty1).getDate(); will(returnValue(feb_1_2006));
           oneOf(mockResult2.getNode());
           oneOf(mockResult2.getId());
       }});


      m_info.assign(df.parse("2006-02-05"));
      m_info.setEvents(events);
      eventsForDay = m_info.getEvents(1);
      assertNotNull(eventsForDay);
      assertEquals(1, eventsForDay.size());
      context.assertIsSatisfied();

      // #4 reassign calendar clears events
      m_info.assign(df.parse("2006-02-05"));
      assertNull(m_info.getEvents(1));

       context.checking(new Expectations() {{
           oneOf(mockResult1).getNode(); will(returnValue(mockNode1)); will(returnValue(mockNode1));
           oneOf(mockResult1.getId());

           oneOf(mockNode1).getProperty(with(equal(PSCalendarMonthModel.EVENT_START_PROP_NAME))); will(returnValue(mockProperty1));
           oneOf(mockProperty1).getDate(); will(returnValue(feb_1_2006));
           oneOf(mockResult2.getNode());
           oneOf(mockResult2.getId());
       }});



      m_info.assign(df.parse("2006-03-05"));
      m_info.setEvents(events);
      assertNull(m_info.getEvents(1));
      context.assertIsSatisfied();


       context.checking(new Expectations() {{
           oneOf(mockResult1).getNode(); will(returnValue(mockNode1)); will(returnValue(mockNode1));
           oneOf(mockResult1.getId());

           oneOf(mockNode1).getProperty(with(equal(PSCalendarMonthModel.EVENT_START_PROP_NAME))); will(returnValue(mockProperty1));
           oneOf(mockProperty1).getDate(); will(returnValue(feb_1_2006));
           oneOf(mockResult2.getNode()); will(returnValue(mockNode2));
           oneOf(mockProperty2).getDate(); will(returnValue(mar_2_2006));
       }});



       // #6 one event in month, one event out


      m_info.setEvents(events);
      assertNull(m_info.getEvents(1));
      eventsForDay = m_info.getEvents(2);
      assertNotNull(eventsForDay);
      assertEquals(1, eventsForDay.size());
      context.assertIsSatisfied();


       context.checking(new Expectations() {{
           oneOf(mockResult1).getNode(); will(returnValue(mockNode1)); will(returnValue(mockNode1));
           oneOf(mockResult1.getId());

           oneOf(mockNode1).getProperty(with(equal(PSCalendarMonthModel.EVENT_START_PROP_NAME))); will(returnValue(mockProperty1));
           oneOf(mockProperty1).getDate(); will(returnValue(mar_2_2006));
           oneOf(mockResult2.getNode()); will(returnValue(mockNode2));
           oneOf(mockNode2).getProperty(with(equal(PSCalendarMonthModel.EVENT_START_PROP_NAME))); will(returnValue(mockProperty2));

           oneOf(mockProperty2).getDate(); will(returnValue(mar_2_2006));
       }});



       // #7 two events on same day should be returned in a list


      m_info.setEvents(events);
      assertNull(m_info.getEvents(1));
      eventsForDay = m_info.getEvents(2);
      assertNotNull(eventsForDay);
      assertEquals(2, eventsForDay.size());
      context.assertIsSatisfied();

   }

   private void doVelocityTest(VelocityContext ctx, String inputtemplate,
         String expectedoutput) throws ParseErrorException,
         MethodInvocationException, ResourceNotFoundException, IOException
   {
      StringWriter out = new StringWriter();
      ms_engine.evaluate(ctx, out, "Velo", inputtemplate);
      assertEquals(expectedoutput, out.toString());
   }

   private VelocityContext getVelocityContext()
   {
      VelocityContext rval = new VelocityContext();
      Map<String, Object> rx = new HashMap<>();
      rval.put("rx", rx);

      rx.put("month", m_info);
      return rval;
   }

   /**
    * Object under test. Assigned in setUp().
    */
   private PSCalendarMonthModel m_info;

   private static VelocityEngine ms_engine = null;

   static
   {
      ms_engine = new VelocityEngine();
      try
      {
         ms_engine.init();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

}
