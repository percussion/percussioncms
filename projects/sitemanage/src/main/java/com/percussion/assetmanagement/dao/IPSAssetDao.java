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
package com.percussion.assetmanagement.dao;

import java.util.Collection;
import java.util.List;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.IPSItemSummary;

/**
 * CRUDs and Asset. This is not a public API.
 * @author adamgent
 */
public interface IPSAssetDao extends IPSGenericDao<PSAsset, String>
{
    public IPSItemSummary addItemToPath(IPSItemSummary item, String folderPath);
    public void removeItemFromPath(IPSItemSummary item, String folderPath);
    
    /**
     * Gets the asset from its identifier, similar with 
     * {@link #find(String)}, except caller can specify the returned asset
     * includes all fields or only the summary properties of the object.
     *  
     * @param id the identifier (primary key) of the asset to get
     * @param isSummary <code>true</code> if load summary properties of the 
     * items, which does not include Clob or Blob type fields; otherwise load 
     * all properties of the items.
     * 
     * @return the asset. It may be <code>null</code> if the asset does not exist.
     * 
     * @throws LoadException if error occurs during the find operation.
     */
    PSAsset find(String id, boolean isSummary) throws LoadException;
    
    /**
     * Finds all assets of the specified type in the specified workflow and state.
     * 
     * @param type the content type of the assets, never blank.
     * @param workflowId 
     * @param stateId set to -1 to include assets in all workflow states.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByTypeAndWf(String type, int workflowId, int stateId);
    
    /**
     * Finds all assets of the specified type with the specified name.
     * 
     * @param type the content type of the assets, never blank.
     * @param name of the assets, never blank.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByTypeAndName(String type, String name);
    
    /**
     * Finds all assets of the specified type
     * 
     * @param type the content type of the assets, never blank.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByType(String type);
    
    /**
     * Turns revision control on for the asset. 
     * For efficiency reasons this will not load the whole asset.
     * @param id not <code>null</code> or empty.
     */
    public void revisionControlOn(String id);
    

    /**
     * Query to return a list of Assets that appear to have Non ADA compliant data.
     * 
     * @return A collection of Assets that are non compliant.
     * @throws PSReportFailedToRunException 
     */
	public List<PSAsset> findAllNonADACompliantImageAssets() throws PSReportFailedToRunException;
	
	/***
	 * Will return a list of all File assets that appear to have invalid Accessibility data.
	 * @return A collection of File assets that are non compliant
	 * @throws PSReportFailedToRunException 
	 */
	public List<PSAsset> findAllNonADACompliantFileAssets() throws PSReportFailedToRunException;
	
	/***
	 * Will return a collection of all File assets
	 * @return A collection of File Assets
	 * @throws PSReportFailedToRunException 
	 */
	public List<PSAsset> findAllFileAssets() throws PSReportFailedToRunException;
	
	/***
	 * Will return a collection of all Image assets
	 * @return A collection of Image Assets
	 * @throws PSReportFailedToRunException 
	 */
	public List<PSAsset> findAllImageAssets() throws PSReportFailedToRunException;
}
