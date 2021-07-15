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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests {@link PSHtmlUtils#stripScriptElement(String)} and {@link PSHtmlUtils#stripElement(String, String)}
 * 
 * @author yubingchen
 *
 */
public class PSHtmlUtils2Test
{
    @Test
    public void testStripHtmlTag() throws Exception
    {
        // strip SCRIPT tag, where it contains text between begin & end tag
        String hasTitleTag = "<html><head> <SCRIPT>hello world</script> </header> <body> <div> <p>Hello</div> </body></html>";
        String hasTitleTag_stripped = "<html><head>  </header> <body> <div> <p>Hello</div> </body></html>";
        validateStripHtml(hasTitleTag, hasTitleTag_stripped);

        // strip SCRIPT tag, where it contains attribute only
        String hasTitleTag1 = "<html><head> <SCRIPT src=\"/hello\" /> </header> <body> <div> <p>Hello</div> </body></html>";
        validateStripHtml(hasTitleTag1, hasTitleTag_stripped);
    }
    
    private void validateStripHtml(String src, String strippedSrc)
    {
        String stripped = PSHtmlUtils.stripElement(src, "script");
        assertEquals(strippedSrc, stripped);
        
        stripped = PSHtmlUtils.stripScriptElement(src);
        assertEquals(strippedSrc, stripped);
    }
}
