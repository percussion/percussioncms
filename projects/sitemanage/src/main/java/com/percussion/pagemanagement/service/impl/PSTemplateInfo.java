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

package com.percussion.pagemanagement.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.security.SecureStringUtils;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSExtractHTMLException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Servlet that allows to import or export a given template from the system
 * using a XML file. When is a GET request, attempts to export a template and
 * return it as a XML file. When is a POST request, attempts to save the
 * template associated to the site supplied as parameter.
 * 
 * @author leonardohildt
 * 
 */
public class PSTemplateInfo extends HttpServlet  {
	
private static final long serialVersionUID = 1L;
  
private static final int DEFAULT_BUFFER_SIZE = 20480; // 20KB.

	private static final Logger log = LogManager.getLogger(PSTemplateInfo.class);
	
   public PSTemplateInfo()
   {
	   PSSpringWebApplicationContextUtils.injectDependencies(this);
   }
   
   /**
    * Handles queries for a xml file
	*/
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
       throws ServletException, IOException
   {
       String templateName = "*";
       String templateId = "";
       String pathInfo = req.getPathInfo();
       PSTemplate templateSelected = null;
       
       if(pathInfo != null)
       {
          String[] path = pathInfo.split("/");
          if(path.length > 1)
          {
        	  templateId = path[1];
        	  templateName = path[2];
          }
       }

       try
       {
    	   // Get the selected template
    	   templateSelected = templateService.exportTemplate(templateId, templateName);
    	   // Init servlet response
           resp.reset();
           resp.setBufferSize(DEFAULT_BUFFER_SIZE);
           resp.setContentType("text/xml");
           resp.setHeader("Content-Disposition", "attachment; filename=\"" + SecureStringUtils.stripAllLineBreaks(
           		templateName) + "\"");
           String ret = PSSerializerUtils.marshal(templateSelected);
           if(ret != null)
           		resp.getWriter().write(ret);
           else
           		throw new IOException("Unable to export template");
       }
       catch (Exception ex)
       {
       	log.error(PSExceptionUtils.getMessageForLog(ex));
       	log.debug(PSExceptionUtils.getDebugMessageForLog(ex));
			try{
				resp.sendError(500);
			}catch(IOException e){
				resp.reset();
				resp.setStatus(500);
			}
       }      
   }
   
	  @Override
	  protected void doPost(HttpServletRequest request,
	        HttpServletResponse response) throws ServletException, IOException
	  {
		  String siteId = null;
	      
	      //Get the site Id from the path
	      String pathInfo = request.getPathInfo();
	      if(pathInfo != null)
	      {
	         String[] path = pathInfo.split("/");
	         if(path.length > 1)
	         {
	       	  siteId = path[1];
	         }
	      }
	       
	      PSTemplate templateImported = null;
	      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	      
	      if (isMultipart)
	      {
	    	  try
			  {
			     List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory())
			           .parseRequest(request);
			     for (FileItem item : items)
			     {
			        if (!item.isFormField())
			        {
			           templateImported = importTemplate(siteId, item);
			        }           
			     }

			     if(templateImported != null && templateImported.getName() != null) {
					 // Return the imported template
					 response.getWriter().print(templateImported.getName());
				 }else
				 	throw new IOException("Unexpected error while importing template.");
			   }
			   catch (Exception e)
			   {
				   log.error(PSExceptionUtils.getMessageForLog(e));
				   log.debug(PSExceptionUtils.getDebugMessageForLog(e));
				   try{
					   response.sendError(500);
				   }catch(IOException e1){
					   response.reset();
					   response.setStatus(500);
				   }
			   }
	      }
	   }

	 
	   /**
	    * Create the template from the uploaded file.
	    * @param siteId the id of the site, assumed not <code>null</code>.
	    * @param item the file item, assumed not <code>null</code>.  The input stream of this item will be closed by this
	    * method.
	    * <code>false</code> otherwise.
	    * @return the newly created template, never <code>null</code>.
	    * @throws PSExtractHTMLException if fail to create template due to error on extracting content
	    */
	   private PSTemplate importTemplate(String siteId, FileItem item)
	      throws PSExtractHTMLException
	   {

	      PSTemplate convertedTemplate = new PSTemplate();
	      
	      try(InputStream fileInput = item.getInputStream())
	      {

	    	  //Build a string with the InputStream
	    	  BufferedReader br = new BufferedReader(new InputStreamReader(item.getInputStream()));
	    	  StringBuilder sb = new StringBuilder();
	    	  String line = null;
	    	  while ((line = br.readLine()) != null) {
	    	        sb.append(line).append("\n");
	    	  }
	    	  br.close();
	    	 
	    	  String validStringXml = sb.toString();
	    	  validStringXml = validStringXml.trim().replaceFirst("^([\\W]+)<","<");
	    	  convertedTemplate = PSSerializerUtils.unmarshal(validStringXml, PSTemplate.class);

	    	  //Import the template
	         return  templateService.importTemplate(convertedTemplate,siteId);

	      } catch (PSDataServiceException | IPSPathService.PSPathNotFoundServiceException | IOException e) {
			throw new PSExtractHTMLException(e);
		  }
	   }
   
   public static IPSTemplateService getTemplateService() {
		return templateService;
	}

	public static void setTemplateService(IPSTemplateService templateService) {
		PSTemplateInfo.templateService = templateService;
	} 

	private static IPSTemplateService templateService;

   
}
