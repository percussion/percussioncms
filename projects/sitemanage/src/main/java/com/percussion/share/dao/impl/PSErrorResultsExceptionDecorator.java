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

