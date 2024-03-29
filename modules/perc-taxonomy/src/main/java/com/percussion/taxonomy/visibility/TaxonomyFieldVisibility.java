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

package com.percussion.taxonomy.visibility;

import java.io.File;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldVisibilityRule;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.taxonomy.TaxonomyDBHelper;

public class TaxonomyFieldVisibility implements IPSFieldVisibilityRule {

	
	// TODO we should really be using the taxonomy DAO... originally we didn't think this would run in the spring context
	private String SQL_STRING = "select v.community_id from Visibility v where v.taxonomy.id = :taxonomy_id";

	private static final Logger log = LogManager.getLogger(TaxonomyFieldVisibility.class);

	public Object processUdf(Object[] params, IPSRequestContext ipsRequestContext) throws PSConversionException {

		boolean ret = true;
		int taxonomy_id = -1;
		
		// get community_id of current session
		long community_id = ipsRequestContext.getSecurityToken().getCommunityId();
		
		// parse param for taxonomy_id
		if ((params == null) || (params.length < 1)){
			log.warn("Missing Param taxonomy_id for TaxonomyFieldVisibility");
		}else if (params.length > 1){
			log.warn("Too many Params for TaxonomyFieldVisibility");
		}else {
			taxonomy_id = Integer.parseInt("" + params[0]);
			if (taxonomy_id <= 0){
				log.warn("Invalid taxonomy_id param value for TaxonomyFieldVisibility");
			}
		}
		
		log.debug("community_id:" + community_id);
		log.debug("taxonomy_id:" + taxonomy_id);
		
		// start DB sesssion
		Session session = TaxonomyDBHelper.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		
		// get similar ID for our main query
		Query query = session.createQuery(SQL_STRING);
		query.setInteger("taxonomy_id", taxonomy_id);

		// add them
		Collection<Long> visible_to_community_ids = (Collection<Long>) query.list();

		log.debug("visible_to_community_ids.size:" + visible_to_community_ids.size());
		
		// if no community visibility mappings are in place we assume that no restrictions are in place
		if (visible_to_community_ids.size() > 0){
			// there are community visibility restrictions in place so we will only return true if db contains the mapping
			ret = visible_to_community_ids.contains(community_id);
		}

		tx.commit();
		session.close();

		return ret;
	}
	
	public void init(IPSExtensionDef arg0, File arg1) throws PSExtensionException {
		// hopefully we didn't need to do anything here :-)
		
	}
	
	/**
	 * A member variable to hold the name of this class.
	 */
	private String m_className = getClass().getName();

}
