/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.legacy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSIdGenerator;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;

/**
 * Allocates the next number in a sequence. 
 *
 * @author davidbenua
 *
 */
public class PSONextNumberUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSONextNumberUDF.class);

   /**
    * 
    */
   public PSONextNumberUDF()
   {
      super();
   }
   /**
    * Generates the next key in a sequence.  Calls the internal PSUtils 
    * method for allocating a block based on the name supplied in 
    * <code>params[0]</code>.  
    * @param params the parameter array
    * @param request the callers request context
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      String keyName = PSUtils.getParameter(params, 0);
      if(StringUtils.isBlank(keyName))
      {
         String emsg = "Key name must be supplied";
         throw new IllegalArgumentException(emsg);
      }
      
      try
      {
         return new Integer(PSIdGenerator.getNextId(keyName));
      } catch (SQLException ex)
      {
         String emsg = "Database Error Allocating ID"; 
         log.error(emsg, ex);
         throw new PSConversionException(0, emsg); 
      } 
      
     
   }
}
