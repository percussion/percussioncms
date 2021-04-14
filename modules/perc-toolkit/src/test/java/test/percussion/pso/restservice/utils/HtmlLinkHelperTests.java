/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.restservice.utils;

import com.percussion.pso.restservice.utils.HtmlLinkHelper;
import junit.framework.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class HtmlLinkHelperTests {

	@Test
	public void testConvertToAbsoluteLink1() throws URISyntaxException, MalformedURLException {
	
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "image1.jpg");
		Assert.assertEquals("http://www.somedomain.com/image1.jpg", test);
		
	}

	@Test
	public void testDotRelative() throws MalformedURLException, URISyntaxException{
		
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "./image1.jpg");
		Assert.assertEquals("http://www.somedomain.com/image1.jpg", test);
		
		
	}
	
	@Test
	public void testRootAbsoluteFile() throws MalformedURLException, URISyntaxException{
		
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "/image1.jpg");
		Assert.assertEquals("http://www.somedomain.com/image1.jpg", test);
		
		
	}
	

	@Test
	public void testRelativeDir() throws MalformedURLException, URISyntaxException{
		
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "images");
		Assert.assertEquals("http://www.somedomain.com/images", test);
		
		
	}

	@Test
	public void testRelativeDirTrailingSlash() throws MalformedURLException, URISyntaxException{
		
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "images/");
		Assert.assertEquals("http://www.somedomain.com/images/", test);
		
		
	}
	
	@Test
	public void testRelativeDirTrailingSlashFile() throws MalformedURLException, URISyntaxException{
		
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "images/file1.docx");
		Assert.assertEquals("http://www.somedomain.com/images/file1.docx", test);
		
		
	}
	
	
	@Test
	public void testRelativeDirTrailingSlashFileBookmark() throws MalformedURLException, URISyntaxException{
		
		String test = HtmlLinkHelper.convertToAbsoluteLink("http://www.somedomain.com/", "images/file1.html#234.78");
		Assert.assertEquals("http://www.somedomain.com/images/file1.html#234.78", test);
		
		
	}

	@Test
	public void testGetBaseLink() throws URISyntaxException, MalformedURLException{

		String test = HtmlLinkHelper.getBaseLink("http://www.somewhere.gov/news/about.html?month=03&year=2011");
		
		Assert.assertEquals("http://www.somewhere.gov/", test);
	}
	
	
	@Test
	public void testBaseWithPort() throws MalformedURLException, URISyntaxException{
		String test = HtmlLinkHelper.getBaseLink("http://www.somewhere.gov:8793/news/about.html?month=03&year=2011");
		
		Assert.assertEquals("http://www.somewhere.gov:8793/", test);
	}
	
	@Test
	public void testFixLinksRandom() throws IOException, TransformerException, ParserConfigurationException, SAXException, URISyntaxException{
		
		InputStream is = this.getClass().getResourceAsStream("randomcar.html");
		
		 Reader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder builder = new StringBuilder();
	        char[] buffer = new char[8192];
	        int read;
	        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
	            builder.append(buffer, 0, read);
	        }

		String n = HtmlLinkHelper.convertLinksToAbsolute("http://www.percussion.com/randomcar.html",builder.toString());
		
		Assert.assertEquals(builder.toString(), n);
		
	}
}
