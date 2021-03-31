/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.assembler;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.impl.plugin.PSVelocityAssembler;

/*
 * This class provides a validating assembler for Velocity based templates.
 * It uses the JTidy library and configuration options to validate or
 * re-form content based on the supplied tidy properties file. Located in 
 * @author NateChadwick
 */
public class PSValidatingVelocityAssembler extends PSVelocityAssembler
implements
IPSAssembler{

	private IPSExtensionDef m_def = null;
	
		/**
	    * Logger for this class
	    */
	   private static final Log log = LogFactory.getLog(PSValidatingVelocityAssembler.class);
	   
	   
	   /**
	    * Constructor 
	    */
	   public PSValidatingVelocityAssembler()
	   {
	      super();
	   }

	   /**
	    * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#doAssembleSingle(com.percussion.services.assembly.IPSAssemblyItem)
	    */
	   @Override
	   protected IPSAssemblyResult doAssembleSingle(IPSAssemblyItem item) throws Exception
	   {
	      IPSAssemblyResult result = super.doAssembleSingle(item); 
	      log.debug("Validating Velocity Content Assembler"); 
	      return ValidatingContentAssemblerMerge.merge(m_def,result);   
	   }

	 /**
	 * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#doAssembleSingle(com.percussion.services.assembly.IPSAssemblyItem)
	 */
	@Override
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		m_def = arg0.clone();
		super.init(arg0, arg1);
	}

	   
	   
}
