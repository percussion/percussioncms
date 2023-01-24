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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.PSMissingApplicationPolicyException;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.xml.sax.SAXException;

import java.io.IOException;

   class PSConfigurationCtxMock extends PSConfigurationCtx{

     public PSConfigurationCtxMock(IPSConfigFileLocator locator, String partOneKey) throws PSInvalidXmlException,
      IOException, SAXException, PSMissingApplicationPolicyException, PSUnknownDocTypeException,
      PSUnknownNodeTypeException
    {
          super(locator, partOneKey);
          // TODO Auto-generated constructor stub
    }

    @Override
      public IPSContainerUtils getUtils(){
         throw new RuntimeException("FixMe");
      }

}
