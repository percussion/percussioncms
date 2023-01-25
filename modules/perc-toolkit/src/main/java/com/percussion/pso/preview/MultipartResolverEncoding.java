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
package com.percussion.pso.preview;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * A multipart resolver that fixes up the encoding.  
 * The Content Explorer Applet emits non-standard MIME headers, and this
 * can confuse JBoss/Tomcat, so to get around this, we look for any
 * charset that contains a semicolon, and strip off the junk that 
 * follows.   
 *
 * @author DavidBenua
 *
 */
public class MultipartResolverEncoding extends CommonsMultipartResolver
      implements
         MultipartResolver
{

   private static final Logger log = LogManager.getLogger(MultipartResolverEncoding.class);
   /**
    * @see CommonsMultipartResolver#determineEncoding(HttpServletRequest)
    */
   @Override
   protected String determineEncoding(HttpServletRequest request)
   {
      String encoding = super.determineEncoding(request);
      
      if(encoding.contains(";"))
      {
         encoding = StringUtils.substringBefore(encoding, ";");
         log.debug("fixed up encoding {]", encoding);
      }
      return encoding;
   }
   
   
}
