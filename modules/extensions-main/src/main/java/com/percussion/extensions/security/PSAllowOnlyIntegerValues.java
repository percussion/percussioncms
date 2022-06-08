package com.percussion.extensions.security;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequestValidationException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

public class PSAllowOnlyIntegerValues implements IPSResultDocumentProcessor, IPSAllowOnlyItemInputValidator{

	private String ms_fullExtensionName;
	
	@Override
	public String validate(String value, String options){
		String ret = "";
	
		if(value == null)
			return ret;

		value = value.trim();
		
		try{
			Long.parseLong(value);
			ret = value;
		}catch(NumberFormatException e){
			ret = "";
		}
		return ret;
	}


	public void preProcessRequest(Object[] params, IPSRequestContext request) throws PSAuthorizationException,
			PSRequestValidationException, PSParameterMismatchException, PSExtensionProcessingException {
		PSExtensionParams ep=null; 
		String paramCSV=null;;
		
		try{
			ep = new PSExtensionParams(params);
			
			paramCSV = ep.getStringParam(0, null, true);
		}catch(PSConversionException e){
			throw new PSParameterMismatchException("Expected a string for parameter 0, got an unknown data type instead.");
		}
	
		if(paramCSV==null)
			throw new PSParameterMismatchException("Missing value for parameter 0, expected a comma seperated list, got an empty list");
	
		String[] htmlParams = paramCSV.split(",");
		for(String s: htmlParams){
			s = s.trim();
			if(!StringUtils.isEmpty(s)){
				
				String v = request.getParameter(s);
				if(v!=null){
					request.setParameter(s, validate(v, null));
				}
			}
		}
		
	}

	@Override
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
		 ms_fullExtensionName = def.getRef().toString();
		
	}

	@Override
	public boolean canModifyStyleSheet() {
		return false;
	}

	@Override
	public Document processResultDocument(Object[] params, IPSRequestContext request, Document resultDoc)
			throws PSParameterMismatchException, PSExtensionProcessingException {
		
		PSExtensionParams ep=null; 
		String paramCSV=null;;
		
		 if(params == null || resultDoc == null)
	         return resultDoc;
		 
	      Element elem = resultDoc.getDocumentElement();
	      if(elem == null)
	         return resultDoc;
	    
	      try{
		      try{
					ep = new PSExtensionParams(params);
					
					paramCSV = ep.getStringParam(0, null, true);
				}catch(PSConversionException e){
					throw new PSParameterMismatchException("Expected a string for parameter 0, got an unknown data type instead.");
				}
		
				if(paramCSV==null)
					throw new PSParameterMismatchException("Missing value for parameter 0, expected a comma seperated list, got an empty list");
			
				String[] htmlParams = paramCSV.split(",");
				for(String s: htmlParams){
					s = s.trim();
					if(!StringUtils.isEmpty(s)){
						
						String v = elem.getAttribute(s);
						if(v!=null){
							
							elem.setAttribute(s, validate(v, null));
						}
					}
				}
	      }catch(Throwable t) //should never happen!
	      {
	         PSConsole.printMsg(ms_fullExtensionName, t);
	      }

	      return resultDoc;
		 
	}
}