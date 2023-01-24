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
package com.percussion.utils.string;

import com.percussion.utils.string.PSXmlPIUtils.Action;
import com.percussion.utils.testing.UnitTest;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class PSXmlPITest
{
   static String ms_test1 = "<doc><el><%active%><?php phpcode?></el><%active2%></doc>";

   static String ms_encode = "<doc><el><!-- @psx_activetag_0 --><!-- @psx_activetag_1 --></el><!-- @psx_activetag_2 --></doc>";

   static String ms_result1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         + "<doc>\n"
         + "   <el><?psx-activetag <%active%>?><?php phpcode?></el><?psx-activetag <%active2%>?>\n"
         + "</doc>";

   @Test
   public void testEncode() throws Exception
   {
      PSStopwatch sw = new PSStopwatch();

      sw.start();
      PSPair<Map<Integer, PSPair<Action, String>>, String> result = PSXmlPIUtils
            .encodeTags(ms_test1);
      sw.stop();
      System.out.println("Encode took " + sw);
      assertEquals(ms_encode, result.getSecond());
      assertEquals(3, result.getFirst().size());
      assertEquals("<%active%>", result.getFirst().get(0).getSecond());
      assertEquals("<?php phpcode?>", result.getFirst().get(1).getSecond());
      assertEquals("<%active2%>", result.getFirst().get(2).getSecond());
      assertEquals(Action.QUOTE, result.getFirst().get(0).getFirst());
      assertEquals(Action.PHP, result.getFirst().get(1).getFirst());
      assertEquals(Action.QUOTE, result.getFirst().get(2).getFirst());
      

      // Dom handling
      sw.start();
      try(InputStream stream = new ByteArrayInputStream(result.getSecond().getBytes(StandardCharsets.UTF_8))) {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(stream, false);
         PSXmlPIUtils.substitutePIs(doc, result.getFirst());
         String str = PSXmlDocumentBuilder.toString(doc);
         sw.stop();
         System.out.println("DOM took " + sw);
         assertEquals(ms_result1, str);
      }
   }

   @Test
   public void testEncode2() throws Exception
   {
      testEncode(); // Just run again for timings
   }   

   static String ms_result2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         + "<doc>\n" + "   <el><%active%><?php phpcode?></el><%active2%>\n" + "</doc>";

   @Test
   public void testStrip() throws Exception
   {
      PSStopwatch sw = new PSStopwatch();

      sw.start();
      String out = PSXmlPIUtils.removePI(ms_result1);
      sw.stop();
      System.out.println("Remove took " + sw);
      assertEquals(ms_result2, out);
   }

}
