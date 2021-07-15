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

package com.percussion.share.dao;

import java.io.InputStream;
import java.util.UUID;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;

import com.percussion.share.service.exception.PSExtractHTMLException;
import org.junit.experimental.categories.Category;

/**
 * Testing {@link PSHtmlUtils}. This test has to be run within server environment
 * because it is rely on PSServer.getRxDir() to load tidy property file.
 */
@Category(IntegrationTest.class)
public class PSHtmlUtilsTest extends ServletTestCase
{

	private final static String MISSING_P_TAG = "<div> <p>Hello</div>";

	//Could this be moved to a separate html file in a testing resources location?
	private final static String VALID_HTML = "<html><head> <title>peter</title> </header> <body> <link rel=\"canonical\" href=\"/myparentfolder\" /> <div> <p>Hello</div> </body></html>";
	private final static String VALID_HTML_NO_CANONICAL = "<html><head> <title>peter</title> </header> <body>  <div> <p>Hello</div> </body></html>";

	public void testExtraction_Negative() throws Exception
	{
		String html = PSHtmlUtils.extractHtml("unknownTag", MISSING_P_TAG, null, true);
		assertTrue(StringUtils.isBlank(html));

		try
		{
			PSHtmlUtils.extractHtml("foo (2)", MISSING_P_TAG, null, true);
			assertTrue(false);
		}
		catch (PSExtractHTMLException e)
		{
			// Bad input (or bad selector)
			System.out.println(e.getMessage());
		}
	}

	public void testExtract() throws Exception
	{
		String html = PSHtmlUtils.extractHtml("div", MISSING_P_TAG, null, true);
		assertTrue(html.contains("<div>"));
		assertTrue(html.contains("</div>"));
		assertTrue(html.contains("<p>"));
		assertTrue(html.contains("</p>"));

		html = PSHtmlUtils.extractHtml("div", MISSING_P_TAG, null, false);
		assertFalse(html.contains("<div>"));
		assertFalse(html.contains("</div>"));
		assertTrue(html.contains("<p>"));
		assertTrue(html.contains("</p>"));

		html = PSHtmlUtils.extractHtml("body", VALID_HTML, null, false);
		assertTrue(html.contains("Hello"));
		assertFalse(html.contains("<body>"));
		assertFalse(html.contains("</body>"));
		assertFalse(html.contains("peter"));

		html = PSHtmlUtils.extractHtml("body", VALID_HTML, null, true);
		assertTrue(html.contains("<body>"));
		assertTrue(html.contains("</body>"));
		assertTrue(html.contains("Hello"));
		assertFalse(html.contains("peter"));
	}

	/**
	 * Test against some real site home pages.
	 * 
	 * @throws Exception
	 */
	public void testHtmlFile() throws Exception
	{

		System.out.println("Testing home_bst.html....");
		validateHtmlFile("home_bst.html");

		System.out.println("Testing home_cc.html....");
		validateHtmlFile("home_cc.html");

		System.out.println("Testing home_sw.html....");
		validateHtmlFile("home_sw.html");

		System.out.println("Testing home_ws.html...."); 
		validateHtmlFile("home_ws.html");

		try
		{
			System.out.println("Testing home_w.html....");
			validateHtmlFile("home_w.html"); 
			assertTrue(false);
		}
		catch (Exception e)
		{
			// tidy fail on "unknown" tag -- <MACRO_PREVIEWMENUITEM>
			// this can be "fixed" by add "macro_previewmenuitem" into "new-empty-tags" properties
			System.out.println("Expecting tidy fail on \"home_w.html\".");
		}

	}

	/**
	 * Validates the specified HTML file.
	 * 
	 * @param name the name of the HTML file, assumed not empty.
	 * 
	 * @throws Exception if an error occurs.
	 */
	private void validateHtmlFile(String name) throws Exception
	{
		InputStream in = null;

		try
		{
			in = this.getClass().getResourceAsStream(name);
			String htmlSource = IOUtils.toString(in);

			String html = PSHtmlUtils.extractHtml("body", htmlSource, name, true);
			assertTrue(html.contains("<body"));

			html = PSHtmlUtils.extractHtml("body", htmlSource, name, false);
			assertFalse(html.contains("<body"));
		}
		finally
		{
			in.close();
		}
	}


	/**
	 * Test two sample htmls, one that contains a canonical link and one that does not.
	 * Additionally test something that is not html.
	 * 
	 */
	@Test
	public void testCheckLinkCanonicalElementTest(){

		assertTrue(PSHtmlUtils.checkLinkCanonicalElement(VALID_HTML));

		assertFalse(PSHtmlUtils.checkLinkCanonicalElement(VALID_HTML_NO_CANONICAL));

		assertFalse(PSHtmlUtils.checkLinkCanonicalElement(UUID.randomUUID().toString()));
	}

	/**
	 * Test stripping of canonical element from a sample html, ensure it changes and equals a non-canonical version of the html.
	 * 
	 *  Try stripping a canonical element from a sample html and a random string that do not contain canonical elements.  Ensure they are unchanged.
	 * 
	 */
	@Test
	public void testStripLinkCanonicalElementTest() {

		String strippedHtml = PSHtmlUtils.stripLinkCanonicalElement(VALID_HTML);
		assertEquals(VALID_HTML_NO_CANONICAL, strippedHtml);
		assertFalse(strippedHtml.equals(VALID_HTML));

		String unchangedHtmlString = PSHtmlUtils.stripLinkCanonicalElement(VALID_HTML_NO_CANONICAL);
		assertEquals(VALID_HTML_NO_CANONICAL, unchangedHtmlString);

		String unchangedString = UUID.randomUUID().toString();
		assertEquals(unchangedString, PSHtmlUtils.stripLinkCanonicalElement(unchangedString));

	}


}
