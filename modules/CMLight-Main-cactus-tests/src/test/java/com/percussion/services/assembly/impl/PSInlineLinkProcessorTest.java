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
package com.percussion.services.assembly.impl;

import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.filter.data.PSItemFilter;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * This is a hard test to keep up to date. It needs to contain a body field
 * that is valid, with valid slots and templates referenced. It is not 
 * automatically run.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSInlineLinkProcessorTest extends ServletTestCase
{
   /**
    * Data for test
    */
   public static String ms_data1 = "<div class=\"rxbodyfield\" xmlns:o=\"urn:www.microsoft.com/office\" "
         + "xmlns:st1=\"urn:www.microsoft.com/smarttags\" "
         + "xmlns:w=\"urn:www.microsoft.com/word\" "
         + "xmlns:x=\"urn:www.microsoft.com/excel\">"
         + "<p class=\"MsoBodyText\" extra=\"&quot;foo&quot;\">Before you sign a "
         + "thing, make sure you're making the \"right\" decisions. "
         + "Now looming in front of you are two big questions: How much can "
         + "I afford? How do I choose the right home? Here\"s a start at "
         + "answering those questions.</p></div>";

   /**
    * @throws Exception
    */
   public void testData1() throws Exception
   {
      String result = process(ms_data1);
      assertEquals(ms_data1, result);

      result = process(ms_data2);
      assertEquals(ms_dataout2, result);
   }

   /**
    * @param input
    * @return processed value
    * @throws Exception
    */
   protected String process(String input) throws Exception
   {
      PSItemFilter filter = new PSItemFilter();
      IPSAssemblyResult work = new PSAssemblyWorkItem();
      work.setFilter(filter);
      PSInlineLinkProcessor proc = new PSInlineLinkProcessor(filter, work);

      String newstr = (String) proc.translate(input);
      return newstr;
   }

   /**
    * 
    */
   public static String ms_data2 = "<div class=\"rxbodyfield\" xmlns:o=\"urn:www.microsoft.com/office\" xmlns:st1=\"urn:www.microsoft.com/smarttags\"\n"
         + "   xmlns:w=\"urn:www.microsoft.com/word\" xmlns:x=\"urn:www.microsoft.com/excel\">\n"
         + "   <p>Inline link: <a title=\"An inline link\"\n"
         + "        href=\"http://zircon.percussion.local:9992/Rhythmyx/rxs_Shared_cas/p_shared.html?shared_variantid=1&amp;sys_revision=4&amp;sys_siteid=301&amp;sys_authtype=0&amp;sys_contentid=377&amp;sys_variantid=329&amp;sys_folderid=316&amp;sys_context=0\"\n"
         + "        rxinlineslot=\"103\" sys_dependentvariantid=\"502\" sys_dependentid=\"377\" \n"
         + "        sys_siteid=\"301\" sys_folderid=\"316\" inlinetype=\"rxhyperlink\">click here</a>.\n"
         + "      An inline image: <img title=\"A picture\" src=\"http://zircon.percussion.local:9992/Rhythmyx/rxs_SharedImage_cas/image?sys_revision=1&amp;sys_siteid=301&amp;sys_authtype=0&amp;sys_contentid=394&amp;sys_variantid=324&amp;sys_folderid=393&amp;sys_context=0\"\n"
         + "               rxinlineslot=\"104\" sys_dependentvariantid=\"324\" sys_dependentid=\"394\"\n"
         + "               sys_siteid=\"301\" sys_folderid=\"393\" inlinetype=\"rximage\"/>\n"
         + "   </p>\n"
         + "   <p>Here\'s an inline variant:</p>\n"
         + "   <div contenteditable=\"false\" rxinlineslot=\"105\" sys_dependentvariantid=\"503\" sys_dependentid=\"377\" sys_siteid=\"301\" \n"
         + "   sys_folderid=\"316\" inlinetype=\"rxvariant\" unselectable=\"on\">\n"
         + "   <p contenteditable=\"false\" unselectable=\"on\" xmlns:o=\"urn:www.microsoft.com/office\" xmlns:st1=\"urn:www.microsoft.com/smarttags\" xmlns:w=\"urn:www.microsoft.com/word\" xmlns:x=\"urn:www.microsoft.com/excel\">The fund\'s objective is to provide long-term capital growth. It invests primarily in equity and equity-related securities of companies around the world that are primarily engaged in the financial services industry.</p>\n"
         + "   <p class=\"rxbodyfield\" contenteditable=\"false\" unselectable=\"on\" xmlns:o=\"urn:www.microsoft.com/office\" xmlns:st1=\"urn:www.microsoft.com/smarttags\" xmlns:w=\"urn:www.microsoft.com/word\" xmlns:x=\"urn:www.microsoft.com/excel\"><strong contenteditable=\"false\" unselectable=\"on\">Investor suitability:</strong> Suitable for investors who wish to diversify their portfolio into specific sectors of the world economy. This fund would complement a large-cap value style fund or a more broadly diversified global equity fund.</p>\n"
         + "   </div>\n" + "</div>";

   /**
    * 
    */
   public static String ms_dataout2 = "<div class=\"rxbodyfield\" xmlns:o=\"urn:www.microsoft.com/office\" xmlns:st1=\"urn:www.microsoft.com/smarttags\" xmlns:w=\"urn:www.microsoft.com/word\" xmlns:x=\"urn:www.microsoft.com/excel\">\n"
         + "   <p>Inline link: <a title=\"An inline link\" href=\"http://127.0.0.1:8080/Rhythmyx/rxs_Shared_cas/p_shared.html?shared_variantid=1&amp;sys_revision=4&amp;sys_siteid=301&amp;sys_authtype=0&amp;sys_contentid=377&amp;sys_variantid=329&amp;sys_folderid=316&amp;sys_context=0\" rxinlineslot=\"103\" sys_dependentvariantid=\"329\" sys_dependentid=\"377\" sys_siteid=\"301\" sys_folderid=\"316\" inlinetype=\"rxhyperlink\">click here</a>.\n"
         + "      An inline image: <img title=\"A picture\" src=\"http://127.0.0.1:8080/Rhythmyx/rxs_SharedImage_cas/image?sys_revision=1&amp;sys_siteid=301&amp;sys_authtype=0&amp;sys_contentid=394&amp;sys_variantid=324&amp;sys_folderid=393&amp;sys_context=0\" rxinlineslot=\"104\" sys_dependentvariantid=\"324\" sys_dependentid=\"394\" sys_siteid=\"301\" sys_folderid=\"393\" inlinetype=\"rximage\"></img>\n"
         + "   </p>\n"
         + "   <p>Here\'s an inline variant:</p>\n"
         + "   <div><p xmlns:o=\"urn:www.microsoft.com/office\" xmlns:st1=\"urn:www.microsoft.com/smarttags\" xmlns:w=\"urn:www.microsoft.com/word\" xmlns:x=\"urn:www.microsoft.com/excel\">The fund\'s objective is to provide long-term capital growth. It invests primarily in equity and equity-related securities of companies around the world that are primarily engaged in the financial services industry.</p><p xmlns:o=\"urn:www.microsoft.com/office\" "
         + "xmlns:st1=\"urn:www.microsoft.com/smarttags\" xmlns:w=\"urn:www.microsoft.com/word\" xmlns:x=\"urn:www.microsoft.com/excel\"><strong>Investor suitability:</strong> Suitable for investors who wish to diversify their portfolio into specific sectors of the world economy. This fund would complement a large-cap value style fund or a more broadly diversified global equity fund.</p></div>\n"
         + "</div>";
}
