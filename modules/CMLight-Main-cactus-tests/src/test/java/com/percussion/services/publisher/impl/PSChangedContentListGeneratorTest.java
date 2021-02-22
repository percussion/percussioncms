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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.publisher.impl;

import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSChangedContentListGeneratorTest extends ServletTestCase
{
   private static final String CONTENT_CHANGE_TYPE_PARAM = "contentChangeType";
   private static final String SITE_ID_PARAM = IPSHtmlParameters.SYS_SITEID;
   private static final String SITEID = "999";
   private PSChangedContentListGenerator gen;
   private IPSContentChangeService changeService;
   

   public void setUp()
   {
      gen = new PSChangedContentListGenerator();
      changeService = PSContentChangeServiceLocator.getContentChangeService();
   }
   
   public void tearDown()
   {
      changeService.deleteChangeEventsForSite(Long.parseLong(SITEID));
   }
   
   public void testGenerate() throws Exception
   {
       
       assertGeneration(null, null, true);
       Map<String, String> params = new HashMap<>();
       assertGeneration(null, null, true);
       
       params.put(SITE_ID_PARAM, null);
       params.put(CONTENT_CHANGE_TYPE_PARAM, "PENDING_LIVE");
       assertGeneration(params, null, true);
       
       params.put(SITE_ID_PARAM, "notanumber");
       params.put(CONTENT_CHANGE_TYPE_PARAM, "PENDING_LIVE");
       assertGeneration(params, null, true);
       
       params.put(SITE_ID_PARAM, SITEID);
       params.put(CONTENT_CHANGE_TYPE_PARAM, null);
       assertGeneration(params, null, true);
       
       params.put(SITE_ID_PARAM, SITEID);
       params.put(CONTENT_CHANGE_TYPE_PARAM, "badType");
       assertGeneration(params, null, true);
       
       List<String> changedIds = new ArrayList<>();
       params.put(SITE_ID_PARAM, SITEID);
       params.put(CONTENT_CHANGE_TYPE_PARAM, "PENDING_LIVE");
       assertGeneration(params, changedIds, false);
       
       changedIds.add(createChange(1, 999, PSContentChangeType.PENDING_LIVE));
       changedIds.add(createChange(2, 999, PSContentChangeType.PENDING_LIVE));
       changedIds.add(createChange(3, 999, PSContentChangeType.PENDING_LIVE));
       assertGeneration(params, changedIds, false);
       
       changedIds.clear();
       changedIds.add(createChange(1, 999, PSContentChangeType.PENDING_STAGED));
       changedIds.add(createChange(2, 999, PSContentChangeType.PENDING_STAGED));
       params.put(CONTENT_CHANGE_TYPE_PARAM, "PENDING_STAGED");
       assertGeneration(params, changedIds, false);
   }

   private String createChange(int contentId, long siteId, PSContentChangeType changeType) throws IPSGenericDao.SaveException {
      PSContentChangeEvent changeEvent = new PSContentChangeEvent();
       changeEvent.setChangeType(changeType);
       changeEvent.setContentId(contentId);
       changeEvent.setSiteId(siteId);
       changeService.contentChanged(changeEvent);
       return String.valueOf(contentId);
   }
   
   private void assertGeneration(Map<String, String> params, List<String> expectedIds, boolean shouldFail) throws Exception
   {
      QueryResult result;
      try
      {
         result = gen.generate(params);
      }
      catch (Exception e)
      {
         assertTrue(shouldFail);
         return;
      }
      
      assertFalse(shouldFail);
      
      RowIterator rows = result.getRows();
      while(rows.hasNext())
      {
         Row row = rows.nextRow();
         Value val = row.getValue(IPSContentPropertyConstants.RX_SYS_CONTENTID);
         assertTrue(NumberUtils.isNumber(val.getString()));
         assertTrue("Unexpected ID: " + val.getLong(), expectedIds.remove(val.getString()));
      }
      
      assertTrue(expectedIds.isEmpty());
      
   }

}
