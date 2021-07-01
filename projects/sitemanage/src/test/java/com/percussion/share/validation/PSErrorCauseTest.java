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
package com.percussion.share.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.validation.PSErrors.PSObjectError;

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
        log.debug(e);
        
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSErrorCauseTest.class);

}
