/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.jexl;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

/***
 * Tools for working with date and time.
 * 
 * @author natechadwick
 *
 */
public class PSODateTools extends PSJexlUtilBase implements IPSJexlExpression {


	 /**
	    * Logger for this class
	    */
	   private static final Log log = LogFactory.getLog(PSODateTools.class);


	   public PSODateTools(){}
	   
	   
	   @IPSJexlMethod(description="Formats a date using the SimpleDateFormat. Returns todays date if date_value is null",
		         params={@IPSJexlParam(name="format", description="format string"), 
			   			 @IPSJexlParam(name="date_value", description="the date value")})
		   public String formatDate(String format, Object date_value) throws ParseException
		   {
		   		SimpleDateFormat fmt = new SimpleDateFormat(format);
		   		
		   		if(date_value==null || date_value.toString().equals(""))
		   			date_value= Calendar.getInstance().getTime();
		   
		  		
		   		return fmt.format(date_value);
		   }

	
}
