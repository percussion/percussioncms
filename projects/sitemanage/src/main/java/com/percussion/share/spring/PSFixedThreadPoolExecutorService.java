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

