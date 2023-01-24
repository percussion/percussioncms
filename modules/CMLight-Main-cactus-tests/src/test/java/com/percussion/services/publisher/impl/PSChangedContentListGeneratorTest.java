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
