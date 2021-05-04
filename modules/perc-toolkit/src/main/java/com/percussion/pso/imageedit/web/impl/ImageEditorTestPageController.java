/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.web.impl;

import com.percussion.pso.imageedit.data.MasterImageMetaData;
import com.percussion.pso.imageedit.data.OpenImageResult;
import com.percussion.pso.imageedit.web.ImagePersistenceManager;
import com.percussion.pso.imageedit.web.ImageUrlBuilder;
import com.percussion.pso.utils.RxRequestUtils;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImageEditorTestPageController extends ParameterizableViewController
      implements
         Controller
{
   private static Log log = LogFactory.getLog(ImageEditorTestPageController.class); 
   
   private ImagePersistenceManager imagePersistenceManager;
   
   private ImageUrlBuilder urlBuilder; 
   /**
    * Default constructor
    */
   public ImageEditorTestPageController()
   {
    
   }
   /**
    * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(HttpServletRequest, HttpServletResponse)
    */
   @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {
      //get the view name
      ModelAndView mav = super.handleRequestInternal(request, response); 
      String user = RxRequestUtils.getUserName(request);
      log.debug("user is " + user);
      String session = RxRequestUtils.getSessionId(request); 
      log.debug("session is " + session); 
      String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID); 
      if(StringUtils.isNotBlank(contentid))
      {
         OpenImageResult oir = imagePersistenceManager.OpenImage(contentid );
         log.debug("ItemStatus is  " + oir.getItemStatus());

         MasterImageMetaData masterImage  = oir.getMasterImage();
         log.info("masterimage " + masterImage); 
         mav.addObject("masterImage", masterImage);
         mav.addObject("urlBuilder", urlBuilder);
      }
      
      return mav; 
   }
   /**
    * @return the imagePersistenceManager
    */
   public ImagePersistenceManager getImagePersistenceManager()
   {
      return imagePersistenceManager;
   }
   /**
    * @param imagePersistenceManager the imagePersistenceManager to set
    */
   public void setImagePersistenceManager(
         ImagePersistenceManager imagePersistenceManager)
   {
      this.imagePersistenceManager = imagePersistenceManager;
   }
   /**
    * @return the urlBuilder
    */
   public ImageUrlBuilder getUrlBuilder()
   {
      return urlBuilder;
   }
   /**
    * @param urlBuilder the urlBuilder to set
    */
   public void setUrlBuilder(ImageUrlBuilder urlBuilder)
   {
      this.urlBuilder = urlBuilder;
   }
}
