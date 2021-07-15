/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.activity.service;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.injectDependencies;

import com.percussion.util.PSStopwatch;
import com.percussion.utils.testing.IntegrationTest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSActivityServiceTest extends ServletTestCase
{

    private static final Logger log = LogManager.getLogger(PSActivityServiceTest.class);
    private boolean hasStarted = false;
    private IPSActivityService activityService;
    

    @SuppressWarnings("unchecked")
    public void testNewContentActivities() throws Exception
    {
        List<Date> dates = new ArrayList<Date>();
        dates.add(new Date());
        dates.add(new Date());
        List<Integer> counts = activityService.findNewContentActivities(Collections.EMPTY_LIST, dates);
        assertTrue(counts.size() == 1);
    }
    
    public void testPerformance() throws Exception
    {
        String path = "//Sites/EnterpriseInvestments";
        Collection<Integer> ids = activityService.findItemIdsByPath(path, null);
        
        Date beginDate = getDate(2008, 3, 24, 0, 0, 0); // 2008-3-24 00:00:00
        List<Date> dates = new ArrayList<Date>();
        dates.add(beginDate);
        dates.add(new Date());
        PSStopwatch sw = new PSStopwatch();
        sw.start();
        activityService.findNewContentActivities(ids, dates);
        sw.stop();
        System.out.println("findNewContentActivities('" + path + "'): " + sw.toString());
        
        sw.start();
        activityService.findNumberContentActivities(ids, dates, "Public", null);
        sw.stop();
        System.out.println("findNumberContentActivities('" + path + "'): " + sw.toString());
        
        sw.start();
        activityService.findPublishedItems(ids, dates);
        sw.stop();
        System.out.println("findPublishedItems(ids, dates)('" + path + "'): " + sw.toString());
        
        sw.start();
        activityService.findPublishedItems(ids);
        sw.stop();
        System.out.println("findPublishedItems(ids)('" + path + "'): " + sw.toString());
    }
    
    private Date getDate(int year, int month, int date, int hour, int minute, int second)
    {
       Calendar cal = Calendar.getInstance();
       cal.clear();
       cal.set(year, month - 1, date, hour, minute, second);
       return cal.getTime();
    }
    
    @SuppressWarnings("unchecked")
    public void testNewContentActivities_Negative() throws Exception
    {
        List<Date> dates = new ArrayList<Date>();
        dates.add(new Date());
        dates.add(new Date());
        
        // negative test
        try
        {
            activityService.findNewContentActivities(Collections.EMPTY_LIST, null);
            assertTrue(false);
        }
        catch (Exception e)
        {
            // ignore
        }
        // negative test
        try
        {
            activityService.findNewContentActivities(Collections.EMPTY_LIST, Collections.singletonList(new Date()));
            assertTrue(false);
        }
        catch (Exception e)
        {
            // ignore
        }
        try
        {
            activityService.findNewContentActivities(null, dates);
            assertTrue(false);
        }
        catch (Exception e)
        {
            // ignore
        }
    }
    
    protected void setUp() 
    {
       try
       {
          super.setUp();
          if (!hasStarted)
          {
              injectDependencies(this);
              hasStarted = true;
          }
       }
       catch (Exception e)
       {
           log.error(e.getMessage());
           log.debug(e.getMessage(), e);
       }
    }
    
    protected void tearDown() 
    {
    }
    
    public void setActivityService(IPSActivityService activityService)
    {
        this.activityService = activityService;
    }
}
