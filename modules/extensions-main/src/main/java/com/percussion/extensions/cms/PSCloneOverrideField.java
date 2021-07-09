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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.extensions.cms;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;

/**
 * This is a generic UDF that can be used to compute field value to override 
 * when cloning. The schme basically involves executing a Rhythmyx URL that 
 * generates an XML document with predefined DTD. Then the value is located 
 * based on the name of the element supplied. The parameter description is as 
 * follows:
 * <p>
 * <ul>
 * <li>First parameter is the name of the Rhythmyx resource that generates 
 * the field value to be overridden. This has a syntax of ../<rxApp>/
 * <resource>.cml</li>
 * <li>Second parameter is the exact name of the element that contains the 
 * child node as the value of the field to oevrride. If there are more than 
 * one sch elements, only the first one is considered</li>
 * <li>Rest of the parameters are the parameter name-value pairs that are 
 * required fo rthe resource to generate the field value.</li>
 * </ul>
 * @author RammohanVangapalli
 */
public class PSCloneOverrideField extends PSSimpleJavaUdfExtension
{
   /* (non-Javadoc)
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if(params == null || params.length < 2)
         throw new PSConversionException(request.getUserLocale(), 1000); //TODO msg code

      String resource = params[0].toString();
      String xpath = params[1].toString();
      
      int loc = resource.indexOf(".");
      if(loc > 0)
      resource = resource.substring(0, loc);
      loc = resource.lastIndexOf("/");
      loc = resource.lastIndexOf("/",loc-1);
      resource = resource.substring(loc+1);  

      Map paramMap = new HashMap();
      for(int i=2; i<params.length; i=i+2)
      {
         String name = params[i].toString();
         String value = "";
         if(params.length > i+1)
            value = params[i+1].toString();
         paramMap.put(name, value);
      }
      
      IPSInternalRequest ir = request.getInternalRequest(resource, paramMap, false);
      Document doc = ir.getResultDoc();
      NodeList nl = doc.getElementsByTagName(xpath);
      if(nl.getLength()>0)
      {
         Node node = nl.item(0).getFirstChild();
         if(node.getNodeType() == Node.TEXT_NODE)
            return ((Text)node).getData().trim();
      }
      throw new PSConversionException(request.getUserLocale(), 1000); //TODO msg code
   }
}
