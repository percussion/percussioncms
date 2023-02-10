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
package com.percussion.share.validation;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.validation.PSErrors.PSObjectError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PSErrorCauseTest
{

    @Test
    public void testCreateErrorCause() throws Throwable
    {
        Exception r;
        try
        {
            throw new RuntimeException("Fail");
        }
        catch (Exception e)
        {
            r = e;
        }

        PSErrors errors = new PSErrors();
        PSObjectError o = new PSObjectError();
        o.setCause(new PSErrorCause(r));
        List<String> args = new ArrayList<String>();
        args.add("arg1");
        args.add("arg2");
        args.add("arg3");
        o.setArguments(args);
        errors.setGlobalError(o);
        String xml = PSSerializerUtils.marshal(errors);
        log.debug(xml);
        System.out.println(xml);
        PSErrors e = PSSerializerUtils.unmarshal(xml, PSErrors.class);
        log.debug(e.getGlobalError().getCause());
        
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSErrorCauseTest.class);

}
