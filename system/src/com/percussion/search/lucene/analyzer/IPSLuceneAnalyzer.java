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
package com.percussion.search.lucene.analyzer;

import com.percussion.extension.IPSExtension;
import com.percussion.extension.PSExtensionProcessingException;

import org.apache.lucene.analysis.Analyzer;

/**
 * Lucene search engine analyzes content before indexing it. Analyzer is
 * responsible for building a TokenStream. Lucene comes with several analyzers
 * from contributors. This interface is meant to provide a way of adding
 * analyzers for languages for which the analyzers are not provided by Rhythmy
 * and override out of the box analyzers for a particular language. Implementers
 * of this class are responsible for creating thread safe Analyzer for the given
 * language. This class should be treated as a factory and the implementers
 * should create appropriate analyzers based on the language instead of
 * implementing this interface for each language separately. Extensions
 * implementing this interface are strictly meant for getting analyzers while
 * indexing and searching the content and not useful anywhere else and needs to
 * be avoided.
 */
public interface IPSLuceneAnalyzer extends IPSExtension
{
   /**
    * Should create an instance of class that implements
    * org.apache.lucene.analysis.Analyzer and return based on the supplied
    * locale.
    * 
    * @param language The language string in the form of two letter language
    *           code hyphen two letter country code. For example en-us. The
    *           intended caller of this method is {@link PSLuceneAnalyzerFactory}
    * @return An object of the class that implements
    *         org.apache.lucene.analysis.Analyzer. May be <code>null</code>.
    * @throws PSExtensionProcessingException if an exception occurs which
    *            prevents the proper handling of this request.
    */
   public Analyzer getAnalyzer(String language)
         throws PSExtensionProcessingException;
}
