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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentException;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class PSKeywordModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      return loadKeyword(guid,true);
   }

   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return loadKeyword(guid,false);
   }

   /**
    * Loads the readonly or modifiable keyword based on the readonly flag.
    * 
    * @param guid must be a valid keyword guid.
    * @param readonly flag to indicate whether to load readonly or modifiable
    * keyword object.
    * @return The keyword corresponding to the supplied guid, never
    * <code>null</code>.
    */
   private PSKeyword loadKeyword(IPSGuid guid, boolean readonly)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");

      IPSContentService service = (IPSContentService) getService();
      PSKeyword kw = null;
      try
      {
         // As there is no readonly version of the keyword implementation return
         // modifiable object in both cases.
         if (readonly)
         {
            kw = service.loadKeyword(guid, null);
         }
         else
         {
            kw = service.loadKeyword(guid, null);
         }
      }
      catch (PSContentException e)
      {
         throw new RuntimeException(e);
      }
      if (kw == null)
      {
         String msg = "Failed to load the design object for guid ({0}) "
               + "of type ({1})";
         Object[] margs = { guid.toString(), getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, margs));
      }
      return kw;

   }

   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj must not be null");
      if (!(obj instanceof PSKeyword))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      IPSContentService service = (IPSContentService) getService();
      service.saveKeyword((PSKeyword) obj);
   }

   @Override
   public IPSGuid nameToGuid(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null");
      IPSContentService service = (IPSContentService) getService();
      List<PSKeyword> kws = service.findKeywordsByLabel(name, null);
      if (kws.size() < 1)
      {
         String msg = "Failed to find the keyword for the given name ({0})";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return kws.get(0).getGUID();
   }
}
