/*******************************************************************************
 * (c) 2005-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.validation;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   
   private static Log log = LogFactory.getLog(PSODateRangeFieldValidator.class);
   
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
            log.debug("Setting source date to " + sourceDate); 
         }
   
      }
      Calendar currentCal = Calendar.getInstance();
      currentCal.setTime(currentDate); 
      log.debug("Current calendar is " + currentCal.getTime()); 

      Number minDays = helper.getOptionalParameterAsNumber(MIN_DAYS, "-1"); 
      if(minDays.intValue() >= 0)
      {
         Calendar minCal = Calendar.getInstance();
         minCal.setTime(sourceDate); 
         minCal.add(Calendar.DAY_OF_MONTH, minDays.intValue()); 
         log.debug("Min Cal is "  + minCal.getTime()); 
         if(currentCal.before(minCal))
         {
            log.debug("Source is before "  + minCal.getTime());
            return new Boolean(false); 
         }
      }
      Number maxDays = helper.getOptionalParameterAsNumber(MAX_DAYS, "-1"); 
      if(maxDays.intValue() >= 0)
      {
         Calendar maxCal = Calendar.getInstance(); 
         maxCal.setTime(sourceDate);
         maxCal.add(Calendar.DAY_OF_MONTH, maxDays.intValue()); 
         log.debug("Max Cal is "  + maxCal.getTime()); 
         if(currentCal.after(maxCal))
         {
            log.debug("Source is after " + maxCal.getTime()); 
            return new Boolean(false); 
         }
      }
       
      
      return new Boolean(true); //validation succeeds
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
