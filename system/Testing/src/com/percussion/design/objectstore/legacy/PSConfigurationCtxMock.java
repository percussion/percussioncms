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
