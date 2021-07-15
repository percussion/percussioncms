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
package com.percussion.content;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.content.data.AssetDef;
import com.percussion.content.data.AssetDef.Field;


public class PSAssetGenerator extends PSItemGenerator<PSAssetServiceRestClient>
{
    /**
     * Stores the directory that should be used for any href's in an asset def that is relative.
     * May be <code>null</code>.
     */
    private File hrefBase;
    
    /**
     * Used to guaranteed unique names for local assets. 
     */
    private static long ms_lastUsedIdCounter = 1;
    
    public PSAssetGenerator(String baseUrl, String uid, String pw)
    {
        this(baseUrl, uid, pw, null);
    }
    
    /**
     * See
     * {@link PSItemGenerator#PSItemGenerator(Class, String, String, String)
     * base class} for param details.
     * 
     * @param hrefBase If provided, any hrefs in the asset def will be
     * interpreted relative to this directory.
     */
    public PSAssetGenerator(String baseUrl, String uid, String pw, File hrefBase)
    {
        super(PSAssetServiceRestClient.class, baseUrl, uid, pw);
        this.hrefBase = hrefBase;
    }
    
    /**
     * 
     * @return The remote client used by this class to generate assets. Never <code>null</code>.
     */
    public PSAssetServiceRestClient getAssetClient()
    {
        return getRestClient();
    }
    
    /**
     * Create a local asset.
     * 
     * @param def Never <code>null</code>.
     * @return Never <code>null</code>.
     */
    public PSAsset createAsset(AssetDef def)
    {
        if (def == null)
            throw new IllegalArgumentException("def cannot be null");
        
        def.setName("LocalContent - " + getNextId());
        return doCreateAsset(def, null);
    }
    
    /**
     * Id generator wrapped so it can be synchronized.
     * 
     * @return A positive number that is unique within the lifetime of this class.
     */
    private synchronized long getNextId()
    {
        return ++ms_lastUsedIdCounter; 
    }

    /**
     * Create a shared asset.
     * 
     * @param def Never <code>null</code>.
     * @param path The directory the asset will be saved to, relative to
     * /Assets, trailing / not a problem, will be stripped.
     * @return Never <code>null</code>.
     * @throws RuntimeException If the content comes from an href and a problem
     * occurs.
     */
    public PSAsset createAsset(AssetDef def, String path)
    {
        if (def == null)
            throw new IllegalArgumentException("def cannot be null");
        
        if (path.endsWith("/"))
            path = path.substring(0, path.length()-1);
        log.info("Creating asset '/Assets" + path + "/" + def.getName() + "' ...");
        String sysPath = "//Folders/$System$/Assets" + path;

        PSAsset result = doCreateAsset(def, sysPath);
        transitionToState(result.getId(), def.getTargetStateName());
        return result;
    }
    
    private PSAsset doCreateAsset(AssetDef def, String path)
    {
        PSAsset asset = new PSAsset();
        Map<String, Object> fieldSet = asset.getFields();
        fieldSet.put("sys_title", def.getName());
        asset.setType(def.getContentType());
        
        for (Field field : def.getField())
        {
            String href = field.getHref();
            if (href == null || href.trim().isEmpty())
                fieldSet.put(field.getName(), field.getValue() == null ? "" : field.getValue().toString());
            else
            {
                File f = new File(href);
                if (!f.isAbsolute() && hrefBase != null)
                    f = new File(hrefBase, href);
                
                if (!f.exists()) {
                    throw new RuntimeException("File not found for field " + field.getName() + ": " 
                            + f.getAbsolutePath());
                }
                byte[] encoded;
                ByteArrayOutputStream raw = new ByteArrayOutputStream();
                try {
                    IOUtils.copy(new FileInputStream(f), raw);
                    encoded = Base64.encodeBase64(raw.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fieldSet.put(field.getName(), new String(encoded));
            }
        }
        
        if (path != null)
            asset.setFolderPaths(asList(path));
        PSAsset result = getRestClient().save(asset);
        log.info("Created asset " + result.getId());
        return result;
    }
}
