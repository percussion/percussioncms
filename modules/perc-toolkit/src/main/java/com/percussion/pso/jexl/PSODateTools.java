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
package com.percussion.pso.jexl;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	   private static final Logger log = LogManager.getLogger(PSODateTools.class);


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
