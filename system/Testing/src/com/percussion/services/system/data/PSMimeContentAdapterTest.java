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


