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
package com.percussion.pso.preview;

import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;
import org.w3c.dom.Document;

import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * A Spring view that returns the XML document specified by the 
 * <code>resultKey</code> field.  
 *
 * @author DavidBenua
 *
 */
public class SimpleXmlView extends AbstractView implements View
{

   private static final Logger log = LogManager.getLogger(SimpleXmlView.class);
   
   private String encoding = "UTF-8"; 
   
   private String resultKey = "result"; 
   /**
    * Default constructor  
    */
   public SimpleXmlView()
   {
     
   }
   /**
    * @see AbstractView#renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)
    */
   @Override
   @SuppressWarnings({ "rawtypes" })
   protected void renderMergedOutputModel(Map model, HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {
      Document result = findResult(model); 
      
      Writer writer = response.getWriter(); 
      response.setContentType(this.getContentType());
      response.setCharacterEncoding(getEncoding()); 
      String content = PSXmlDocumentBuilder.toString(result, PSXmlDocumentBuilder.FLAG_OMIT_DOC_TYPE);
      writer.append(content);
      writer.flush();
   }
   
   /**
    * Find the result object in the model. 
    * @param model
    * @return the result document.
    */
   @SuppressWarnings({ "rawtypes" })
   protected Document findResult(Map model)
   {
      String emsg; 
      Object result = model.get(getResultKey()); 
      if(result == null)
      {
         emsg = "Result object " + getResultKey() + " was not found"; 
         log.error(emsg); 
         throw new RuntimeException(emsg); 
      }
      if(!(result instanceof Document))
      {
         emsg = "Result object " + getResultKey() + " was not an XML Document"; 
         log.error(emsg); 
         throw new RuntimeException(emsg); 
      }
      return (Document) result; 
   }
   /**
    * @return the encoding
    */
   public String getEncoding()
   {
      return encoding;
   }
   /**
    * @param encoding the encoding to set
    */
   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }
   /**
    * @return the resultKey
    */
   public String getResultKey()
   {
      return resultKey;
   }
   /**
    * @param resultKey the resultKey to set
    */
   public void setResultKey(String resultKey)
   {
      this.resultKey = resultKey;
   }
}
