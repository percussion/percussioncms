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
package com.percussion.services.system;

import com.percussion.data.PSIdGenerator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.utils.guid.IPSGuid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link IPSSystemService}
 */
@Category(IntegrationTest.class)
public class PSSystemServiceTest extends ServletTestCase
{

   private static final Logger log = LogManager.getLogger(PSSystemServiceTest.class);

   /**
    * Test loading and saving configurations
    * 
    * @throws Exception if the test fails.
    */
   public void testLoadConfiguration() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      byte[] data = "some test content...".getBytes();
      
      for (PSConfigurationTypes type : PSConfigurationTypes.values())
      {
         File file = svc.getConfigurationFile(type);
         assertTrue(file.exists());
         File backup = backupFile(file);
         try
         {
            PSMimeContentAdapter content; 
            content = svc.loadConfiguration(type);
            validateContent(file, content);
            content.setContent(new ByteArrayInputStream(data));
            content.setContentLength(data.length);
            svc.saveConfiguration(content);
            validateContent(file, data);
            content = svc.loadConfiguration(type);
            validateContent(file, content);
         }
         finally
         {
            restoreBackup(backup, file);
         }
      }
   }

   /**
    * Test loading content status history. Assumes database is available and
    * has content status entries for a fixed content id (currently 471).
    */
   public void testLoadContentStatusHistory()
   {
      int contentId = 471;
      IPSGuid id = new PSLegacyGuid(contentId, 1);
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      List<PSContentStatusHistory> histList = svc.findContentStatusHistory(id);
      assertFalse(histList.isEmpty());
      
      PSContentStatusHistory lastHist = svc.findLastCheckInOut(id);
      assertTrue(lastHist != null);
      
      // find the last check in/out from "histList"
      PSContentStatusHistory lastCheckInOut = null;
      for (int i=0; i<histList.size(); i++)
      {
         if ("CheckOut".equalsIgnoreCase(histList.get(i).getTransitionLabel()))
         {
            if (lastCheckInOut == null || lastCheckInOut.getId() < histList.get(i).getId())
            {
               lastCheckInOut = histList.get(i);
            }
         }
      }
      assertTrue(lastHist.getId() == lastCheckInOut.getId());
      
   }

   List<PSContentStatusHistory> m_insertedHistories;
   
   protected void setUp() 
   {
      try
      {
         super.setUp();
         m_insertedHistories = new ArrayList<PSContentStatusHistory>();
         
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
   
   private void createLargeHistory() throws Exception
   {
      int itemCount = 6000;
      String[] states = {"Draft", "Review", "Pending", "Live", "Quick Edit", "Pending", "Live", "Quick Edit", "Archive"};
      for (int i = 0; i < itemCount ; i++)
      {
         int contentId = PSIdGenerator.getNextId("CONTENT");
         int revision;
         for (int j = 0; j < states.length; j++)
         {
            if (j < 4)
               revision = 1;
            else if (j < 7)
               revision = 2;
            else
               revision = 3;
            
            createNewContentStatusHistory(contentId, new Date(), states[j], j + 1, "Unit-test", revision);
         }
      }
           
   }
   
   private PSContentStatusHistory createNewContentStatusHistory(int contentId,
         Date date, String stateName, int stateId, String transitionName, int revision) 
         throws CloneNotSupportedException
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      IPSGuid id = new PSLegacyGuid(contentId, 1);
      List<PSContentStatusHistory> entries = svc.findContentStatusHistory(id);
      PSContentStatusHistory hist = new PSContentStatusHistory();
      hist.setId(-1L);
      hist.setActor("Admin");
      hist.setContentId(contentId);
      hist.setEventTime(date);
      hist.setIsValidValue("N");
      hist.setLastModifiedDate(date);
      hist.setLastModifierName("Admin");
      hist.setRevision(revision);
      hist.setRoleName("Admin");
      hist.setSessionId("0");
      hist.setStateId(stateId);
      hist.setStateName(stateName);
      hist.setTitle("test");
      hist.setTransitionId(0);
      hist.setWorkflowId(5);
      hist.setTransitionLabel("test");
      
     
      svc.saveContentStatusHistory(hist);
      
      return hist;
   }
   

   protected void tearDown() 
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      try
      {
         for (PSContentStatusHistory h : m_insertedHistories)
         {
            try
            {
               svc.deleteContentStatusHistory(h);
            }
            catch (Exception e)
            {
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);
            }
         }
         super.tearDown();
         
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
   
   private PSContentStatusHistory createContentStatusHistory(int contentId,
         Date date, String stateName, int stateId, String transitionName) 
         throws CloneNotSupportedException
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      IPSGuid id = new PSLegacyGuid(contentId, 1);
      List<PSContentStatusHistory> entries = svc.findContentStatusHistory(id);
      PSContentStatusHistory cloned =  entries.get(entries.size()-1).clone();
      
      cloned.setId(-1L);
      cloned.setEventTime(date);
      cloned.setLastModifiedDate(date);
      cloned.setStateName(stateName);
      cloned.setStateId(stateId);
      if (transitionName != null)
         cloned.setTransitionLabel(transitionName);
      
      svc.saveContentStatusHistory(cloned);
      m_insertedHistories.add(cloned);
      
      return cloned;
   }
   
   /* only to be used for debugging, need to add call to createLargeHistory() into setUp() for a single execution.
   public void testLargeQueries() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      Collection<Integer> contentIds = new ArrayList<Integer>();
      int contentId = 2666;
      while (contentId < 3700)
      {
         contentIds.add(contentId++);
      }
      
      svc.findNumberContentActivities(contentIds, new Date("1/1/2012"), new Date(), "Live", null);
      svc.findNewContentActivities(contentIds, new Date("1/1/2012"), new Date(), "Live");
      svc.findPublishedItems(contentIds, "Public", "Archive");      
      svc.findPublishedItems(contentIds, new Date("1/1/2012"), new Date(), "Public", "Archive");
      svc.findPublishedItems(contentIds, new Date(), new Date(), "Public", "Archive");
      svc.findPageIdsContentActivities(contentIds, new Date(), new Date(), "Public", null);
   }
   */
   
   @SuppressWarnings("unchecked")
   public void testContentActivities() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();

      List<Integer> contentIds = Collections.singletonList(new Integer(1));
      int count = svc.findNumberContentActivities(contentIds, new Date(), new Date(), "Live", "Live");
      assertTrue(count == 0);
      
      count = svc.findNewContentActivities(contentIds, new Date(), new Date(), "Live");
      assertTrue(count == 0);      
    
      count = svc.findPublishedItems(contentIds, new Date(), new Date(), "Live", "Archive" );
      assertTrue(count == 0);         
   }

   @SuppressWarnings("unchecked")
   public void testFindPublishedItemsWithDates() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();

      List<Integer> contentIds = Collections.singletonList(new Integer(375));

      // No content are created between 2006-3-24 00:00:00 and 2007-3-24 00:00:00 
      Date beginDate = getDate(2006, 3, 24, 0, 0, 0); // 2006-3-24 00:00:00
      Date endDate   = getDate(2007, 3, 24, 0, 0, 0); // 2007-3-24 00:00:00
      int count = svc.findPublishedItems(contentIds, beginDate, endDate, "Public", "Archive");
      assertTrue(count == 0);
      
      // No content are transitioned to "Public" on 2008-3-24 00:00:00, but not include 2008-3-24 00:00:00
      endDate = getDate(2008, 3, 24, 0, 0, 0); // 2008-3-24 00:00:00
      count = svc.findPublishedItems(contentIds, beginDate, endDate, "Public", "Archive");
      assertTrue(count == 0);

      // Items are transitioned to "Public" on 2008-3-24 00:00:00
      beginDate = getDate(2008, 3, 24, 0, 0, 0); // 2008-3-24 00:00:00
      count = svc.findPublishedItems(contentIds, beginDate, new Date(), "Public", "Archive");
      assertTrue(count == 1);
      
      // Items are transitioned to "Public" before now and never transitioned to "Archive"
      count = svc.findPublishedItems(contentIds, new Date(), new Date(), "Public", "Archive");
      assertTrue(count == 1);
      
      // After transition to public on 2008-3-24 00:00:00, then transition to "Archive" on 2008-3-24 00:00:01 
      Date archiveDate = getDate(2008, 3, 24, 0, 0, 1); 
      createContentStatusHistory(375, archiveDate, "Archive", 7, "Take Down");
      
      endDate = getDate(2008, 3, 24, 0, 0, 2);
      count = svc.findPublishedItems(contentIds, beginDate, endDate, "Public", "Archive");
      assertTrue(count == 0);
      
      // After transition to public on 2008-3-24 00:00:00, transition to "Archive" on 2008-3-24 00:00:01
      // then transition to public again on 
      endDate = getDate(2008, 4, 4, 0, 0, 1);
      count = svc.findPublishedItems(contentIds, beginDate, endDate, "Public", "Archive");
      assertTrue(count == 1);
   }
   
   @SuppressWarnings("unchecked")
   public void testFindPublishedItems() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();

      List<Integer> contentIds = Collections.singletonList(new Integer(375));

      // Items are transitioned to "Public" on 2008-4-6 00:00:00
      Collection<Long> items = svc.findPublishedItems(contentIds, "Public", "Archive");
      assertTrue(items.size() == 1);
      
      // After transition to public on 2008-4-6 00:00:00, then transition to "Archive" on 2008-4-6 00:00:01 
      Date archiveDate = getDate(2008, 4, 6, 0, 0, 1); 
      createContentStatusHistory(375, archiveDate, "Archive", 7, "Take Down");
      
      // Items are no longer published
      items = svc.findPublishedItems(contentIds, "Public", "Archive");
      assertTrue(items.size() == 0);
   }
   
   private Date getDate(int year, int month, int date, int hour, int minute, int second)
   {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(year, month - 1, date, hour, minute, second);
      return cal.getTime();
   }
   
   @SuppressWarnings("unchecked")
   public void testFindNewContentActivities() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      
      // Not all "Public" transitions are done right after 2008-3-24 00:00:01
      Date beginDate =  getDate(2008, 3, 24, 0, 0, 1); // 2008-3-24 00:00:01
      List<Integer> contentIds = Collections.singletonList(new Integer(375));
      int count = svc.findNewContentActivities(contentIds, beginDate, new Date(), "Public");
      assertTrue(count == 0);

      // The items are created before 2008-3-24 00:00:00, but NOT INCLUDE 2008-3-24 00:00:00 
      beginDate = getDate(2006, 3, 24, 0, 0, 0);  // 2006-3-24 00:00:00
      Date endDate = getDate(2008, 3, 24, 0, 0, 0);  // 2008-3-24 00:00:00
      
      count = svc.findNewContentActivities(contentIds, beginDate, endDate, "Public");
      assertTrue(count == 0);

      // The items are created and all transitions are done after 2006-3-24 00:00:00
      beginDate = getDate(2006, 3, 24, 0, 0, 0);  // 2006-3-24 00:00:00
      count = svc.findNewContentActivities(contentIds, beginDate, new Date(), "Public");
      assertTrue(count == 1);

      // All "Public" transitions are done right after 2008-3-24 00:00:00
      beginDate = getDate(2008, 3, 24, 0, 0, 0);  // 2008-3-24 00:00:00
      count = svc.findNewContentActivities(contentIds, beginDate, new Date(), "Public");
      assertTrue(count == 1);
   }
   
   @SuppressWarnings("unchecked")
   public void testFindNumberContentActivities() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();

      List<Integer> contentIds = Collections.singletonList(new Integer(375));
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -2);
      Date beginDate = cal.getTime();
      int origCount = svc.findNumberContentActivities(contentIds, beginDate, new Date(), "Public", null);
      
      Calendar cal2 = Calendar.getInstance();
      cal2.add(Calendar.DATE, -1);
      Date date1Ago = cal2.getTime();
      
      createContentStatusHistory(375, date1Ago, "Public", 5, "Return to Public");
      
      int count = svc.findNumberContentActivities(contentIds, beginDate, new Date(), "Public", null);
      
      assertTrue(count == origCount + 1);
   }


   public void testContentStatusHistoryCRUD() throws Exception
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      
      IPSGuid id = new PSLegacyGuid(375, 3);
      List<PSContentStatusHistory> entries = svc.findContentStatusHistory(id);
      PSContentStatusHistory cloned =  entries.get(entries.size()-1).clone();
      cloned.setId(-1L);
      assertTrue(cloned.getId() == -1L);
      
      // add a new row
      svc.saveContentStatusHistory(cloned);
      assertTrue(cloned.getId() != -1L);
      List<PSContentStatusHistory> entries2 = svc.findContentStatusHistory(id);
      assertTrue(entries.size() == entries2.size() -1);
      
      svc.deleteContentStatusHistory(cloned);
      entries2 = svc.findContentStatusHistory(id);
      assertTrue(entries.size() == entries2.size());
   }
   
   /**
    * Validates that the supplied file matches the loaded content.
    * 
    * @param file The file to match against, assumed not <code>null</code> and
    * to exist.
    * @param content The content to validate, assumed not <code>null</code>.
    * 
    * @throws IOException if there are any errors.
    */
   private void validateContent(File file, PSMimeContentAdapter content) 
      throws IOException
   {
      // copy content
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtils.copy(content.getContent(), out);
      byte[] data = out.toByteArray();
      assertEquals(content.getContentLength(), data.length);
      assertEquals(content.getContentLength(), file.length());
      validateContent(file, data);
   }

   /**
    * Validates the contents of the file and the supplied data are equal.
    * 
    * @param file The file to check, assumed not <code>null</code> and to exist.
    * @param data The data to match, assumed not <code>null</code>.
    * 
    * @throws IOException If there are any errors.
    */
   private void validateContent(File file, byte[] data) throws IOException
   {
      FileInputStream in = null;
      try
      {
         in = new FileInputStream(file);
         assertTrue(IOUtils.contentEquals(in, 
            new ByteArrayInputStream(data)));
      }
      finally
      {
         IOUtils.closeQuietly(in);
      }      
   }
   
   /**
    * Copies the source file to a backup temp file, which is set for 
    * deleteOnExit.
    * 
    * @param file The file to copy, assumed not <code>null</code>.
    * 
    * @return The backup file, never <code>null</code>.
    * 
    * @throws IOException if the copy fails.
    */
   private File backupFile(File file) throws IOException
   {
      File backup = file.createTempFile("cfgTest_", ".bak");
      backup.deleteOnExit();
      FileUtils.copyFile(file, backup);
      
      return backup;
   }
   
   /**
    * Copies the backup file over the original file and deletes the backup.
    * 
    * @param backup The backup file, assumed not <code>null</code>.
    * @param file The original file, assumed not <code>null</code>.
    * 
    * @throws IOException if the copy or delete fails.
    */
   private void restoreBackup(File backup, File file) throws IOException
   {
      FileUtils.copyFile(backup, file);
      backup.delete();
   }
}

