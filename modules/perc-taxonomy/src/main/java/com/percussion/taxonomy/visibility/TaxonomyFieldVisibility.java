/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.taxonomy.visibility;

import java.io.File;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private Log log = LogFactory.getLog(TaxonomyFieldVisibility.class);

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
