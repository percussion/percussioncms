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
package com.percussion.share.spring;

import static org.apache.commons.lang.Validate.isTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Wraps {@link Executors#newFixedThreadPool(int, java.util.concurrent.ThreadFactory)} as
 * a spring bean.
 * 
 * @author adamgent
 *
 */
public class PSFixedThreadPoolExecutorService extends PSAbstractExecutorServiceFactory
{

    private int poolSize = 0;
    
    @Override
    public ExecutorService getObject() throws Exception
    {
        int n = getPoolSize();
        isTrue(n > 0, "pool size must be greater than 0");
        if (getThreadFactory() != null)
            return Executors.newFixedThreadPool(n, getThreadFactory());
        return Executors.newFixedThreadPool(n);
    }



    public int getPoolSize()
    {
        return poolSize;
    }

    public void setPoolSize(int poolSize)
    {
        this.poolSize = poolSize;
    }
    
    

}

