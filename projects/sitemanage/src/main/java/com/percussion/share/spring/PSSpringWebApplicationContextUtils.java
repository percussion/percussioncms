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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    // This does not work in JBoss 5 because JBoss sucks and because of
    // component scanning bug.
    // public static void injectDependenciesByAnnotations(Object bean) throws
    // Exception {
    // logAutoWire(bean);
    // AutowireCapableBeanFactory beanFactory =
    // getWebApplicationContext().getAutowireCapableBeanFactory();
    // beanFactory.autowireBeanProperties(bean,
    // AutowireCapableBeanFactory.AUTOWIRE_NO, false);
    // beanFactory.initializeBean(bean, bean.getClass().getName());
    // }

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
    private static final Log log = LogFactory.getLog(PSSpringWebApplicationContextUtils.class);

}
