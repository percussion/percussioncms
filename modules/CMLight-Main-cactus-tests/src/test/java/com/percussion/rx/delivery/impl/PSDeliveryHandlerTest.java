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
package com.percussion.rx.delivery.impl;

import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.IOUtils;
import org.junit.experimental.categories.Category;

import java.io.*;

/**
 * Three tests for the delivery handler:
 * 
 *     Test Canonicalization logic
 *     Test the retry logic used when the ftp delivery of an RetryItem fails.
 *     Test the release logic of an RetryItem
 *     
 *     The last two tests are dealing with the different methods used for
 *     temporarily storing the content prior to delivery.
 * 
 * RetryItem tests added by Bill Langlais
 * 
 * @author Doug Rand
 */

@Category(IntegrationTest.class)
public class PSDeliveryHandlerTest extends ServletTestCase
{
   public void testCanonicalize()
   {
      PSBaseDeliveryHandler h = new PSFileDeliveryHandler();
      final String abcCanon = "/a/b/c";
      assertEquals(abcCanon, h.canonicalPath("\\a\\\\b//c"));
      assertEquals("a/b/c", h.canonicalPath("a\\\\b//c"));
      assertEquals(abcCanon, h.canonicalPath("d:\\a//\\\\b/c"));
   }


   /**
    * Tests to see if the Retry Item is properly managing the items streams so
    * that they can be read from the beginning after the first time they are
    * read from.
    * 
    * 
    * @throws IOException
    */
   public void testItemRetry() throws IOException
   {
      RetryItemSetupData data = new RetryItemSetupData();

      try
      {
         checkRetryItem(data.getRetryItemFile());
         checkRetryItem(data.getRetryItemData());
      }
      finally
      {
         data.getRetryItemFile().release();
         data.getRetryItemData().release();
      }
   }

   /**
    * Tests to see if the release logic of the RetryItem isworking properly.
    * 
    * @throws IOException
    */
   public void testRetryItemRelease() throws IOException
   {
      RetryItemSetupData data = new RetryItemSetupData();

      // Need to close RetryItems that have that file open
      data.getRetryItemFile().release();

      try
      {
         readContent(data.getRetryItemFile());
         fail("Able to read closed Stream for retryItemFile!");
      }
      catch (Exception e)
      {

      }

      if (data.getRetryItemFileFile().exists())
      {
         fail("Release of retryItemFile did not delete temp file");
      }
   }
   


   private class RetryItemSetupData
   {
      public RetryItemSetupData() throws IOException
      {
         OutputStream os = null;
         InputStream is = null;

         try
         {
            os = new FileOutputStream(mi_file);

            int cnt = 0;
            for (byte let = 'A'; let < 'F'; ++let)
            {
               for (int inc = 0; inc < 100; ++inc)
               {
                  mi_bytearray[cnt++] = let;
               }
            }

            is = new ByteArrayInputStream(mi_bytearray);
            IOUtils.copy(is, os);
         }
         finally
         {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
         }
      }

      public PSBaseFtpDeliveryHandler.RetryItem getRetryItemData()
      {
         return mi_retryItemData;
      }

      public PSBaseFtpDeliveryHandler.RetryItem getRetryItemFile()
      {
         return mi_retryItemFile;
      }

      public File getRetryItemFileFile()
      {
         return mi_file;
      }

      private PSPurgableTempFile mi_file = new PSPurgableTempFile(
            "PSDeliveryHandleUnitTest", ".temp", null);

      private byte[] mi_bytearray = new byte[STREAM_SIZE];

      private PSFtpDeliveryHandler mi_handler = new PSFtpDeliveryHandler();

      private PSBaseDeliveryHandler.Item mi_itemData = mi_handler.new Item(
            new PSLegacyGuid(456, 789), null, 
                  mi_bytearray, null, 100L, "text/html", 123, true, 456, 1000L, 1);

      private PSBaseDeliveryHandler.Item mi_itemFile = mi_handler.new Item(
            new PSLegacyGuid(123, 456), mi_file, null, "text/html", 456, true, 789, 1000L, 1);

      private PSBaseDeliveryHandler.Item mi_itemFileStream;

      private PSBaseFtpDeliveryHandler.RetryItem mi_retryItemData = mi_handler.new RetryItem(
            mi_itemData);

      private PSBaseFtpDeliveryHandler.RetryItem mi_retryItemFile = mi_handler.new RetryItem(
            mi_itemFile);
   }

   private void checkRetryItem(PSBaseFtpDeliveryHandler.RetryItem retryItem)
      throws IOException
   {
      byte[] first = readContent(retryItem);

      if (retryItem.getRetryPossible())
      {
         byte[] second = readContent(retryItem);

         for (int inc = 0; inc < first.length; ++inc)
         {
            assertEquals(
                  "First and Second read different content at position " + inc
                        + " First = " + first[inc] + " Second = "
                        + second[inc], first[inc], second[inc]);
         }
      }
      else
      {
         throw new RuntimeException("RetryItem does not support retry!");
      }
   }

   private byte[] readContent(PSBaseFtpDeliveryHandler.RetryItem retryItem)
      throws IOException
   {
      ByteArrayOutputStream bos = null;
      InputStream stream = null;
      byte[] content = null;

      try
      {
         bos = new ByteArrayOutputStream(STREAM_SIZE);
         stream = retryItem.getContentStream();
         IOUtils.copy(stream, bos);

         content = bos.toByteArray();

         assertEquals("First Stream Read was wrong length " + content.length
               + " not " + STREAM_SIZE, STREAM_SIZE, content.length);
      }
      finally
      {
         IOUtils.closeQuietly(bos);
         IOUtils.closeQuietly(stream);
      }
      return content;
   }

   private static final int STREAM_SIZE = 500;
   
}
