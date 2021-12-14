/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.web;

import com.percussion.pso.imageedit.data.ImageBean;
import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.data.MasterImageMetaData;
import com.percussion.pso.imageedit.data.SimpleImageMetaData;
import com.percussion.pso.imageedit.data.SizedImageMetaData;
import com.percussion.pso.imageedit.data.UserSessionData;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManagerLocator;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import com.percussion.pso.imageedit.services.cache.ImageCacheManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ImageEditorWizard
{
	private static final Logger log = LogManager.getLogger(ImageEditorWizard.class);
	private ImageCacheManager imageCacheManager = null;
	private ImageSizeDefinitionManager imageSizeDefMgr = null; 
	private ImageResizeManager imageResizeMgr = null; 
	private ImageUrlBuilder urlBuilder = null;
	private ImagePersistenceManager imagePersistenceManager = null;
	
	private int maxDisplayHeight = 1500;
	private int maxDisplayWidth = 2000; 
	
	
	public ImageEditorWizard()
	{
		
	}

	public void init() throws Exception
	{
	   if(imageCacheManager == null)
	   {
	      imageCacheManager = ImageCacheManagerLocator.getImageCacheManager();
	   }
	   if(imageSizeDefMgr == null)
	   {
	      imageSizeDefMgr = ImageSizeDefinitionManagerLocator.getImageSizeDefinitionManager();
	   }
	   
	   //these should be initialized from the Spring bean defn. 
	   Validate.notNull(imageResizeMgr);
	   Validate.notNull(urlBuilder); 
	   Validate.notNull(imagePersistenceManager); 
	   
	   log.debug("ImageEditorWizard: ----------------------------  Starting the ImageEditorWizard controller....----------------------------");
       //Most pages possible for the wizard:
       
       int maxSize = 2 + imageSizeDefMgr.getAllImageSizes().size();
       String[] wizardPages = new String[maxSize];
       wizardPages[0] = MAIN_PAGE;
       wizardPages[maxSize - 1] = CONFIRM_PAGE;
       for (int i = 1; i < maxSize - 1; i++)
       {
           wizardPages[i] = SIZE_PAGE;
       }
       
       //setPages(wizardPages);
       
       String outputWp = "";
       for (int j = 0; j < wizardPages.length; j++)
           outputWp += "[" + wizardPages[j] + "]  ";
       
       log.debug("ImageEditorWizard: ----------------------------  Wizard Pages: " + outputWp + "  ----------------------------");
       log.debug("ImageEditorWizard: ----------------------------  Wizard Pages length: " + wizardPages.length + "  ----------------------------");
	}

	@RequestMapping(params = "_finish")
	protected ModelAndView processFinish(final @ModelAttribute("command") Object command,
										 final Errors errors,
										 final ModelMap modelMap,
										 final SessionStatus status)
			throws Exception
	{
	/*	log.debug("processFinish: Save the Rx content item.");
		String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
		log.debug("processFinish: content id is " + contentid);
		String folderid = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
		log.debug("processFinish: folder id is " + folderid); 
		String actionParam = request.getParameter("action");
		log.debug("processFinish: action is " + actionParam); 
		
		Boolean closeWindow = new Boolean(false); 
		
		UserSessionData usd = getUserSessionData(request);
		MasterImageMetaData mimd = usd.getMimd();
		
		String user = RxRequestUtils.getUserName(request);
        log.debug("processFinish: user is " + user);
        String session = RxRequestUtils.getSessionId(request); 
        log.debug("processFinish: session is " + session); 
		
        //remove any empty images (this can happen if the user
        //presses "finish" part way through the process). 
        Map<String,SizedImageMetaData> sized = mimd.getSizedImages();
        mimd.setSizedImages(cleanEmptySizedImages(sized));
        */
		/*
		if (StringUtils.isNotBlank(contentid)) 
		{
			imagePersistenceManager.UpdateImage(mimd, contentid, null);
			log.debug("processFinish: mimd persisted to existing Rx item: " + contentid);
		}
		else
		{
			if (StringUtils.isBlank(folderid))
				folderid = null;
			
			contentid = imagePersistenceManager.CreateImage(mimd, folderid, false);
			log.debug("processFinish: the contentid of the new item is: " + contentid);
		}
		//everything is saved now...
		usd.setDirty(false); 
		String redirectUrl = "imageeditor?sys_contentid=" + contentid + "&sys_folderid=" + folderid; 
		ModelAndView mav = new ModelAndView("results");
		mav.addObject("image",command);
		if(actionParam.equalsIgnoreCase("close"))
		{
		   log.debug("processFinish: closing window"); 
		   redirectUrl = ""; 
		   closeWindow = new Boolean(true); 
		}
		mav.addObject("redirectUrl", redirectUrl);
		mav.addObject("closeWindow", closeWindow);
		mav.addObject("contentid", contentid);
		return mav;
		 */
		 return null;
	}
	
	protected Map<String,SizedImageMetaData> cleanEmptySizedImages(Map<String,SizedImageMetaData> inmap)
	{
	   Map<String,SizedImageMetaData> outmap = new LinkedHashMap<String, SizedImageMetaData>();
	   for(Map.Entry<String,SizedImageMetaData> entry : inmap.entrySet())
	   {
	      String key = entry.getKey();
	      SizedImageMetaData simd = entry.getValue();
	      if(simd.getX() != 0 || simd.getY() != 0 || simd.getHeight() != 0 ||
	         simd.getWidth() != 0 || StringUtils.isNotBlank(simd.getImageKey()))
	      {
	         outmap.put(key, simd); 
	      }
	   }
	   return outmap; 
	}

	@RequestMapping(params = "_cancel")
	protected ModelAndView processCancel(final HttpServletRequest request,
										 final HttpServletResponse response,
										 final SessionStatus status)
			throws Exception
	{
	    String folderid = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
        log.debug("processCancel: folder id is {}", folderid);
        String folderParam = ""; 
        if(StringUtils.isNotBlank(folderid))
        {
           folderParam = "&sys_folderid=" + folderid;
        }

		status.setComplete();
		
		//clear old session data. 
		setUserSessionData(request, null);

		//re-open the image editor in this window. 
		return new ModelAndView(new RedirectView("imageeditor?openImed=true" + folderParam));
	}
	

	protected void onBind(HttpServletRequest request, Object command,
			BindException errors) throws Exception
	{
		/*
		log.debug("onBind: doing onBind...");
		UserSessionData usd = getUserSessionData(request);
		MasterImageMetaData mimd = usd.getMimd();
		
		ImageBean ib = (ImageBean)command;		
		
		if (mimd == null)
		{
			log.error("onBind: mimd attribute is not set in session.");
		}
		else
		{
			int currentPage = getCurrentPage(request);
					
			if (request instanceof MultipartHttpServletRequest)
			{
				MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest)request;
				MultipartFile mpFile = mRequest.getFile(FILE_UPLOAD_FIELD);
				
				String action = request.getParameter("action");
				log.debug("onBind: the action is: " + action);
				
				if (mpFile.isEmpty() && StringUtils.isBlank(action))
				{
					log.debug("onBind: nothing has been uploaded - not storing image...");
				}
				else if (mpFile.isEmpty() && action.equals("cleared"))
				{
					log.debug("onBind: nothing has been uploaded and something has been cleared - clearing stored image if any...");
					mimd.setImageKey(null);
					mimd.setMetaData(null);
					mimd.clearSizedImages(); 
					usd.setDirty(true); //clearing the file always sets dirty.
				}
				else
				{
					log.debug("onBind: the original name of the file being uploaded is: " + mpFile.getOriginalFilename());
					storeImage(mimd, mpFile);
					setupDisplayImage(mimd, usd); 
					usd.setDirty(true); //uploaded files are always dirty.
				}
				
				mimd.setAlt(ib.getAlt());
				mimd.setDescription(ib.getDescription());
				mimd.setDisplayTitle(ib.getDisplayTitle());
				mimd.setSysTitle(ib.getSysTitle());
				
			}
			//set the dirty flag if the form is dirty or the session is already dirty. 
	        usd.setDirty(usd.isDirty() || ib.isDirty()); 
	        log.debug("onBind: The dirty flag is " + usd.isDirty()); 
	        
			log.debug("onBind: There are " + getPageCount(request, command) + " pages. -- currently on page " + getCurrentPage(request));
			
			log.debug("onBind: current Page is " + currentPage);
			//Figure out how many pages you will need to go through
			if (currentPage == 0)
			{
			    String contentId = request.getParameter("sys_contentid");
			    String folderId = request.getParameter("sys_folderid"); 
			    String sessionId = RxRequestUtils.getSessionId(request); 
			    String username = request.getRemoteUser(); 
			    if(StringUtils.isBlank(contentId) && StringUtils.isNotBlank(folderId) 
			          && StringUtils.isNotBlank(ib.getSysTitle()))
			    {
			       boolean bval = imagePersistenceManager.validateSystemTitleUnique(ib.getSysTitle(), folderId ); 
			       if(!bval)
			       {
			          log.info("system title is not unique in folder " + ib.getSysTitle());
			          errors.rejectValue("sysTitle", "title.not.unique", new String[]{ib.getSysTitle()}, "System Title is not unique in the specified folder.");
			       }
			    }
			   
				log.debug("onBind: ib.getSizedImages(): " + ib.getSizedImages());
				setPagesDynamically(request, ib.getSizedImages());
				setupSizedImages(request, ib.getSizedImages(), mimd);
			}
					
			if (currentPage > 0 && currentPage < getPageCount(request, command) - 1)
			{
				log.debug("onBind: dealing with binding data for page " + currentPage);
						
				SizedImageMetaData simd = getCurrentSizedImage(mimd, currentPage - 1);
				ImageSizeDefinition isd = simd.getSizeDefinition();
				if(ib.getX() == 0 && ib.getY() == 0 && ib.getWidth() == 0 && ib.getHeight() == 0)
				{ //ignore this request, it was likely caused by a refresh...
				   log.debug("No crop box data present, skipping this request"); 
				   return; 
				}
				//can't do this on the client side because MSIE cannot detect change events. 
				if(!usd.isDirty())
				{ //not already dirty, so we must check to see if the box has changed
				   if(simd.getX() != ib.getX() || simd.getY() != ib.getY() || simd.getHeight() != ib.getHeight()
				    || simd.getWidth() != ib.getWidth())
				   {
				      usd.setDirty(true); 
				   }
				}
				
				Rectangle cropBox = new Rectangle(ib.getX(), ib.getY(), ib.getWidth(), ib.getHeight());
				double scaleFactor = usd.getScaleFactor(); 
				if(scaleFactor > 1.0)
				{
				   log.debug("scaling crop box " + scaleFactor); 
				   ImageMetaData imd = mimd.getMetaData(); 
				   cropBox = scaledRectangle(cropBox,scaleFactor, new Dimension(imd.getWidth(),imd.getHeight()));
				   log.debug("new crop box is " + cropBox); 
				}
				Dimension size = new Dimension(isd.getWidth(), isd.getHeight());
				
				resizeSimpleImage(simd, mimd.getImageKey(), cropBox, size); 
				simd.setX(ib.getX());
				simd.setY(ib.getY());
				simd.setHeight(ib.getHeight());
				simd.setWidth(ib.getWidth()); 
				
				Boolean constraint = ib.isConstraint();
				log.debug("constraint from ib: " + constraint);
				log.debug("request constraint: " + request.getParameter("constraint"));
				if (constraint == null)
					simd.setConstraint(false);
				else if (constraint.booleanValue())
					simd.setConstraint(constraint);
				
			}
			else
				log.debug("onBind: page is not > 0 or < " + (getPageCount(request, command) - 1));
			
		}
		
		log.debug("onBind: END");
		*/

	}
	
	protected void setupDisplayImage(MasterImageMetaData mimd, UserSessionData usd)
	   throws Exception
	{
	    log.debug("height is {} width is {}", mimd.getMetaData().getHeight(), mimd.getMetaData().getWidth());
        double scalefactor = computeScaleFactor(mimd.getMetaData().getHeight(), 
              mimd.getMetaData().getWidth()); 
        log.debug("Scale Factor is  {}", scalefactor);
        usd.setScaleFactor(scalefactor);
        SimpleImageMetaData displayImage; 
        if(scalefactor > 1.0)
        { //the image must be resized. 
           displayImage = createScaledImage(mimd, scalefactor);
           log.debug("Display image is {}", displayImage);
        }
        else
        {
           displayImage = new SimpleImageMetaData(mimd);
        }
        usd.setDisplayImage(displayImage); 
	}
	protected void resizeSimpleImage(SimpleImageMetaData image, String imageKey, Rectangle cropBox, Dimension size)
	   throws Exception
	{
	   ImageData imageData = imageCacheManager.getImage(imageKey);
	   InputStream is = new ByteArrayInputStream(imageData.getBinary());
	   ImageData imd =  imageResizeMgr.generateImage(is, cropBox, size);
	   String sizedKey = imageCacheManager.addImage(imd);
	   image.setImageKey(sizedKey);
	   image.setHeight(imd.getHeight());
	   image.setWidth(imd.getWidth()); 
	   image.setMetaData(imd); 
	}
	
	protected SimpleImageMetaData createScaledImage(MasterImageMetaData mimd, double scaleFactor)
	   throws Exception
	{
	   ImageMetaData imageData = mimd.getMetaData();
	   SimpleImageMetaData output = new SimpleImageMetaData();
	   long height = Math.round(imageData.getHeight()/scaleFactor);
	   long width = Math.round(imageData.getWidth()/scaleFactor);
	   Dimension dim = new Dimension(new Long(width).intValue(), 
	         new Long(height).intValue()); 
	   resizeSimpleImage(output, mimd.getImageKey(), null, dim);
	   return output; 
	}

    protected Rectangle scaledRectangle(Rectangle rect, double scaleFactor, Dimension imageSize)
    {
       long x = Math.max(Math.round(rect.getX() * scaleFactor),0l);
       long y = Math.max(Math.round(rect.getY()* scaleFactor), 0l);
       long h = Math.min(Math.round(rect.getHeight() * scaleFactor), imageSize.height - y); 
       long w = Math.min(Math.round(rect.getWidth() * scaleFactor), imageSize.width - x) ; 
       Rectangle out = new Rectangle(new Long(x).intValue(),new Long(y).intValue(),
             new Long(w).intValue(),new Long(h).intValue());
       return out;  
    }
	
	public void setupSizedImages(HttpServletRequest request, String sizedImages, MasterImageMetaData mimd)
	{
		Collection<SizedImageMetaData> simds =  mimd.getSizedImages().values(); 
	    
		UserSessionData usd = getUserSessionData(request);
		if(!StringUtils.isBlank(sizedImages))
		{
			log.debug("setupSizedImages: Handling the sized images...");
			//Images have been selected and we didn't have any images stored previously
			String[] sizedImagesArray = sizedImages.split(",");
			log.debug("setupSizedImages: the bean has {} sized image(s) selected", sizedImagesArray.length );
			
			if (simds.size() == 0)
			{
				log.debug("setupSizedImages: no simds were previously defined");
				for (int x = 1; x <= sizedImagesArray.length; x++)
				{
					SizedImageMetaData simd = new SizedImageMetaData();
					ImageSizeDefinition isd = imageSizeDefMgr.getImageSize(sizedImagesArray[x - 1]);
					simd.setSizeDefinition(isd);
					mimd.addSizedImage(simd);
					log.debug("setupSizedImages: adding new image size {}", isd.getLabel());
					usd.setDirty(true); 
				}
				
			}
			else
			//Images have been selected and there were others stored previously
			{
				log.debug("setupSizedImages: simds have been previously defined");
				
				Map<String, SizedImageMetaData> previousSimds = mimd.getSizedImages();
				Map<String, SizedImageMetaData> newSimds = new HashMap<String, SizedImageMetaData>(); 
				
				for (int x = 0; x < sizedImagesArray.length; x++)
				{
			        SizedImageMetaData simd = null;
			         
					String key = sizedImagesArray[x];
					if (previousSimds.containsKey(key))
					{
						simd = previousSimds.get(key);
						log.debug("setupSizedImages: Image of type [{}] previously chosen, re-using existing one", key);
						previousSimds.remove(key); 
					}
					else
					{
						simd = new SizedImageMetaData();
						ImageSizeDefinition isd = imageSizeDefMgr.getImageSize(key);
						simd.setSizeDefinition(isd);
						log.debug("setupSizedImages: Image of type [{}] previously chosen, re-using existing one", key);
						usd.setDirty(true); 
					}
					newSimds.put(simd.getSizeDefinition().getCode(), simd);
				}
				mimd.setSizedImages(newSimds);
				if(previousSimds.size() > 0)
				{  //some SIMDs were "left over", something has changed. 
				   usd.setDirty(true); 
				}
			}

			log.debug("setupSizedImages: Sized Images in MIMD: {}", mimd.getSizedImages().size());
		}
		else 
		{
			if (simds.size() > 0)
			{
				log.debug("setupSizedImages: There were sized images selected, but none have been selected now, so clearing out sized images");
				mimd.setSizedImages(new LinkedHashMap<String,SizedImageMetaData>());
				usd.setDirty(true);
			}
		}
	}


	@RequestMapping(method = RequestMethod.POST)
   protected void postProcessPage(@RequestParam("_page") final int currentPage,
								  final @ModelAttribute("command") Object command,
								  final HttpServletResponse response) throws Exception
   {
   	/*
      UserSessionData usd = getUserSessionData(request);
      MasterImageMetaData mimd = usd.getMimd();
      
      if(page == 0)
      {
         log.debug("postProcessPage: processing main page");
         if(mimd.getMetaData().getSize() == 0L)
         {
            log.info("no image uploaded"); 
            errors.reject("master.image.missing"); 
         }
         if(mimd.getSizedImages().isEmpty())
         {
            log.info("no image sizes selected"); 
            errors.reject("no.image.sizes.selected"); 
         }
      }      */
   }

   /**
    * Gets the target page number.  Overridden to limit the 
    * target page to those pages that actually exist.  If the target 
    * is "off the end", this routine returns the last page, which should
    * be the confirm page. 
    * 
    */
   protected int getTargetPage(HttpServletRequest request, int currentPage)
   {
   	/*
      UserSessionData usd = getUserSessionData(request);
      int pageCount = 2;
      if(usd.getPages() != null)
      {  
         pageCount = usd.getPages().length; 
      }
      log.debug("getTargetPage: page count is " + pageCount); 
      int target = super.getTargetPage(request, currentPage);
      if(target >= pageCount)
      {
         return pageCount - 1; 
      }

      return target; 
     */
	  return -1;
   }

 	protected int getPageCount(HttpServletRequest request, Object command)
	{
		/*
		int pageCount;
		UserSessionData usd = getUserSessionData(request);
		
		if (usd.getPages() != null)
			pageCount = usd.getPages().length;
		else
			pageCount = getPages().length;
		
		log.debug("getPageCount: Returning total number of pages as: " + pageCount);
		return pageCount;

		 */
		return -1;
	}

	public void setPagesDynamically(HttpServletRequest request, String sizedImages)
	{	
		int maxSize = 2;
		String[] wizardPages = new String[maxSize];
		UserSessionData usd = getUserSessionData(request);
		
		if(!StringUtils.isBlank(sizedImages))
		{
			String[] sizedImagesArray = sizedImages.split(",");
			log.debug("setPagesDynamically: the bean has {} sized image(s) selected",sizedImagesArray.length);
			
			maxSize += sizedImagesArray.length;
			log.debug("setPagesDynamically: the max size is: {}", maxSize);
					
			wizardPages = new String[maxSize];
			
			int x;
			wizardPages[0] = MAIN_PAGE;
			for (x = 1; x <= sizedImagesArray.length; x++)
				wizardPages[x] = SIZE_PAGE;

			log.debug("setPagesDynamically: X after the loop is : {}", x);
			wizardPages[x] = CONFIRM_PAGE;
		}
		//if no image sizes are selected 
		else
		{
			wizardPages[0] = MAIN_PAGE;
			wizardPages[1] = CONFIRM_PAGE;
		}
		
		String outputWp = "";
		for (int j = 0; j < wizardPages.length; j++)
			outputWp += "[" + wizardPages[j] + "]  ";
		
		log.debug("setPagesDynamically: Wizard Pages: {}", outputWp);
		
		//Set the pages to the UserSessionData
		usd.setPages(wizardPages);
		
	}
	
	protected void storeImage(MasterImageMetaData mimd, MultipartFile mpFile)
	{
		try
		{
			InputStream imageStream = mpFile.getInputStream();
			log.debug("storeImage: uploaded a multipart file -- filename: {}", FILE_UPLOAD_FIELD);
			
			ImageData imageData = imageResizeMgr.generateImage(imageStream, null, null);
			log.debug("storeImage: file name is: {}", mpFile.getOriginalFilename());
			imageData.setFilename(mpFile.getOriginalFilename());
			
			//Store the image in the cache
			String imageKey = imageCacheManager.addImage(imageData);
			
			//Uploading a new image 
			if (imageKey != mimd.getImageKey())
			{
				//Then clear out the previous sized images and their data
				mimd.clearSizedImages();
			}
			
			//Add the image key and data to the Master Image
			mimd.setImageKey(imageKey);
			mimd.setMetaData(imageData);
		}
		catch(Exception e)
		{
			log.debug("storeImage: An exception was caught in storeImage: No inputStream found because no file was uploaded.", e.fillInStackTrace());
		}
	}

	protected SizedImageMetaData getCurrentSizedImage(MasterImageMetaData mimd, int currPage)
	{
		log.debug("getCurrentSizedImage: creating array of size: " + mimd.getSizedImages().size());
		List<SizedImageMetaData> simds = new ArrayList<SizedImageMetaData>(mimd.getSizedImages().values());
		return simds.get(currPage);
		
	}

	protected String getViewName(HttpServletRequest request, Object command,
			int page)
	{
	   UserSessionData usd = getUserSessionData(request);
	   log.debug("getViewName: got usd: {}", usd);

	   String[] wizardPages = usd.getPages();
	   if (wizardPages == null)
	   {
	      log.debug("getViewName: usd exists but wizardPages is empty.");
	      return MAIN_PAGE;
	   }

	   if (usd.getPages().length > page)
	   {
	      log.debug("getViewName returning page: {}", wizardPages[page]);
	      return wizardPages[page];
	   }
	   log.debug("getViewName: wizardPages[{}] does not have as many entries as the current page number [{}] expected",  wizardPages.length, page);
	   return MAIN_PAGE;

	}

	protected void validatePage(Object command, Errors errors, int page)
	{
		log.debug("validatePage: doing validatePage...");
		ImageBean ib = (ImageBean)command;
		//ImageData im = (ImageData)mimd.getMetaData();
		
		if (page == 0)
		{
			log.debug("validatePage: Dealing with the main page");
			if (StringUtils.isBlank(ib.getSysTitle()))
			{			   
				errors.rejectValue("sysTitle", "blank.field.not.allowed", new String[]{"System Title"}, "System Title can't be blank.");
            }
   	   	   if (StringUtils.isBlank(ib.getDisplayTitle()))
   	   	   {
				errors.rejectValue("displayTitle", "blank.field.not.allowed", new String[]{"Display Title"}, "Display Title can't be blank.");
   	   	   }	
   	   	   ValidateFieldLength(errors, "sysTitle", 255, ib.getSysTitle());
   	   	   ValidateFieldLength(errors, "displayTitle", 512, ib.getDisplayTitle()); 
   	   	   ValidateFieldLength(errors, "description", 1024, ib.getDescription()); 
   	   	   ValidateFieldLength(errors, "alt", 255, ib.getAlt());
   	   	   
   	   	   if(errors.hasErrors())
   	   	      return;
   	   	  
		   log.debug("validatePage: Nothing wrong with main page, continuing");
		   return; 
		}
		log.debug("validatePage: Looking at page {} nothing needs to be validated here (yet)... ",page);
		
	}
	
	protected void ValidateFieldLength(Errors errors, String fieldName, int length, String fieldValue)
	{
	    if(StringUtils.isBlank(fieldValue))
	    { // field is null or empty
	       log.debug("validatePage: field {} is empty", fieldName);
	       return; 
	    }
	    if(fieldValue.trim().length() > length)
	    {
	       errors.rejectValue(fieldName, "field.too.large", new Object[]{fieldName, length}, "field too large");
	    }
	}

	protected Object formBackingObject(HttpServletRequest request)
			throws Exception
	{
		/*
	  Object cmd = "Exists";
		
	  String openImed = request.getParameter("openImed"); 
	  log.debug("formBackingObject: openImed is: " + openImed);
	  
	  if (openImed != null)
	  {
		  if (openImed.equals("true"))
		  {
			  cmd = null;
			  setUserSessionData(request, null);
			  log.debug("formBackingObject: set cmd to null and cleared USD");
		  }
	  }
		
	  //log.debug("in the form backing object with page " + getCurrentPage(request));
	  String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID); 
	  log.debug("formBackingObject: The contentid is: " + contentid);
	  
      if (StringUtils.isBlank(contentid))
      {

		 log.debug("formBackingObject: cmd is: " + cmd);
    	 try
    	 {
   		  	log.debug("formBackingObject: attempting to get command, if it doesn't exist, catch error and return new");
    		 Object command = getCommand(request);
    		 

   		    log.debug("formBackingObject: command exists");
    		 if (cmd == null)
    		 {
    			 log.debug("formBackingObject: opening from Action Menu, returning new");
    			 return new ImageBean();
    		 }
    		 if (command != null)
    			 {
        			 log.debug("formBackingObject: navigating, so using same command");
    				 return command;
    			 }
    		 log.debug("formBackingObject: not coming from Action Menu, but command null? returning new");
    		 return new ImageBean();    			 
    		 }
    	 	
    	 catch (HttpSessionRequiredException hsre)
    	 {
    		 log.debug("formBackingObject: info - Command does not exist yet, need to create it, continuing");
    	 }
    	 
         return new ImageBean(); 	
      }
      //Found a content id, so load the item.
      log.debug("formBackingObject: found contentid [id:" + contentid + "]");
      String user = RxRequestUtils.getUserName(request);
      log.debug("formBackingObject: user is " + user);
      String session = RxRequestUtils.getSessionId(request); 
      log.debug("formBackingObject: session is " + session); 

      if(StringUtils.isNotBlank(contentid) && contentid != null)
      {
         log.debug("formBackingObject: contentid is not blank, going to open the image...");
         OpenImageResult oir = imagePersistenceManager.OpenImage(contentid );
         log.debug("formBackingObject: ItemStatus is  " + oir.getItemStatus());

         MasterImageMetaData mimd  = oir.getMasterImage();
         log.info("formBackingObject: masterimage " + mimd); 

         UserSessionData usd = getUserSessionData(request);
         usd.setMimd(mimd);   
         setupDisplayImage(mimd, usd); 

         ImageBean ib = new ImageBean();
         ib.setSysTitle(mimd.getSysTitle());
         ib.setDescription(mimd.getDescription());
         ib.setAlt(mimd.getAlt());
         ib.setDisplayTitle(mimd.getDisplayTitle());

         String sizedImages = null;
         Iterator<SizedImageMetaData> simds = mimd.getSizedImages().values().iterator();
         while (simds.hasNext())
         {
            SizedImageMetaData simd = simds.next();
            ImageSizeDefinition isd = simd.getSizeDefinition();
            String code = isd.getCode();
            sizedImages += code;
            if (simds.hasNext())
               sizedImages += ",";

            //testing to ensure simds have x,y,w,h
            log.debug("formBackingObject: simd [" + code + "]: x -> " + simd.getX() + " == y -> " + simd.getY());
         }

         ib.setSizedImages(sizedImages);
         setUserSessionData(request, usd);
         setPagesDynamically(request, sizedImages);

         return ib;
      }         
      log.debug("formBackingObject: contentid is blank, so creating a new item / imagebean");
      return new ImageBean();
      */
		return  null;
	}

	protected Map<String, Object> referenceData(HttpServletRequest request, Object command,
			Errors errors, int page) throws Exception
	{
		/*
	    UserSessionData usd = getUserSessionData(request);
		Map<String, Object> referenceData = new HashMap<String,Object>();
		int currentPage = getCurrentPage(request);
		
		log.debug("referenceData: Current page in refData " + currentPage);
		
		referenceData.put("dirtyFlag", usd.isDirty()?"true":"false"); 
		referenceData.put("ImageUrlBuilder", urlBuilder);
		referenceData.put("allSizes", imageSizeDefMgr.getAllImageSizes());  
	    referenceData.put("page", currentPage); 	
	
		MasterImageMetaData mimd = usd.getMimd();
		
		if(usd.getPages() == null)
		{
		   log.debug("referenceData: no pages defined yet");
		   setPagesDynamically(request, null); 
		}
		int pageCount = usd.getPages().length; 
		log.debug("Page count is " + pageCount);
		referenceData.put("pagecount", pageCount);
		
	        
		if (currentPage == 0)
		{
		   String sizedDivStyle = "none";
		   if(StringUtils.isNotBlank(mimd.getImageKey()))
		   {
		      sizedDivStyle = "block";
		   }
		   referenceData.put("sizedDivStyle", sizedDivStyle);
		   referenceData.put("allDisplaySizes", buildAllSizesList(mimd)); 
		   referenceData.put("metadata", buildMasterImageDisplay(mimd)); 		   
		}
		
		if (currentPage > 0 && currentPage < getPageCount(request, command) - 1)
		{   
		    SizedImageMetaData simd = getCurrentSizedImage(mimd, currentPage - 1);
			referenceData.put("SizedImage", simd);
			Map<String,String> cropbox = buildCropBox(simd); 
			referenceData.put("cropbox", cropbox);
			ImageSizeDefinition isd = simd.getSizeDefinition();
			referenceData.put("sizecode", isd.getCode());
            referenceData.put("sizelabel", isd.getLabel());
            referenceData.put("definedHeight", isd.getHeight());
            referenceData.put("definedWidth", isd.getWidth()); 
            
			Map<String,String> displayImage = buildDisplayImage(usd);
			referenceData.put("displayImage", displayImage); 
		
	    	
		}
		else if(currentPage == getPageCount(request,command)-1)
		{  //this is the confirm page. 
	
		   Map<String,String> displayImage = buildDisplayImage(usd);
		   referenceData.put("displayImage", displayImage); 
		   List<Map<String,String>> sizedImagesDisplay = buildAllSizedImagesDisplay(mimd.getSizedImages());
		   referenceData.put("sizedImages", sizedImagesDisplay); 
		}
	    
		return referenceData;

		 */
		return null;
	}
	
	protected List<Map<String,String>> buildAllSizesList(MasterImageMetaData mimd)
	{
	   List<Map<String,String>> allSizes = new ArrayList<Map<String,String>>();
	   Map<String, SizedImageMetaData> sizedImages = mimd.getSizedImages(); 
	   
	   for(ImageSizeDefinition size : imageSizeDefMgr.getAllImageSizes())
	   {
	      Map<String,String> sizeData = new HashMap<String, String>(); 
	      sizeData.put("code", size.getCode());
	      sizeData.put("label", size.getLabel());
	      if(sizedImages.containsKey(size.getCode()))
	      {
	         sizeData.put("checked", "checked");
	      }
	      allSizes.add(sizeData); 
	   }
	   
	   return allSizes;
	}
	
	protected Map<String,String> buildMasterImageDisplay(MasterImageMetaData mimd)
	{
	   ImageMetaData md = mimd.getMetaData(); 
	   Map<String,String> masterData = new HashMap<String, String>(); 
	   masterData.put("filename" , md.getFilename());
	   masterData.put("ext", md.getExt());
	   masterData.put("mimetype", md.getMimeType());
	   masterData.put("size", String.valueOf(md.getSize()));
	   masterData.put("height", String.valueOf(md.getHeight()));
	   masterData.put("width", String.valueOf(md.getWidth())); 
	   String url = this.urlBuilder.buildUrl(mimd.getImageKey()); 
	   masterData.put("url", url); 
	   
	   return masterData;
	}
	protected Map<String, String> buildCropBox(SizedImageMetaData simd)
	{
	   Map<String, String> box = new HashMap<String, String>(); 
	   ImageSizeDefinition size = simd.getSizeDefinition();
	   if(simd.getHeight() > 0 && simd.getWidth() > 0)
	   {
	      Rectangle rect = new Rectangle(simd.getX(), simd.getY(),
	             simd.getWidth(), simd.getHeight()); 
	     
	      box.put("height", String.valueOf(Math.round(rect.getHeight())));
	      box.put("width", String.valueOf(Math.round(rect.getWidth())));
	      box.put("x", String.valueOf(Math.round(rect.getX())));
	      box.put("y", String.valueOf(Math.round(rect.getY())));
	      box.put("constraint", simd.isConstraint()?"1":"0"); 
	   }
	   else
	   {
	      box.put("height", String.valueOf(size.getHeight()));
	      box.put("width", String.valueOf(size.getWidth()));
	      box.put("x", "0");
	      box.put("y", "0");
	      box.put("constraint",(size.getHeight() > 0 && size.getWidth() > 0)?"1":"0"); 
	   }
	   return box;
	}
	
	protected Map<String, String> buildDisplayImage(UserSessionData usd)
	{
	   MasterImageMetaData mimd = usd.getMimd(); 
	   Map<String,String> displayImage = new HashMap<String, String>();
	   displayImage.put("title", mimd.getDisplayTitle()); 
	   displayImage.put("alt", mimd.getAlt());
	   String url; 
	   if(usd.getDisplayImage() != null)
	   {
	      displayImage.put("height", String.valueOf(usd.getDisplayImage().getHeight()));
	      displayImage.put("width", String.valueOf(usd.getDisplayImage().getWidth()));
          url = this.urlBuilder.buildUrl(usd.getDisplayImage().getImageKey()); 
	      displayImage.put("url", url);
	   }
	   else
	   {
	      displayImage.put("height", String.valueOf(mimd.getMetaData().getHeight()));
	      displayImage.put("width", String.valueOf(mimd.getMetaData().getWidth()));	   
	      url = this.urlBuilder.buildUrl(mimd.getImageKey()); 
	      displayImage.put("url", url);
	   }  
	   
	   return displayImage; 
	}
	
	
	protected List<Map<String,String>> buildAllSizedImagesDisplay(Map<String,SizedImageMetaData> sizedImages)
	{
	   List<Map<String,String>> l = new ArrayList<Map<String,String>>(); 
	   for(Map.Entry<String,SizedImageMetaData> entry : sizedImages.entrySet())
	   {
	      SizedImageMetaData simd = entry.getValue();
	      Map<String,String> r = new HashMap<String, String>();
	       r.put("label",simd.getSizeDefinition().getLabel() );
	       if(StringUtils.isNotBlank(simd.getImageKey()))
	       {
	          r.put("url", this.urlBuilder.buildUrl(simd.getImageKey()));
	       }
	       else continue;  //skip images without urls.
	       if(simd.getMetaData() != null)
	       {
	          r.put("height", String.valueOf(simd.getMetaData().getHeight()));
	          r.put("width", String.valueOf(simd.getMetaData().getWidth()));
	       }
	       else continue; //skip images with no metadata. 
	      l.add(r); 
	   }
	   
	   return l; 
	}
	protected double computeScaleFactor(int height, int width)
	{
	   if(height <= maxDisplayHeight && width < maxDisplayWidth)
	   { //image is small enough that we don't need to scale it. 
	      return 1.0;
	   }
	   Double hr = new Double(height) / new Double(maxDisplayHeight);
	   Double wr = new Double(width) / new Double(maxDisplayWidth); 
	   return Math.max(hr.doubleValue(), wr.doubleValue()); 
	}
	
	public UserSessionData getUserSessionData(HttpServletRequest request)
	{
		UserSessionData usd = (UserSessionData)request.getSession().getAttribute("userData");
		if ( usd == null)
		  {
            log.debug("getUserSessionData: creating new USD...");
 
			usd = new UserSessionData();
			MasterImageMetaData mimd = new MasterImageMetaData();
			ImageMetaData imd = new ImageMetaData();
			mimd.setMetaData(imd);
			
			usd.setMimd(mimd);		
			
			request.getSession().setAttribute("userData", usd);
			log.debug("getUserSessionData: setting usd attribute");
		  }
		      
		return usd;
	}
	
	public void setUserSessionData(HttpServletRequest request, UserSessionData usd)
	{
		request.getSession().setAttribute("userData", usd);
	}

	public String getFILE_UPLOAD_FIELD()
	{
		return FILE_UPLOAD_FIELD;
	}

	public void setFILE_UPLOAD_FIELD(String file_upload_field)
	{
		FILE_UPLOAD_FIELD = file_upload_field;
	}

	public String getMAIN_PAGE()
	{
		return MAIN_PAGE;
	}

	public void setMAIN_PAGE(String main_page)
	{
		MAIN_PAGE = main_page;
	}

	public String getCONFIRM_PAGE()
	{
		return CONFIRM_PAGE;
	}

	public void setCONFIRM_PAGE(String confirm_page)
	{
		CONFIRM_PAGE = confirm_page;
	}

	public String getSIZE_PAGE()
	{
		return SIZE_PAGE;
	}

	public void setSIZE_PAGE(String size_page)
	{
		SIZE_PAGE = size_page;
	}

	/**
    * @return the maxDisplayHeight
    */
   public int getMaxDisplayHeight()
   {
      return maxDisplayHeight;
   }

   /**
    * @param maxDisplayHeight the maxDisplayHeight to set
    */
   public void setMaxDisplayHeight(int maxDisplayHeight)
   {
      this.maxDisplayHeight = maxDisplayHeight;
   }

   /**
    * @return the maxDisplayWidth
    */
   public int getMaxDisplayWidth()
   {
      return maxDisplayWidth;
   }

   /**
    * @param maxDisplayWidth the maxDisplayWidth to set
    */
   public void setMaxDisplayWidth(int maxDisplayWidth)
   {
      this.maxDisplayWidth = maxDisplayWidth;
   }

   public ImageUrlBuilder getUrlBuilder()
	{
		return urlBuilder;
	}

	public void setUrlBuilder(ImageUrlBuilder urlBuilder)
	{
		this.urlBuilder = urlBuilder;
	}
	
	private String FILE_UPLOAD_FIELD = "binary";
	private String MAIN_PAGE = "main";
	private String CONFIRM_PAGE = "confirm";
	private String SIZE_PAGE = "sizeimage";

	
	/**
    * @return the imageResizeMgr
    */
   public ImageResizeManager getImageResizeMgr()
   {
      return imageResizeMgr;
   }

   /**
    * @param imageResizeMgr the imageResizeMgr to set
    */
   public void setImageResizeMgr(ImageResizeManager imageResizeMgr)
   {
      this.imageResizeMgr = imageResizeMgr;
   }

   public ImagePersistenceManager getImagePersistenceManager()
	{
		return imagePersistenceManager;
	}

	public void setImagePersistenceManager(
			ImagePersistenceManager imagePersistenceManager)
	{
		this.imagePersistenceManager = imagePersistenceManager;
	}

   /**
    * @param imageCacheManager the imageCacheManager to set
    */
   protected void setImageCacheManager(ImageCacheManager imageCacheManager)
   {
      this.imageCacheManager = imageCacheManager;
   }

   /**
    * @param imageSizeDefMgr the imageSizeDefMgr to set
    */
   protected void setImageSizeDefMgr(ImageSizeDefinitionManager imageSizeDefMgr)
   {
      this.imageSizeDefMgr = imageSizeDefMgr;
   }

}
