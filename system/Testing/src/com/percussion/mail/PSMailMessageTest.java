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
package com.percussion.mail;

import com.percussion.util.PSCharSets;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.StringTokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the PSMailMessage class.
 */
public class PSMailMessageTest
{
   public PSMailMessageTest() {}

   @Test
   public void testConstructor() throws Exception
   {
      PSMailMessage msg = new PSMailMessage();
      assertEquals("", msg.getBodyText());
      assertEquals(PSCharSets.rxJavaEnc(), msg.getCharEncoding());
   }

   @Test
   public void testBodyTextRandom() throws Exception
   {
      // append a really long line and make sure it gets broken properly
      StringBuffer line = new StringBuffer(8400);
      SecureRandom rand = new SecureRandom();
      for (int i = 0; i < 8400; i++)
      {
         line.append( (char)(32 + (rand.nextInt(126-32))));
      }

      PSMailMessage msg = new PSMailMessage();

      {
         msg.appendBodyText(line.toString());

         String bodyText = msg.getBodyText();
         int crLf = bodyText.indexOf("\r\n");
         int pos = 998;
         assertEquals(pos, crLf);
         while (crLf > 0)
         {
            System.err.println("expected pos=" + pos + "; actual=" + crLf);
            crLf = bodyText.indexOf("\r\n", pos+2);
            pos+=1000;
         }
      }

      {
         for (int i = 0; i < 10; i++)
         {
            line.insert(rand.nextInt(line.length() - 1), "\r\n");
         }
         msg.appendBodyText(line.toString());

         String bodyText = msg.getBodyText();
         int crLf = bodyText.indexOf("\r\n");
         int lastCrLf = crLf;
         while (crLf > 0)
         {
            assertTrue("" + (crLf - lastCrLf) + " <= 1000?", (crLf - lastCrLf) <= 1000);
            lastCrLf = crLf;
            crLf = bodyText.indexOf("\r\n", crLf+2);
         }
      }

   }

   @Test
   public void testBodyText() throws java.io.IOException
   {
      // append a really long line and make sure it gets broken properly
      StringBuffer line = new StringBuffer(8400);
      for (int i = 0; i < 8400; i++)
      {
         line.append('a');
      }

      String bodyLine = line.toString();
      System.err.println("line\r\n" + bodyLine);

      PSMailMessage msg = new PSMailMessage();
      msg.appendBodyText(bodyLine);

      String body = msg.getBodyText();
      System.err.println("body\r\n" + body);

      String eachLine = bodyLine.substring(0, 998) + "\r\n";
      for (int i = 0; i <= 7400; i += 1000)
      {
         assertEquals(eachLine, body.substring(i, i+1000));
      }
   }

   @Test
   public void testBodyTextWithLines() throws java.io.IOException
   {
      StringBuffer line = new StringBuffer(100);
      String content = "This is some text right here, baby!";
      for (int i = 0; i < 100; i++)
      {
         line.append(content);
         line.append("\r\n");
      }

      PSMailMessage msg = new PSMailMessage();
      msg.appendBodyText(line.toString());

      StringTokenizer tok = new StringTokenizer(msg.getBodyText(), "\r\n");
      int i = 0;
      while (tok.hasMoreTokens())
      {
         assertEquals(content, tok.nextToken());
         i++;
      }
      assertEquals(i, 100);
   }

   @Test
   public void testBodyTextWithLinesSeparate() throws java.io.IOException
   {
      String content = "This is some text right here, baby!";

      PSMailMessage msg = new PSMailMessage();
      for (int i = 0; i < 100; i++)
      {
         msg.appendBodyText(content);
         msg.appendBodyText("\r\n");
      }

      StringTokenizer tok = new StringTokenizer(msg.getBodyText(), "\r\n");
      int i = 0;
      while (tok.hasMoreTokens())
      {
         assertEquals(content, tok.nextToken());
         i++;
      }
      assertEquals(100, i);
   }


   @Test
   public void testBodyTextWithLinesTogether() throws java.io.IOException
   {
      String content = "This is some text right here, baby!";

      PSMailMessage msg = new PSMailMessage();
      for (int i = 0; i < 100; i++)
      {
         msg.appendBodyText(content + "\r\n");
      }

      StringTokenizer tok = new StringTokenizer(msg.getBodyText(), "\r\n");
      int i = 0;
      while (tok.hasMoreTokens())
      {
         assertEquals(content, tok.nextToken());
         i++;
      }
      assertEquals(100, i);
   }

   @Test
   public void testBodyTextWithMaxLengthLinesTogether() throws java.io.IOException
   {
      System.err.println("\n\n\n");
      String content = "123456789*";
      StringBuffer contentBuf = new StringBuffer(998);
      for (int i = 0; i < 99; i++)
      {
         contentBuf.append(content);
      }
      contentBuf.append("12345678\r\n");
      assertEquals(1000, contentBuf.length());
      System.err.println("INDEX OF \\r\\n is: " + contentBuf.toString().indexOf("\r\n"));
      PSMailMessage msg = new PSMailMessage();
      
      String longLine = contentBuf.toString().substring(0, contentBuf.length() - 2);
      for (int i = 0; i < 10; i++)
      {
         msg.appendBodyText(longLine);
      }


      StringTokenizer tok = new StringTokenizer(msg.getBodyText(), "\r\n");
      int i = 0;
      while (tok.hasMoreTokens())
      {
         assertEquals(longLine, tok.nextToken());
         i++;
      }
      assertEquals(10, i);
   }

}
