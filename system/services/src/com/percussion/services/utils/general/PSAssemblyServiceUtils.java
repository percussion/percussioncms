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
package com.percussion.services.utils.general;

import com.percussion.cms.PSCmsException;
import com.percussion.server.PSRequestParsingException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.PSTemplateNotImplementedException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.util.PSParseUrlQueryString;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

   /**
    * Get a map that associates a particular content type with a set of template
    * ids for this slot
    *
    * @return a map, never <code>null</code> but could be empty
    */
   public static Map<IPSGuid, Set<IPSGuid>> getSlotAssociationMap(IPSTemplateSlot slot)
   {
      Collection<PSPair<IPSGuid, IPSGuid>> assoc = slot.getSlotAssociations();
      Map<IPSGuid, Set<IPSGuid>> map = new HashMap<IPSGuid, Set<IPSGuid>>();
      for (PSPair<IPSGuid, IPSGuid> pair : assoc)
      {
         IPSGuid ctype = pair.getFirst();
         IPSGuid ttype = pair.getSecond();
         Set<IPSGuid> templates = map.get(ctype);
         if (templates == null)
         {
            templates = new HashSet<IPSGuid>();
            map.put(ctype, templates);
         }
         templates.add(ttype);
      }
      return map;
   }

}
