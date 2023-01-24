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

import com.percussion.webservices.PSErrorsException;

/**
 * 
 * Wraps a legacy web service exception.
 * 
 * @see PSErrorResultsExceptionDecorator
 * @author adamgent
 *
 */
public class PSErrorsExceptionDecorator extends PSExceptionDecorator
{
    private static final long serialVersionUID = 1L;

    public PSErrorsExceptionDecorator(PSErrorsException e)
    {
        if (e.getErrors() != null &&  ! e.getErrors().isEmpty()) {
            Object error = e.getErrors().entrySet().iterator().next().getValue();
            if (error instanceof Throwable) {
                wrap(e);
                return;
            }
        }
        wrap(e);
    }
    
}

