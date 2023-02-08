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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
import com.percussion.services.error.PSNotFoundException;
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
   protected Object getPropertyValue(Object obj, String propName) throws PSNotFoundException {
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
