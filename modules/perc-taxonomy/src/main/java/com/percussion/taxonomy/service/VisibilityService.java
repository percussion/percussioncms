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
