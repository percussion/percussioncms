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
package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

/**
 * UDF to get the rhythmyx request root. This can be used as base request url
 * to make XSL document calls to rhythmyx resources. 
 */
public class PSRhythmyxRoot extends PSSimpleJavaUdfExtension
{
   /**
    * This UDF constructs the rhythmyx root url out of the supplied request
    * and returns it as <code>String</code>.
    * 
    * @param params no parameters are used for this UDF, all values will be 
    *    ignored.
    * @param request the request from which the protocol, host and port will
    *    be inherited.
    * @return a rhythmyx request root as <code>String</code>, something like
    *    <code>http://localhost:9992/Rhythmyx</code>.
    * @throws PSConversionException is never thrown.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      String protocol = request.getOriginalProtocol();
      String host = request.getOriginalHost();
      int port = request.getOriginalPort();
      String root = PSServer.getRequestRoot();
      
      return protocol + "://" + host + ":" + port + root;
   }
}
