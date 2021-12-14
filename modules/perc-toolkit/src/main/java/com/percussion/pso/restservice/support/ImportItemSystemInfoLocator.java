/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.support;

import com.percussion.services.PSBaseServiceLocator;

/**
 */
public class ImportItemSystemInfoLocator extends PSBaseServiceLocator {
	
	   /**
	    * Gets the PSO Workflow Action Service bean. 
	   
	    * @return the PSO Workflow Action Service bean.  */
	   public static IImportItemSystemInfo getImportItemSystemInfo()
	   {
	      return (IImportItemSystemInfo) PSBaseServiceLocator.getBean(IMPORT_SYSTEM_INFO_BEAN); 
	   }
	   
	   /**
	    * Field IMPORT_SYSTEM_INFO_BEAN.
	    * (value is ""psoImportSystemInfo"")
	    */
	   public static final String IMPORT_SYSTEM_INFO_BEAN = "psoImportSystemInfo";
	}
