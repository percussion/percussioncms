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
package linkback;

import com.percussion.soln.linkback.codec.impl.StringLinkBackTokenImpl;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringLinkBackTokenTest {

    private static final Logger log = LogManager.getLogger(StringLinkBackTokenTest.class);

    @Test
    public final void testEncode() {
        log.info("Starting of testEncode");
        Map<String, Object> pmap = new HashMap<String, Object>();
        pmap.put(IPSHtmlParameters.SYS_CONTENTID, "1013");
        pmap.put(IPSHtmlParameters.SYS_REVISION, new String[] { "7", "8", "9" });
        pmap.put(IPSHtmlParameters.SYS_TEMPLATE, "301");
        pmap.put(IPSHtmlParameters.SYS_FOLDERID, "196");
        pmap.put(IPSHtmlParameters.SYS_SITEID, new String[] { "102", "103" });
        StringLinkBackTokenImpl impl = new StringLinkBackTokenImpl();

        String tok = impl.encode(pmap);
        assertNotNull(tok);
        log.info("token is " + tok);

        Map<String, String> omap = impl.decode(tok);
        assertNotNull(omap);
        log.info("Size of map is " + omap.size());
        assertEquals(5, omap.size());
        assertEquals("7", omap.get(IPSHtmlParameters.SYS_REVISION));
        assertEquals("102", omap.get(IPSHtmlParameters.SYS_SITEID));
        assertEquals("1013", omap.get(IPSHtmlParameters.SYS_CONTENTID));
        log.info("Finished testEncode");
    }
    
    
    @Test
    public final void testEncodeWithVariantId() {
        log.info("Starting of testEncode");
        Map<String, Object> pmap = new HashMap<String, Object>();
        pmap.put(IPSHtmlParameters.SYS_CONTENTID, "1013");
        pmap.put(IPSHtmlParameters.SYS_REVISION, new String[] { "7", "8", "9" });
        /*
         * Notice we use the variantid instead of template here.
         */
        pmap.put(IPSHtmlParameters.SYS_VARIANTID, "301");
        pmap.put(IPSHtmlParameters.SYS_FOLDERID, "196");
        pmap.put(IPSHtmlParameters.SYS_SITEID, new String[] { "102", "103" });
        StringLinkBackTokenImpl impl = new StringLinkBackTokenImpl();

        String tok = impl.encode(pmap);
        assertNotNull(tok);
        log.info("token is " + tok);

        Map<String, String> omap = impl.decode(tok);
        assertNotNull(omap);
        log.info("Size of map is " + omap.size());
        assertEquals(5, omap.size());
        /*
         * Variantid should now be template.
         */
        assertEquals("301", omap.get(IPSHtmlParameters.SYS_TEMPLATE));
        log.info("Finished testEncode");
    }
    
}
