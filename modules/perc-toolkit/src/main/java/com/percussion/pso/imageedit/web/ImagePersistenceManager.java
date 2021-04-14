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

import com.percussion.pso.imageedit.data.MasterImageMetaData;
import com.percussion.pso.imageedit.data.OpenImageResult;
import com.percussion.services.content.data.PSItemStatus;

/**
 * The Image Persistence Manager is used for loading and saving 
 * images to/from the Rhythmyx server. 
 * <p>
 * All of these methods MUST be called in the user request thread, and the 
 * user's Rhythmyx session id and user name must be supplied.  These values
 * can be obtained from the <code>RxRequestUtils</code> class in the PSOToolkit. 
 * 
 * 
 * @author DavidBenua
 *
 */
public interface ImagePersistenceManager
{

    /**
     * Opens an existing image. If necessary, the image will be checked out to the current user, and the 
     * edit revision will be returned. 
     * @param contentid the content id for the image
     * @return the image result, which consists of the image metadata and the server item status. 
     * @throws Exception when a server error occurs. 
     */
    public OpenImageResult OpenImage(String contentid) throws Exception;
   
    /**
     * Updates an existing image.  The image must have been obtained from <code>OpenImage</code> or 
     * created with <code>CreateImage</code>.  
     * <p>
     * The item will be restored to the previous state, according to the settings in the item status parameter. 
     * If the item status is missing, the item will be checked in, but no further action will be taken.  
     * @param image the image metadata. Must not be <code>null</code>.
     * @param contentid the content id. Must not be <code>null</code>.
     * @param itemStatus the item status. May be <code>null</code>
     * @throws Exception when a server error occurs. 
     */
    public void UpdateImage(MasterImageMetaData image, String contentid, PSItemStatus itemStatus )
       throws Exception; 
    
    /**
     * Creates a new image in the server. 
     * @param image the image metadata. Must not be <code>null</code>.
    * @param folderid the folder id where the item will be created. If this is <code>null</code>
    * then the item will not be added to any folder. 
    * @param checkin set to <code>true</code> to cause the new item to be checked in. Otherwise the 
     * item will be checked out to the current user.  
     * @return the content id of the newly created image. 
     * @throws Exception when a server error occurs. 
     */
    public String CreateImage(MasterImageMetaData image, String folderid, boolean checkin ) throws Exception;
    
    /**
     * Validates that the sysTitle will be valid in the given folder.
     * @param sysTitle the proposed new title
     * @param folderId the folder id.
      * @return <code>true</code> if the title will be valid, 
     * <code>false</code otherwise. 
     * @throws Exception
     */
    public boolean validateSystemTitleUnique(String sysTitle, String folderId )
    throws Exception; 

}
