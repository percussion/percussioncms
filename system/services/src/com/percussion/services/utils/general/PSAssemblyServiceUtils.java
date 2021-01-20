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
package com.percussion.services.utils.general;

import com.percussion.cms.PSCmsException;
import com.percussion.server.PSRequestParsingException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.PSTemplateNotImplementedException;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.filter.PSFilterException;
import com.percussion.util.PSParseUrlQueryString;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Various methods to allow easy direct calling of the assembly service
 */
public class PSAssemblyServiceUtils
{

   /**
    * Empty ctor
    */
   private PSAssemblyServiceUtils()
   {      
   }
   
   /**
    * Utility method to make a call to the assembly service to assemble a
    * document based on the url passed in. This method expects there is
    * only one result for this url (non multi-page).
    * 
    * @param url the url to retrieve the assembled document.
    * @param extraParams extra parameters that will be appended to the passed in
    *           url. . May be <code>null</code> or empty.
    * @return the assembly result for the url, may be <code>null</code> if an
    *         exception occurred when retrieving the result.
    * @throws PSRequestParsingException if there was an error when trying to
    *            parse parameters from the supplied template url.
    * @throws PSCmsException
    * @throws PSAssemblyException
    * @throws PSTemplateNotImplementedException
    * @throws RepositoryException
    * @throws PSFilterException
    * @throws ItemNotFoundException
    */
   @SuppressWarnings("unchecked")
   public static IPSAssemblyResult getAssembledDocumentResult(
      String url, Map extraParams) throws PSRequestParsingException,
      PSAssemblyException, PSCmsException,
      ItemNotFoundException, PSFilterException,
      RepositoryException, PSTemplateNotImplementedException
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
      .getAssemblyService();
      IPSAssemblyItem item = service.createAssemblyItem();
      
      Map params;
      
      /* The HashMap returned by the parseParameters method has an overridden
       * put method which creates ArrayLists for key values if the key already
       * exists in the map, so create a new HashMap with the mappings in order
       * to use putAll with expected behavior.
       */
      params = new HashMap(PSParseUrlQueryString.parseParameters(url));
      if(extraParams != null && !extraParams.isEmpty())
        params.putAll(extraParams);
           
      Iterator iter = params.keySet().iterator();
      while (iter.hasNext())
      {
        Object param = iter.next();
        Object obj = params.get(param);
        String value = (obj == null) ? "" : obj.toString();
        if (!StringUtils.isBlank(value))
           item.setParameterValue(param.toString(), value);
      }
      
      item.normalize();
      List<IPSAssemblyItem> listofitems = Collections.singletonList(item);
      List<IPSAssemblyResult> results = service.assemble(listofitems);
     
      
      return results.get(0);
      
   }
   /**
    * Utility method to make a call to the assembly service to assemble a
    * document based on the url passed in. This method
    * expects there is only one result for this url (non multi-page).
    * @param url the url to retrieve the assembled document.
    * @param extraParams extra parameters that will be appended to the
    * passed in url. May be <code>null</code> or empty.
    * @return the assembly result for the url as a string, may be <code>null</code> if
    * an exception occurred when retrieving the result.
    * @throws PSRequestParsingException 
    * @throws PSCmsException 
    * @throws PSAssemblyException 
    * @throws PSTemplateNotImplementedException 
    * @throws RepositoryException 
    * @throws PSFilterException 
    * @throws ItemNotFoundException 
    * @throws IllegalStateException if the result is not a textual piece of
    * content
    * @throws UnsupportedEncodingException if the charset is not supported
    */
   public static String getAssembledDocument(
      String url, Map extraParams) throws PSRequestParsingException,
   PSAssemblyException, PSCmsException,
   ItemNotFoundException, PSFilterException,
   RepositoryException, PSTemplateNotImplementedException,
   IllegalStateException, UnsupportedEncodingException
   {
      IPSAssemblyResult result = getAssembledDocumentResult(url, extraParams);
      if(result != null && result.getStatus() == Status.SUCCESS)
      {
         return result.toResultString();
      }
      return null;
   }

}
