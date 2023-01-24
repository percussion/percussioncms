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
    StringBuilder sb = new StringBuilder();
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

