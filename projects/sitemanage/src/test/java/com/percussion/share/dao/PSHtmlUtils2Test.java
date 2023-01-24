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
