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
package com.percussion.assetmanagement.dao;

import java.util.Collection;
import java.util.List;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;

/**
 * CRUDs and Asset. This is not a public API.
 * @author adamgent
 */
public interface IPSAssetDao extends IPSGenericDao<PSAsset, String>
{
    public IPSItemSummary addItemToPath(IPSItemSummary item, String folderPath) throws PSDataServiceException;
    public void removeItemFromPath(IPSItemSummary item, String folderPath) throws PSDataServiceException;
    
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
    PSAsset find(String id, boolean isSummary) throws PSDataServiceException;
    
    /**
     * Finds all assets of the specified type in the specified workflow and state.
     * 
     * @param type the content type of the assets, never blank.
     * @param workflowId 
     * @param stateId set to -1 to include assets in all workflow states.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByTypeAndWf(String type, int workflowId, int stateId) throws LoadException;
    
    /**
     * Finds all assets of the specified type with the specified name.
     * 
     * @param type the content type of the assets, never blank.
     * @param name of the assets, never blank.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByTypeAndName(String type, String name) throws LoadException;

	/**
	 * Finds all assets that use Encryption and needs republish in case of key rotation.
	 *
	 *
	 * @return collection of assets, never <code>null</code>, may be empty.
	 */
	public Collection<PSAsset> findAllAssetsUsingEncryption() throws LoadException, PSReportFailedToRunException;
    
    /**
     * Finds all assets of the specified type
     * 
     * @param type the content type of the assets, never blank.
     * 
     * @return collection of assets, never <code>null</code>, may be empty.
     */
    public Collection<PSAsset> findByType(String type) throws LoadException;
    
    /**
     * Turns revision control on for the asset. 
     * For efficiency reasons this will not load the whole asset.
     * @param id not <code>null</code> or empty.
     */
    public void revisionControlOn(String id) throws LoadException;
    

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
