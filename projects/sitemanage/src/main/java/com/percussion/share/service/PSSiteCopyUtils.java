/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.share.service;

import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.share.data.PSMapWrapper;
import com.percussion.share.service.exception.PSValidationException;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Utility class for copy site functionality. And meant to be used for only for this functionality.
 */

public class PSSiteCopyUtils
{   
    
    final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
    
    // Following constants are used to give error messages if any action is performed on a site while it is in the 
    // middle of the copy process
    public static final String SITE_COPY_KEY    = "Site is being Copied";

    public static final String CAN_NOT_DELETE_SITE = "You cannot delete a site while the site is being copied";
    
    public static final String CAN_NOT_COPY_SITE = "You cannot copy a site while the site is being copied";

    public static final String CAN_NOT_UPDATE_SITE_PROPERTIES = "You cannot update properties of a  site while " +
                                                                 "the site is being copied" ;

    public static final String CAN_NOT_CREATE_SAME_COPIED_SITE_NAME = "You cannot create a site with the name of a " +
                                                                      "site that is currently being created.";

    public static final String CAN_NOT_EDIT_SAME_COPIED_SITE_NAME= "You cannot edit a site name with the name " +
                                                                    "of a site that is currently being created." ;
     
    public static final String CAN_NOT_DELETE_FOLDER = "You cannot delete a site folder/section while the site" +
                                                         " is being copied.";
    
    public static final String CAN_NOT_MOVE_FOLDER  = "You cannot move a site folder/page of a site while the site " +
                                                       "is being copied.";
    
    public static final String CAN_NOT_EDIT_FOLDER_NAME = "You cannot edit site name folder while the site is " +
                                                            "being copied.";
    
    public static final String CAN_NOT_UPDATE_FOLDER_PROPERTIES = "@Cannot Edit Site Name Folder";
    
    public static final String CAN_NOT_DELETE_PAGE = "You cannot delete a site page while the site is " +
                                                      "being copied.";
    
    public static final String CAN_NOT_MOVE_SECTION = "You cannot move a section of a site while the " +
                                                      "site is being copied."; 
    
    public static final String CAN_NOT_EDIT_SECTION = "You cannot edit a section name of a site while the " +
                                                         "site is being copied.";
    
    public static volatile String s_sourceSite;
    
    public static volatile String s_targetSite;
    
    public static volatile boolean copyInProgress=false;
    
    public static PSMapWrapper getCopySiteInfo()
    {
        rwl.readLock().lock();
        try 
        {
           PSMapWrapper wrapper = new PSMapWrapper();
           if (copyInProgress)
           {
               Map<String,String> siteInfoMap = wrapper.getEntries();
               siteInfoMap.put(COPY_SOURCE, s_sourceSite);
               siteInfoMap.put(COPY_TARGET, s_targetSite);
               wrapper.setEntries(siteInfoMap);
              
           }
           return wrapper;
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    
    /**
     * Set the map with source and target site names, this gets set when a site
     * copy gets started
     * 
     * @param sourceSite not <code>null</code> or <code>empty</code>
     * @param targetSite not <code>null</code> or <code>empty</code>
     */
    public static void startSiteCopy(String sourceSite, String targetSite) throws PSValidationException {
        boolean canCopy = false;
        rwl.readLock().lock();
        if (!copyInProgress)
        {
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            try
            {
                if (!copyInProgress)
                {
                    s_sourceSite = sourceSite;
                    s_targetSite = targetSite;
                    copyInProgress = true;
                    canCopy = true;
                }
                rwl.readLock().lock();
            }
            finally
            {
                rwl.writeLock().unlock();
            }

        }

        try
        {
            if (!canCopy)
                validateParameters("copy").reject(PSSiteCopyUtils.SITE_COPY_KEY, PSSiteCopyUtils.CAN_NOT_COPY_SITE)
                        .throwIfInvalid();
        }
        finally
        {
            rwl.readLock().unlock();
        }

    }
    
    /**
     * Reset copy site after completion
     */
    public static void clearCopySite()
    {    
        rwl.writeLock().lock();
        try 
        {
            s_sourceSite = null;
            s_targetSite = null;
            copyInProgress = false;
        } finally {
            rwl.writeLock().unlock();
        }
    }
    
    /**
     * @param siteName name of the site not <code>null</code> or <code>empty</code>
     * @param methodName name of the method from where this method  gets called  
     * not <code>null</code> or <code>empty</code>
     * @param errorMessage error message based on what kind of action is being done on a site while getting copied 
     *                      not <code>null</code> or <code>empty</code>
     */
    public static void throwCopySiteMessageIfNotAllowed(String siteName, String methodName, String errorMessage) throws PSValidationException {
        validate(siteName, methodName);
        notEmpty(errorMessage, "Error Message can not be blank");
        rwl.readLock().lock();
        try {
            if (copyInProgress)
            {
                if(s_sourceSite.equals(siteName))
                {
                    validateParameters(methodName).reject(SITE_COPY_KEY, errorMessage).throwIfInvalid();
                }
            } 
        } finally {
            rwl.readLock().unlock();
        }
    }
   
    /**
     * If the passed in site name is same as the site name that is getting created as part of the copy
     * throws an error message
     * @param siteName  not <code>null</code> or <code>empty</code>
     * @param methodName name of the method from where it is getting called.  
     * not <code>null</code> or <code>empty</code>
     * @param errorMessage  not <code>null</code> or <code>empty</code>
     */
   public static void throwCopySiteMessageIfSameTargetName(String siteName, String methodName, String errorMessage) throws PSValidationException {
        validate(siteName, methodName);
        notEmpty(errorMessage, "Error Message can not be blank");
        rwl.readLock().lock();
        try {
            if (copyInProgress)
            {
                if (s_targetSite.equals(siteName))
                {
                    validateParameters(methodName).reject(SITE_COPY_KEY, errorMessage).throwIfInvalid();
                }
            }
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    /**
     * Checks to see if there is any site is under copy process and if so, if the passed in 
     * site name is same as either the site getting copied or the site going to be created 
     * from the copy then throws related message.
     * @param siteName name of the site
     * @param newSiteName  name of the new site name
     * @param methodName name of the method from where it is getting called. 
     */
    public static void throwCopySiteMessageForUpdateError(String siteName, String newSiteName, String methodName) throws PSValidationException {
        validate(siteName, methodName);
        notNull(newSiteName, "New Site Name can not be null");
        notEmpty(newSiteName, "New Site Name can not be empty");
        rwl.readLock().lock();
        try {
            if (copyInProgress)
            {
                String errorMessage = "";
                if(s_sourceSite.equals(siteName))                                                                        
                {
                    errorMessage = CAN_NOT_UPDATE_SITE_PROPERTIES;
                }
                if (s_targetSite.equals(newSiteName)) 
                {
                    errorMessage = CAN_NOT_EDIT_SAME_COPIED_SITE_NAME;
                }
                validateParameters(methodName).reject(SITE_COPY_KEY, errorMessage).throwIfInvalid();
            } 
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    public static boolean isSiteCopyInProgress()
    {
        rwl.readLock().lock();
        try {
            return (copyInProgress);
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    private static void validate(String siteName, String methodName)
    {
        notNull(siteName, "Site Name can not be null");
        notEmpty(siteName, "Site Name can not be empty");
        notNull(methodName, "Method Name can not be null" );
        notEmpty(methodName, "Method Name can not be empty");
    }
   
   private static String  COPY_SOURCE = "Source";
    
    private static String  COPY_TARGET = "Target";
}
