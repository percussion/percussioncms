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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.assetmanagement.dao.impl;

import static com.percussion.share.rx.PSLegacyExtensionUtils.addParameters;
import static java.text.MessageFormat.format;
import static org.apache.commons.collections.MapUtils.getInteger;
import static org.apache.commons.collections.MapUtils.getString;
import static org.apache.commons.lang.StringUtils.endsWith;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notEmpty;

import com.percussion.cms.PSCmsException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.rx.PSLegacyExtensionUtils;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renames the asset title if its not unique in the given folder.
 * This extension runs before the item is saved and will only run
 * if a {@link PSPurgableTempFile file} is in the request.
 * <p>
 * The {@link IPSHtmlParameters#SYS_TITLE system title} will be renamed accordingly:
 * <p>
 * <em>Brackets are conditional, braces are not.</em>
 * <pre>
 * {FileNameWithOutExt}[ (Try #)][.FileExt]
 * </pre>
 * <p>
 * An example of three consecutive assets that want to have the name: <code>stuff.jpg</code> in the same folder: 
 * <code>/assets/uploads/images</code>
 * <pre>
 * /assets/uploads/images/stuff.jpg
 * /assets/uploads/images/stuff (1).jpg
 * /assets/uploads/images/stuff (2).jpg
 * </pre>
 * @author adamgent
 *
 */
public class PSAssetRenameItemInputTransformer implements IPSItemInputTransformer
{

    /**
     * Fields that end in this suffix and have the value: <code>true</code>
     * indicate the file is dirty.
     */
    private static final String FILE_FIELD_DIRTY_SUFFIX = "_dirty";
    /**
     * The field from which the sys_title should be derived from.
     */
    private static final String FILE_NAME_FIELD_PARAMETER = "fileNameField";
    /**
     * The maximum number of times that renaming will occur
     * on the current asset.
     * This is a fail safe to avoid potentially infinite renaming loops
     * if the server is incorrectly reporting that the file fails unique
     * sys_title validation.
     */
    private static final int MAX_RENAME_TRIES = 1000;
    private List<String> parameterNames = new ArrayList<String>();
    
    @Override
    public void preProcessRequest(Object[] args, IPSRequestContext request)
    {
        Map<String, String> p = new HashMap<String, String>();
        addParameters(p, parameterNames, args);
        addParameters(p, request);
        Integer contentId = getInteger(p, IPSHtmlParameters.SYS_CONTENTID);
        String title = getString(p, IPSHtmlParameters.SYS_TITLE);
        String fileNameField = getString(p, FILE_NAME_FIELD_PARAMETER, "img_filename");
        String newTitle = getString(p, fileNameField);
        
        /*
         * If the filename field is blank or null we will have
         * to use the system title. 
         */
        if (isBlank(newTitle)) {
            if (log.isDebugEnabled())
                log.debug(format("The file name field:{0} did not have a new title", fileNameField));
            newTitle = title;
        }
        
        Action action = getAction(request);
        if (log.isTraceEnabled()) {
            log.trace("Running rename asset extension: " + p);
        }
        /*
         * We only run on insert and update
         */
        if (action == Action.SKIP ) 
            return;
        
        if ( ! isFileModified(request) ) {
            log.debug("No files were modified. No renaming");
            return;
        }
        
        /*
         * If the new title is blank we will let the sys_title validator fail
         * (the validator runs after this type of extension).
         */
        if (isBlank(newTitle)) return;
        
        /*
         * We remove any bad file name characters that our new title might have
         * to avoid publishing problems.
         */
        newTitle = PSFolderPathUtils.replaceInvalidItemNameCharacters(newTitle);
        /*
         * Make sure the title is unique and correct it if its not.
         */
        newTitle = findUniqueTitle(request, action, newTitle);
        setTitle(request,newTitle);
        
        /*
         * Logging of what we did
         */
        if ( ! newTitle.equals(title) ) {
            if (log.isDebugEnabled())
                log.debug(format("Renaming asset id: {0} with title: {1} to: {2}", 
                        contentId, title, newTitle));
        }
        else {
            if (log.isDebugEnabled())
                log.debug(format("No need to rename asset: {0}, title:{1}", contentId, title));
        }
   
    }
    

    /**
     * Determines if a new file is in the request.
     * @param request never <code>null</code>.
     * @return <code>true</code> if a file is in the request.
     */
    private boolean isFileModified(IPSRequestContext request) 
    {
        List<String> params = PSLegacyExtensionUtils.getParameterNames(request);
        for (String p : params) {
            /*
             * See if there is a field marked dirty.
             */
            if (endsWith(p, FILE_FIELD_DIRTY_SUFFIX) && 
                    "true".equals(request.getParameter(p)))
                return true;
            /*
             * See if there is a non-empty temp file.
             */
            Object obj = request.getParameterObject(p);
            boolean isTempFile = obj != null && obj instanceof PSPurgableTempFile;
            if (isTempFile) {
                PSPurgableTempFile t = (PSPurgableTempFile) obj;
                boolean isNotEmpty = isNotBlank(t.getSourceFileName());
                if ( isNotEmpty ) {
                    return true;
                }
            }
        }
        return false;

    }
    
    /**
     * Determines what kind of content editor request this is:
     * UPDATE, INSERT, or SKIP
     * @param request never <code>null</code>.
     * @return never <code>null</code>.
     */
    private Action getAction(IPSRequestContext request) {
        String actionType = request.getParameter("DBActionType");
        if ("INSERT".equals(actionType)) {
            return Action.INSERT;
        }
        else if ("UPDATE".equals(actionType)) {
            return Action.UPDATE;
        }
        return Action.SKIP;
    }
    

    /**
     * Finds a unique title for the request.
     * 
     * @param request never <code>null</code>.
     * @param action never <code>null</code>.
     * @param newTitle never <code>null</code>.
     * @return never <code>null</code>.
     */
    private String findUniqueTitle(IPSRequestContext request, Action action, String newTitle)
    {
        String originalTitle = newTitle;
  
        /*
         * Loop through trying to find a name that has not been taken.
         */
        for (int i = 0; i < MAX_RENAME_TRIES; i++)
        {
            newTitle = renameTitle(originalTitle, i);
            notEmpty(newTitle, "newTitle failed");
            if ( isValidUniqueName(request, newTitle, action) ) {
                break;
            }
        }
        return newTitle;
    }

    /**
     * Validates if the given new title is unique.
     * @param request never <code>null</code>.
     * @param newTitle never <code>null</code> or empty.
     * @param action never <code>null</code>.
     * @return <code>true</code> if it is unique.
     */
    protected boolean isValidUniqueName(IPSRequestContext request, String newTitle, Action action) {
        /*
         * This method is protected for unit testing purposes.
         */
        String originalTitle = getTitle(request);
        setTitle(request, newTitle);
        String folderId = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
        folderId = folderId == null ? request.getParameter(IPSHtmlParameters.SYS_ASSET_FOLDERID) : folderId;
        String redirectUrl = getRedirectUrl(request);
        if (isNotBlank(folderId) && isBlank(redirectUrl)) {
            setRedirectUrl(request, IPSHtmlParameters.SYS_FOLDERID + '=' + folderId); 
        }
        else {
            log.warn(IPSHtmlParameters.SYS_FOLDERID + " or " + IPSHtmlParameters.SYS_ASSET_FOLDERID + " should be in "
                    + "the request but was not.");
        }
        try
        {
            PSServerFolderProcessor.validateUniqueDepName(request, action == Action.INSERT);
        }
        catch (PSCmsException e)
        {
            /*
             * We continue trying to rename in this case.
             */
            if (log.isTraceEnabled())
                log.trace("Failed to rename asset to: " + newTitle, e);
            return false;
        }
        finally
        {
            /*
             * Set our original title back into the request.
             * The caller will have to put the new title back
             * in the request.
             */
            setTitle(request, originalTitle);
            setRedirectUrl(request, redirectUrl);
        }
        return true;
    }


    private void setRedirectUrl(IPSRequestContext request, String redirectUrl)
    {
        request.setParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL, redirectUrl);
    }


    private String getRedirectUrl(IPSRequestContext request)
    {
        return request.getParameter(IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
    }
    
    /**
     * Represents the  content editor action.
     * @author adamgent
     *
     */
    protected static enum Action {
        INSERT,UPDATE, SKIP;
    }
    
    private String getTitle(IPSRequestContext r) {
        return r.getParameter(IPSHtmlParameters.SYS_TITLE);
    }
    private void setTitle(IPSRequestContext r, String title) {
        r.setParameter(IPSHtmlParameters.SYS_TITLE, title);
    }
    
    private String renameTitle(String title, Integer renameTry) {
        String newTitle = title;
        if (renameTry != 0) 
            newTitle = PSFolderPathUtils.addEnumeration(title, renameTry);
        return newTitle;
    }
    
    @Override
    public void init(IPSExtensionDef def, @SuppressWarnings("unused") File arg1)
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        setParameterNames(PSLegacyExtensionUtils.getParameterNames(def));
    }

    public List<String> getParameterNames()
    {
        return parameterNames;
    }

    public void setParameterNames(List<String> parameterNames)
    {
        this.parameterNames = parameterNames;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSAssetRenameItemInputTransformer.class);

}

