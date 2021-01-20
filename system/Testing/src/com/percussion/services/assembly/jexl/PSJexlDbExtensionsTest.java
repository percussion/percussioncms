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

package com.percussion.services.assembly.jexl;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class PSJexlDbExtensionsTest {

    /**
     * Get a precomputed velocity context with the jexl extensions registered.
     *
     * @return the context, never <code>null</code>
     */
    public VelocityContext getContext()
    {
        VelocityContext rval = new VelocityContext();
        Map<String, Object> sys = new HashMap<String, Object>();
        rval.put("sys", sys);

        sys.put("string", new PSStringUtils());
        sys.put("db", new PSDbUtils());
        sys.put("doc", new PSDocumentUtils());
        sys.put("guid", new PSGuidUtils());
        sys.put("codec", new PSCodecUtils());
        sys.put("link", new PSLinkUtils());
        sys.put("cond", new PSCondUtils());
        return rval;
    }
    public void doTest(VelocityContext ctx, String inputtemplate,
                       String expectedoutput) throws ParseErrorException,
            MethodInvocationException, ResourceNotFoundException, IOException
    {
        String out = run(ctx, inputtemplate);
        assertEquals(expectedoutput, out);
    }
    /**
     * Run the velocity engine. We use this here to run most of the tests
     *
     * @param ctx the context, from {@link #getContext()}
     * @param template the template to run, never <code>null</code> or empty
     * @return the result
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws ResourceNotFoundException
     * @throws IOException
     */
    public String run(VelocityContext ctx, String template)
            throws ParseErrorException, MethodInvocationException,
            ResourceNotFoundException, IOException
    {
        if (StringUtils.isBlank(template))
        {
            throw new IllegalArgumentException("template may not be null or empty");
        }
        StringWriter out = new StringWriter();
        ms_engine.evaluate(ctx, out, "Velo", template);
        return out.toString();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testDbUtils() throws Exception
    {
        VelocityContext ctx = getContext();
        doTest(
                ctx,
                "$sys.db.get('rxdefault'," +
                        "'select SLOTID, SLOTNAME from RXSLOTTYPE where SLOTID = 504')",
                "[{SLOTNAME=rffContacts, SLOTID=504}]");
    }
    /**
     * Velocity engine eis initialized in the static block, never
     * <code>null</code> afterward
     */
    public static VelocityEngine ms_engine = null;

    static
    {
        ms_engine = new VelocityEngine();
        try
        {
            ms_engine.init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
