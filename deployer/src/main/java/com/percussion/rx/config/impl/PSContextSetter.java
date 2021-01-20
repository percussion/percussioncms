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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * This setter is used to set Context properties.
 *
 * @author YuBingChen
 */
public class PSContextSetter extends PSSimplePropertySetter
{
   @SuppressWarnings("unchecked")
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         @SuppressWarnings("unused")
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {

      // validate the arguments.
      if (!(obj instanceof IPSPublishingContext))
      {
         throw new PSConfigException(
               "obj must be an instance of IPSPublishingContext.");
      }
      IPSPublishingContext context = (IPSPublishingContext) obj;
      if (DEFAULT_SCHEME.equals(propName))
      {
         setDefaultScheme(context, propName, propValue);
      }
      else
      {
         super.applyProperty(context, state, aSets, propName, propValue);
      }
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (DEFAULT_SCHEME.equals(propName))
      {
         IPSPublishingContext context = (IPSPublishingContext) obj;
         if (context.getDefaultSchemeId() == null)
            return null;
         
         PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
         model.setContextId(context.getGUID());
         return model.guidToName(context.getDefaultSchemeId());
      }
      
      return super.getPropertyValue(obj, propName);
   }   
   
   /**
    * Sets the default Location Scheme to the specified Context.
    * 
    * @param context the Context, assumed not <code>null</code> or empty.
    * @param propName the Site Variable name, assumed not <code>null</code> or
    * empty.
    * @param propValue the new default Location Scheme.
    * 
    * @throws Exception an if error occurs.
    */
   @SuppressWarnings("unchecked")
   private void setDefaultScheme(IPSPublishingContext context, String propName,
         Object propValue) throws Exception
   {
      if (propValue == null || StringUtils.isBlank((String)propValue))
      {
         context.setDefaultSchemeId(null);
         return;
      }

      if (!(propValue instanceof String))
         throw new PSConfigException(
               "The name of the default Location Scheme must be a string. It cannot be type of \""
                     + propValue.getClass().getName() + "\".");
      
      // set the default Location Scheme
      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
      model.setContextId(context.getGUID());
      String schemeName = (String) propValue;
      IPSGuid schemeId = model.nameToGuid(schemeName);
      if (schemeId == null)
      {
         throw new PSConfigException(
               "Failed to set the default Location Scheme for Context \""
                     + context.getName()
                     + "\". This is because the Location Scheme name, \""
                     + schemeName + "\" does not exist within the Context.");
      }
      context.setDefaultSchemeId(schemeId);
   }

   
   /**
    * The name of the default Location Scheme property
    */
   public static final String DEFAULT_SCHEME = "defaultLocationScheme";
}
