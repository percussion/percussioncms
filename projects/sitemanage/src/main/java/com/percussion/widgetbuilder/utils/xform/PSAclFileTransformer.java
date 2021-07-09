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
package com.percussion.widgetbuilder.utils.xform;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.widgetbuilder.utils.IPSWidgetFileTransformer;
import com.percussion.widgetbuilder.utils.PSWidgetPackageBuilderException;
import com.percussion.widgetbuilder.utils.PSWidgetPackageSpec;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Tranforms an Acl file by replace all guids with newly generated ids.
 * 
 * @author JaySeletz
 *
 */
public class PSAclFileTransformer implements IPSWidgetFileTransformer
{
    
    @Override
    public Reader transformFile(File file, Reader reader, PSWidgetPackageSpec packageSpec) throws PSWidgetPackageBuilderException
    {
        PSAclImpl acl;
        try
        {
            acl = getAclImpl(reader);
            IPSAclService aclService = PSAclServiceLocator.getAclService();
            // aclService.assignNewIds(acl, getAclId(file));
            return aclToReader(acl);
        }
        catch (Exception e)
        {
            throw new PSWidgetPackageBuilderException("Failed to transform Acl definition file", e);
        }
    }

    /**
     * Get a reader to the supplied acl
     * 
     * @param acl
     * 
     * @return The reader
     * 
     * @throws SAXException 
     * @throws IOException 
     */
    private Reader aclToReader(PSAclImpl acl) throws IOException, SAXException
    {
        return new StringReader(acl.toXML());
    }

    /**
     * Get the acl object from the reader
     * 
     * @param reader The reader to read from.
     * 
     * @return The acl object.
     * 
     * @throws SAXException 
     * @throws IOException 
     */
    private PSAclImpl getAclImpl(Reader reader) throws IOException, SAXException
    {
        PSAclImpl acl = new PSAclImpl();
        acl.fromXML(IOUtils.toString(reader));
        return acl;
    }

    @Override
    public boolean handleFile(File file)
    {
        return file.getName().endsWith("aclDef");
    }

    @Override
    public File transformPath(File file, PSWidgetPackageSpec packageSpec)
    {
        IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
        long id = guidMgr.createGuid(PSTypeEnum.ACL).longValue();
        
        String path = file.getPath();
        String prefix = "AclDef-";
        String fullWidgetName = packageSpec.getFullWidgetName();
        
        String leftPart = StringUtils.substringBefore(path, prefix);
        String rightPart = StringUtils.substringAfterLast(path, fullWidgetName);
        
        
        String newKey = prefix + id;
        File newDir = new File(leftPart + newKey);
        File newPath =  new File(newDir, fullWidgetName + rightPart);
        
        packageSpec.getResolverTokenMap().put("ACL_DEPENDENCY_KEY", newKey);
        packageSpec.getResolverTokenMap().put("ACL_DEPENDENCY_ID", String.valueOf(id));
        
        return newPath;
    }


    /**
     * Extract the acl id from the supplied file path
     * 
     * @param file
     * 
     * @return The id
     */
    private long getAclId(File file)
    {
        String dirName = file.getParentFile().getName();
        String prefix = "AclDef-";
        String id = StringUtils.substringAfter(dirName, prefix);
        return Long.valueOf(id);
    }
}
