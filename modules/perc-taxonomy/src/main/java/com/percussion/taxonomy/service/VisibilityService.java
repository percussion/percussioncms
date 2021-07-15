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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.taxonomy.service;

import java.util.Collection;

import org.hibernate.HibernateException;

import com.percussion.taxonomy.domain.Visibility;
import com.percussion.taxonomy.repository.VisibilityDAO;
import com.percussion.taxonomy.repository.VisibilityServiceInf;

public class VisibilityService implements VisibilityServiceInf {

	public VisibilityDAO VisibilityDAO;

	public Collection getAllVisibilities() {
		try {
			return VisibilityDAO.getAllVisibilities();
		} catch (HibernateException e) {
			throw new HibernateException(e);
		}
	}

	public Collection getAllVisibilitiesForTaxonomyId(int taxonomy_id) {
		try {
			return VisibilityDAO.getAllVisibilitiesForTaxonomyId(taxonomy_id);
		} catch (HibernateException e) {
			throw new HibernateException(e);
		}
	}	
		
	public Visibility getVisibility(int id) {
		try {
			return VisibilityDAO.getVisibility(id);
		} catch (HibernateException e) {
			throw new HibernateException(e);
		}
	}

	public void removeVisibility(Visibility Visibility) {
		try {
			VisibilityDAO.removeVisibility(Visibility);
		} catch (HibernateException e) {
			throw new HibernateException(e);
		}
	}

	public void removeVisibilities(Collection<Visibility> Visibilities) {
		try {
			VisibilityDAO.removeVisibilities(Visibilities);
		} catch (HibernateException e) {
			throw new HibernateException(e);
		}
	}

	public void saveVisibility(Visibility Visibility) {
		try {
			VisibilityDAO.saveVisibility(Visibility);
		} catch (HibernateException e) {
			throw new HibernateException(e);
		}
	}

	public void setVisibilityDAO(VisibilityDAO VisibilityDAO) {
		this.VisibilityDAO = VisibilityDAO;
	}
}
