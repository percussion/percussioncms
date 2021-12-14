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
