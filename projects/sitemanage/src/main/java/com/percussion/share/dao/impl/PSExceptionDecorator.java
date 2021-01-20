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

