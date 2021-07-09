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
package com.percussion.share.dao.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;

/**
 * Wraps a Legacy Webservice multi error exception into a single exception
 * by wrapping one of the original exceptions.
 * <p>
 * Most of the operations in the new system are done on a single item so wrapping
 * the first exception found is generally the real exception we want.
 * 
 * @author adamgent
 *
 */
public class PSErrorResultsExceptionDecorator extends PSExceptionDecorator
{

    private static final long serialVersionUID = 1L;

    public PSErrorResultsExceptionDecorator(String message, PSErrorResultsException cause)
    {
        this(cause);
    }
    
    public PSErrorResultsExceptionDecorator(PSErrorResultsException cause)
    {
        Map<IPSGuid, Object> errors =  cause.getErrors();
        Throwable realCause = cause;
        if (! errors.isEmpty() ) {
            Entry<IPSGuid, Object> entry = errors.entrySet().iterator().next();
            Object object = entry.getValue();
            if (object instanceof Throwable)
                realCause = (Throwable) object;
        }
        wrap(realCause);
    }
}

