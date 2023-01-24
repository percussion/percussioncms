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
package com.percussion.services.system.data;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.tools.PSTestUtils;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Unit tests for the {@link PSMimeContentAdapter} class.
 */
public class PSMimeContentAdapterTest extends TestCase
{
   /**
    * Tests the programming interface. 
    * 
    * @throws Exception If the test fails
    */
   public void testInterface() throws Exception
   {
      String data = "some content...";
      ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
      
      PSMimeContentAdapter content = new PSMimeContentAdapter();
      // test defaults
      assertEquals(content.getCharacterEncoding(), 
         PSCharSetsConstants.rxStdEnc());
      assertEquals(content.getMimeType(), 
         IPSMimeContentTypes.MIME_TYPE_OCTET_STREAM);
      assertEquals(content.getTransferEncoding(), 
         IPSMimeContentTypes.MIME_ENC_BASE64);
      assertNotNull(content.getContent());
      assertTrue(content.getContentLength() == -1);
      
      try
      {
         content.getGUID();
         assertTrue("should have thrown", false);
      }
      catch (IllegalStateException e)
      {
         // TODO: handle exception
      }

      try
      {
         content.getName();
         assertTrue("should have thrown", false);
      }
      catch (IllegalStateException e)
      {
         // TODO: handle exception
      }
      
      // test setters
      PSTestUtils.testSetter(content, "GUID", null, IPSGuid.class, true);
      PSTestUtils.testSetter(content, "GUID", new PSGuid(
         PSTypeEnum.INTERNAL, 0), IPSGuid.class, true);
      PSTestUtils.testSetter(content, "GUID", new PSGuid(
         PSTypeEnum.CONFIGURATION, 123), IPSGuid.class, false);
      PSTestUtils.testSetter(content, "Name", null, true);
      PSTestUtils.testSetter(content, "Name", "", true);
      PSTestUtils.testSetter(content, "Name", "test", false);
      PSTestUtils.testSetter(content, "Content", null, InputStream.class, true);
      PSTestUtils.testSetter(content, "Content", in, InputStream.class, false);
      PSTestUtils.testSetter(content, "ContentLength", new Long(-2), long.class, true);
      PSTestUtils.testSetter(content, "ContentLength", new Long(100), long.class, false);
      PSTestUtils.testSetter(content, "MimeType", null, true);
      PSTestUtils.testSetter(content, "MimeType", "", true);
      PSTestUtils.testSetter(content, "MimeType", "test", false);
      
      try
      {
         content.setGUID(new PSGuid(PSTypeEnum.CONFIGURATION, 456));
         assertTrue("should have thrown", false);
      }
      catch (IllegalStateException e)
      {
         // TODO: handle exception
      }
      
      PSTestUtils.testSetter(content, "CharacterEncoding", null, true);
      PSTestUtils.testSetter(content, "CharacterEncoding", "", true);
      
      PSTestUtils.testSetter(content, "AttachmentId", new Long(1), long.class, 
         false);
      assertNull(content.getContent());
      
      content.setContent(in);
      assertEquals(content.getAttachmentId(), -1);
   }
}


