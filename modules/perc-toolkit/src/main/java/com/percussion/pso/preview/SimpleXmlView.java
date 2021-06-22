/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
