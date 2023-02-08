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
package com.percussion.webservices.transformation.converter;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case for the {@link PSMimeContentAdapterConverter}
 */
@Category(IntegrationTest.class)
public class PSMimeContentAdapterConverterTest extends PSConverterTestBase
{
  /**
    * Tests the conversion from a server to a client object.
    *  
    * @throws Exception if the test fails 
    */
   @SuppressWarnings(value={"unchecked"})
   public void testConversion() throws Exception
   {
      String data = "some content...";
      ByteArrayInputStream in; 
      in = new ByteArrayInputStream(data.getBytes());

      PSMimeContentAdapter ca1 = new PSMimeContentAdapter();
      ca1.setContent(in);
      ca1.setContentLength(data.getBytes().length);
      ca1.setName("test");
      ca1.setGUID(new PSGuid(PSTypeEnum.CONFIGURATION, 123));
      
      PSMimeContentAdapter ca2 = (PSMimeContentAdapter) roundTripConversion(
         PSMimeContentAdapter.class, 
         com.percussion.webservices.system.PSMimeContentAdapter.class, 
         ca1);
      ca1.setContent(new ByteArrayInputStream(data.getBytes()));
      assertEquals(ca1, ca2);
      
      ca2.setAttachmentId(12);
      ca2.setContentLength(1000);
      ca2.setName("test2");
      ca2.setCharacterEncoding("enc");
      ca2.setMimeType(IPSMimeContentTypes.MIME_TYPE_APPLICATION_XML);
      ca2.setTransferEncoding(IPSMimeContentTypes.MIME_ENC_BINARY);

      PSMimeContentAdapter ca3 = (PSMimeContentAdapter) roundTripConversion(
         PSMimeContentAdapter.class, 
         com.percussion.webservices.system.PSMimeContentAdapter.class, 
         ca2);
      assertEquals(ca2, ca3);
      
      PSMimeContentAdapter ca4 = new PSMimeContentAdapter();
      ca4.setContent(new ByteArrayInputStream(data.getBytes()));
      ca4.setContentLength(data.getBytes().length);
      ca4.setName("test4");
      ca4.setGUID(new PSGuid(PSTypeEnum.CONFIGURATION, 456));
      
      List<PSMimeContentAdapter> srcList = 
         new ArrayList<PSMimeContentAdapter>();
      srcList.add(ca1);
      srcList.add(ca2);
      srcList.add(ca4);
      List<PSMimeContentAdapter> tgtList = roundTripListConversion(
         com.percussion.webservices.system.PSMimeContentAdapter[].class, 
         srcList);
      srcList.get(0).setContent(new ByteArrayInputStream(data.getBytes()));
      srcList.get(2).setContent(new ByteArrayInputStream(data.getBytes()));
      assertEquals(srcList, tgtList);
   }
}

