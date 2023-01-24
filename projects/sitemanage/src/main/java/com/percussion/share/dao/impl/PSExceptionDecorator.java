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

import static org.apache.commons.lang.Validate.notNull;

/**
 * This decorator wraps an existing exception making 
 * instances of this class look like the wrapped exception.
 * <p>
 * This is different than chaining exceptions where the 
 * {@link #getCause()} is the real exception you wanted
 * to throw.
 * <p>
 * The reason why you would use this class or a derivative 
 * instead of chaining is to avoid useless and unnecessary stack
 * information.
 *  
 * @author adamgent
 *
 */
public class PSExceptionDecorator extends RuntimeException
{

    private static final long serialVersionUID = 1L;
    private String message;
    
    protected void wrap(Throwable cause) {
        notNull(cause);
        setMessage(cause.getMessage());
        setStackTrace(cause.getStackTrace());
        initCause(cause.getCause());
    }

    @Override
    public String getMessage()
    {
        return message;
    }
    
    protected void setMessage(String message)
    {
        this.message = message;
    }
}

