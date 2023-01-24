/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.activity.service;

import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.injectDependencies;

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
           log.error(PSExceptionUtils.getMessageForLog(e));
           log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
