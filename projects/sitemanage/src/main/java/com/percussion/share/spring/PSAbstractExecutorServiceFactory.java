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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.FactoryBean;

/**
 * Springify Java Concurrency {@link ExecutorService} so that you can
 * use the default {@link ExecutorService}s provided {@link Executors} as
 * spring beans.
 * <p> 
 * For more information on FactoryBeans read springs documentation.
 * @author adamgent
 *
 */
public abstract class PSAbstractExecutorServiceFactory implements FactoryBean
{

    private ThreadFactory threadFactory;
    
    @Override
    public abstract ExecutorService getObject() throws Exception;

    @SuppressWarnings("unchecked")
    @Override
    public Class getObjectType()
    {
        return ExecutorService.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

}

