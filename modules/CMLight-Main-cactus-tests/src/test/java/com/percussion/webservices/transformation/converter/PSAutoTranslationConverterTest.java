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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.Converter;
import org.junit.experimental.categories.Category;

/**
 * Test the {@link PSAutoTranslationConverter}.
 */
@Category(IntegrationTest.class)
public class PSAutoTranslationConverterTest extends PSConverterTestBase
{
   /**
    * Test the converter
    * 
    * @throws Exception if there are any errors.
    */
   public void testConverter() throws Exception
   {
      // Conversion of an object with minimal data.
      // Is needed to make sure that passing around only locale, as required
      // by the create translation web service, does not break the converter
      final PSAutoTranslation src = new PSAutoTranslation();
      src.setLocale("locale2");
      final PSAutoTranslation tgt = (PSAutoTranslation) roundTripConversion(
            PSAutoTranslation.class, 
            com.percussion.webservices.content.PSAutoTranslation.class, src);
      assertEquals(src, tgt);

      // normal case
      src.setCommunityId(1232);
      src.setCommunityName("comm2");
      src.setContentTypeId(4562);
      src.setContentTypeName("ctype2");
      src.setWorkflowId(7892);
      src.setWorkflowName("wf2");
      List<PSAutoTranslation> srcList = new ArrayList<PSAutoTranslation>();
      srcList.add(src);
      srcList.add(tgt);
      
      final List tgtList = roundTripListConversion(
         com.percussion.webservices.content.PSAutoTranslation[].class, 
         srcList);
      assertEquals(srcList, tgtList);
   }
   
   /**
    * Negative values can't be tested with roundrip because they have to contain
    * host information and this information is stripped during conversion.
    */
   public void testNegativeIdsConvertion()
   {
      final long communityId = 5;
      final long contentTypeId = 6;
      final long workflowId = 7;

      // start with the non-stripped host information in the client object
      final com.percussion.webservices.content.PSAutoTranslation src =
         new com.percussion.webservices.content.PSAutoTranslation();
      src.setLocale("locale1");
      src.setCommunityId(getGuid(PSTypeEnum.COMMUNITY_DEF, communityId));
      src.setContentTypeId(getGuid(PSTypeEnum.NODEDEF, contentTypeId));
      src.setWorkflowId(getGuid(PSTypeEnum.WORKFLOW, workflowId));

      final Converter converter =
            PSTransformerFactory.getInstance().getConverter(
                  com.percussion.webservices.content.PSAutoTranslation.class);
      final PSAutoTranslation dst =
            (PSAutoTranslation) converter.convert(PSAutoTranslation.class, src);
      assertEquals(communityId, dst.getCommunityId());
      assertEquals(contentTypeId, dst.getContentTypeId());
      assertEquals(workflowId, dst.getWorkflowId());
   }

   /**
    * Generates a negative GUID for the provided type and id.
    * @param type the type to generate GUID for.
    * Assumed not <code>null</code>. 
    * @param id the object id.
    * Assumed not 0.
    * @return long presentation of new GUID.
    */
   private long getGuid(PSTypeEnum type, long id)
   {
      final long host = 92642436432L;
      long guid = new PSGuid(host, type, id).longValue();
      assertTrue(guid < 0);
      return guid;
   }
}

