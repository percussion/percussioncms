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
