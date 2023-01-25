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

package com.percussion.taxonomy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.lucene.textconverter.IPSLuceneTextConverter;
import com.percussion.taxonomy.domain.Relationship_type;

public class TaxonomyTextConverter implements IPSLuceneTextConverter {

	// TODO we should really be using the taxonomy DAO... originally we didn't think this would run in the spring context
	private String SQL_SIMILAR = "select r.related_node.id from Related_node r where r.node.id in (:nodes) and r.relationship.id = :relationship_type_id";
	private String SQL_GET_VALUES = "select v.Name from Value v where v.node.id in (:ids) and v.lang.id = :language_id";


	private static final Logger log = LogManager.getLogger(TaxonomyTextConverter.class);

	public String getConvertedText(InputStream is, String mimetype)
			throws PSExtensionProcessingException {

		// TODO for now we assume all percussion users are searching in english
		int langID = 1;

		if (is == null) {
			throw new IllegalArgumentException("is must not be null");
		}

		Session session = TaxonomyDBHelper.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		String resultText = "";
		Collection<String> searchTerms = new Vector<String>();
		try {
			String rawIDsAsString = IOUtils.toString(is, "UTF-8");
			log.info("processing input string: " + rawIDsAsString);
			String[] rawIDsAsArray = StringUtils.split(StringUtils.trimToEmpty(rawIDsAsString),",");
			Collection<Integer> ids = new Vector<Integer>();
			for (String id_string : rawIDsAsArray) {
				// strip out spaces because input string will be in format "1 , 2 , 7"
				ids.add(NumberUtils.toInt(StringUtils.trimToEmpty(id_string)));
			}

			// get similar ID for our main query
			Query query = session.createQuery(SQL_SIMILAR);
			query.setParameterList("nodes", ids);
			query.setInteger("relationship_type_id", Relationship_type.SIMILAR);

			// add them
			ids.addAll((Collection<Integer>) query.list());

			// run our main query
			query = session.createQuery(SQL_GET_VALUES);
			query.setInteger("language_id", langID);
			query.setParameterList("ids", ids);
			Collection<String> values = (Collection<String>) query.list();

			// add values to our search string to return
			for (String v : values) {
				log.debug("adding search term:" + v);
				//System.out.println("TaxonomyTextConverterv2 - adding:" + v);
				searchTerms.add(v);
			}
			resultText = StringUtils.join(searchTerms, ' ');
		} catch (IOException e) {
			throw new PSExtensionProcessingException(m_className, e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new PSExtensionProcessingException(m_className, e);
			}

		}

		tx.commit();
		session.close();

		return resultText;
	}

	public void init(IPSExtensionDef def, File codeRoot)
			throws PSExtensionException {
	}

	/**
	 * A member variable to hold the name of this class.
	 */
	private String m_className = getClass().getName();
}
