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
package com.percussion.services.assembly.impl;

import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.filter.data.PSItemFilter;

import static org.junit.Assert.assertEquals;

/**
 * This is a hard test to keep up to date. It needs to contain a body field
 * that is valid, with valid slots and templates referenced. It is not 
 * automatically run.
 * 
 * @author dougrand
 */
public class PSInlineLinkProcessorTest
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
