/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.utils;


import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author stephenbolton
 * @version $Revision: 1.0 $
 */
public class MethodCacheInterceptor implements MethodInterceptor, InitializingBean {
  /**
   * Field logger.
   */
  private static final Logger logger = LogManager.getLogger(MethodCacheInterceptor.class);

  /**
   * Field cache.
   */
  private Cache cache;

  /**
   * sets cache name to be used
   * @param cache Cache
   */
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  /**
   * Checks if required attributes are provided.
   * @throws Exception
   * @see InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(cache, "A cache is required. Use setCache(Cache) to provide one.");
  }

  /**
   * main method
   * caches method result if method is configured for caching
   * method results must be serializable
   * @param invocation MethodInvocation
   * @return Object
   * @throws Throwable
   * @see MethodInterceptor#invoke(MethodInvocation)
   */
  public Object invoke(MethodInvocation invocation) throws Throwable {
    String targetName  = invocation.getThis().getClass().getName();
    String methodName  = invocation.getMethod().getName();
    Object[] arguments = invocation.getArguments();
    Object result;

    logger.debug("looking for method result in cache");
    String cacheKey = getCacheKey(targetName, methodName, arguments);
    Element element = cache.get(cacheKey);
    if (element == null) {
      //call target/sub-interceptor
      logger.debug("calling intercepted method");
      result = invocation.proceed();

      //cache method result
      logger.debug("caching result");
      element = new Element(cacheKey, (Serializable) result);
      cache.put(element);
    }
    return element.getValue();
  }

  /**
   * creates cache key: targetName.methodName.argument0.argument1...
   * @param targetName String
   * @param methodName String
   * @param arguments Object[]
   * @return String
   */
  private String getCacheKey(String targetName,
                             String methodName,
                             Object[] arguments) {
    StringBuffer sb = new StringBuffer();
    sb.append(targetName)
      .append('.').append(methodName);
    if ((arguments != null) && (arguments.length != 0)) {
      for (int i=0; i < arguments.length; i++) {
        sb.append('.')
          .append(arguments[i]);
      }
    }

    return sb.toString();
  }
}

