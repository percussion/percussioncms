/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.transform;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSDataTypeConverter;

/**
 * A field input transform for setting one date field based on another.
 * This works like the standard date adjust, except that it takes its
 * source from a different field.  
 * <p>
 * If the source date is blank (or not specified), then the current date
 * and time are used.  
 *
 * @author davidbenua
 *
 */
public class PSODateAdjust extends PSDefaultExtension
      implements
         IPSFieldInputTransformer
{
   private static Log log = LogFactory.getLog(PSODateAdjust.class);
   private IPSExtensionDef extDef = null; 
   
   /**
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      Date sourceDate = new Date();
      PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(extDef,
            params, request, log);
      String sourceFieldName = helper.getOptionalParameter("sourceFieldName",
            "");
      if (StringUtils.isNotBlank(sourceFieldName))
      {
         String sourceFieldValue = request.getParameter(sourceFieldName);
         if (StringUtils.isNotBlank(sourceFieldValue))
         {
            sourceDate = PSDataTypeConverter
                  .parseStringToDate(sourceFieldValue);
            log.debug("source date is " + sourceDate);
         } else
         {
            log.debug("no source field value found: using current time");
         }
      } else
      {
         log.debug("no source field name, using current time");
      }
      Number years = helper.getOptionalParameterAsNumber(YEARS, "0");
      Number months = helper.getOptionalParameterAsNumber(MONTHS, "0");
      Number days = helper.getOptionalParameterAsNumber(DAYS, "0");
      Number hours = helper.getOptionalParameterAsNumber(HOURS, "0");
      Number mins = helper.getOptionalParameterAsNumber(MINUTES, "0");
      Number secs = helper.getOptionalParameterAsNumber(SECONDS, "0");
      Calendar cal = Calendar.getInstance();
      cal.setTime(sourceDate);
      cal.add(Calendar.YEAR, years.intValue());
      cal.add(Calendar.MONTH, months.intValue());
      cal.add(Calendar.DAY_OF_MONTH, days.intValue());
      cal.add(Calendar.HOUR_OF_DAY, hours.intValue());
      cal.add(Calendar.MINUTE, mins.intValue());
      cal.add(Calendar.SECOND, secs.intValue());
      return new Timestamp(cal.getTime().getTime());
   }


   @Override
   public void init(IPSExtensionDef def, File ifile)
         throws PSExtensionException
   {
      super.init(def, ifile);
      extDef = def; 
   }
    
   public static final String YEARS = "years";
   public static final String MONTHS = "months"; 
   public static final String DAYS = "days";
   public static final String HOURS = "hours";
   public static final String MINUTES = "minutes";
   public static final String SECONDS = "seconds"; 
   
}
