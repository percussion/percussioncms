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

package com.percussion.services.assembly.jexl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger log = LogManager.getLogger(PSJexlDbExtensionsTest.class);

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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
}
