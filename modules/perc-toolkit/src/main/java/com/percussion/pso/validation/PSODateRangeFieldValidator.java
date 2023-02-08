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
package com.percussion.pso.validation;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;

/**
 * 
 *
 * @author davidbenua
 *
 */
public class PSODateRangeFieldValidator extends PSDefaultExtension
      implements
         IPSFieldValidator
{

   private static final Logger log = LogManager.getLogger(PSODateRangeFieldValidator.class);
   
   private IPSExtensionDef extDef = null; 
   /**
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(extDef, params, request, log);
      String currentField = helper.getRequiredParameter(CURRENT_FIELD); 
      Date currentDate = PSDataTypeConverter.parseStringToDate(currentField); 
      Validate.notNull(currentDate); 
      String sourceField = helper.getOptionalParameter(SOURCE_FIELD, "");
      Date sourceDate = new Date(); 
      if(StringUtils.isNotBlank(sourceField))
      {
         String sourceVal = request.getParameter(sourceField); 
         if(StringUtils.isNotBlank(sourceVal))
         {
            sourceDate = PSDataTypeConverter.parseStringToDate(sourceVal);
            log.debug("Setting source date to {}", sourceDate);
         }
   
      }
      Calendar currentCal = Calendar.getInstance();
      currentCal.setTime(currentDate); 
      log.debug("Current calendar is {}", currentCal.getTime());

      Number minDays = helper.getOptionalParameterAsNumber(MIN_DAYS, "-1"); 
      if(minDays.intValue() >= 0)
      {
         Calendar minCal = Calendar.getInstance();
         minCal.setTime(sourceDate); 
         minCal.add(Calendar.DAY_OF_MONTH, minDays.intValue()); 
         log.debug("Min Cal is {}", minCal.getTime());
         if(currentCal.before(minCal))
         {
            log.debug("Source is before {}", minCal.getTime());
            return Boolean.FALSE;
         }
      }
      Number maxDays = helper.getOptionalParameterAsNumber(MAX_DAYS, "-1"); 
      if(maxDays.intValue() >= 0)
      {
         Calendar maxCal = Calendar.getInstance(); 
         maxCal.setTime(sourceDate);
         maxCal.add(Calendar.DAY_OF_MONTH, maxDays.intValue()); 
         log.debug("Max Cal is {}", maxCal.getTime());
         if(currentCal.after(maxCal))
         {
            log.debug("Source is after {}", maxCal.getTime());
            return Boolean.FALSE;
         }
      }
       
      
      return Boolean.TRUE; //validation succeeds
   }
   
   @Override
   public void init(IPSExtensionDef def, File ifile)
         throws PSExtensionException
   {
      super.init(def, ifile);
      extDef = def; 
   }
   
   public static final String CURRENT_FIELD = "currentField"; 
   public static final String SOURCE_FIELD = "sourceFieldName"; 
   public static final String MIN_DAYS = "minDays";
   public static final String MAX_DAYS = "maxDays"; 
   
   
}
