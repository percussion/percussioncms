/******************************************************************************
 *
 * [ PSContentBrowserTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.content.ui.browse;

import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSItemSummary.ObjectTypeEnum;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.apache.cactus.ServletTestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.experimental.categories.Category;

import java.util.HashMap;

import static com.percussion.content.ui.browse.PSContentBrowser.COLUMN_DESCRIPTION;
import static com.percussion.content.ui.browse.PSContentBrowser.COLUMN_ID;
import static com.percussion.content.ui.browse.PSContentBrowser.COLUMN_NAME;
import static com.percussion.content.ui.browse.PSContentBrowser.COLUMN_TYPE;

/**
 * @author Andriy Palamarchuk
 */
@Category(IntegrationTest.class)
public class PSContentBrowserTest extends ServletTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      PSSecurityFilter.authenticate(request, response, "admin1", "demo");
   }

   public void testSummaryToJsonObject() throws JSONException
   {
      final IPSGuid guid = new PSLegacyGuid(368, -1);
      final PSItemSummary summary = new PSItemSummary();
      final String name = "Name 1";

      summary.setGUID(guid);
      summary.setContentTypeId(311);
      summary.setName(name);
      summary.setObjectType(ObjectTypeEnum.ITEM);
      
      // existing content type name
      {
         final String contentTypeName = "rffGeneric";
         final String contentTypeLabel = "Generic";
         summary.setContentTypeName(contentTypeName);
         final JSONObject json = PSContentBrowser.summaryToJsonObject(summary,
               new HashMap<String, String>());
         
         TestCase.assertEquals(summary.getGUID().getUUID(), json.get(COLUMN_ID));
         TestCase.assertTrue(json.getString(COLUMN_NAME).contains(name));
         TestCase.assertEquals(contentTypeLabel, json.get(COLUMN_DESCRIPTION));
         TestCase.assertEquals(ObjectTypeEnum.ITEM.getOrdinal(), json.get(COLUMN_TYPE));
      }

      // non-existing content type name
      {
         final String contentTypeName = "NONEXISTING CONTENT TYPE";
         summary.setContentTypeName(contentTypeName);

         final JSONObject json = PSContentBrowser.summaryToJsonObject(summary,
               new HashMap<String, String>());
         TestCase.assertEquals(contentTypeName, json.get(COLUMN_DESCRIPTION));
      }
   }
}
