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

import com.percussion.cms.IPSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * Utilities for Spring {@link WebApplicationContext}s.
 * 
 * @author adamgent
 *
 */
public class PSSpringWebApplicationContextUtils
{
    private static WebApplicationContext webApplicationContext;

    /**
     * Gets the default web application context.
     * Use of this method should be avoided as it couples code to this class.
     * @return default web application context.
     */
    public static WebApplicationContext getWebApplicationContext()
    {
        return webApplicationContext;
    }

    protected static void setWebApplicationContext(WebApplicationContext context)
    {
        webApplicationContext = context;
    }


    /**
     * This will autowire objects using the objects getters and setters. It will
     * not work if your object uses constructor based wiring.
     * @param bean never <code>null</code>.
     */
    public static void injectDependencies(Object bean)
    {
        try
        {
            logAutoWire(bean);
            AutowireCapableBeanFactory beanFactory = getWebApplicationContext().getAutowireCapableBeanFactory();

            beanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
            beanFactory.initializeBean(bean, bean.getClass().getName());
        }
        catch (Exception e)
        {
            String errMsg = "Fail to auto inject bean: " + bean.toString();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    private static void logAutoWire(Object bean)
    {
        if (log.isDebugEnabled())
            log.debug("Autowiring bean: " + bean);
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);

}
