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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.lucene.PSSearchUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.bn.BengaliAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ckb.SoraniAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lt.LithuanianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

public class PSLocaleSpecificLuceneAnalyzer implements IPSLuceneAnalyzer {

    private static volatile PSLocaleSpecificLuceneAnalyzer instance = null;

    public static PSLocaleSpecificLuceneAnalyzer getInstance(){
        if (instance == null) {
            synchronized (PSLocaleSpecificLuceneAnalyzer.class) {
                if (instance == null) instance = new PSLocaleSpecificLuceneAnalyzer();
            }
        }

        return instance;
    }

     /**
     * Initializes this extension.
     * <p>
     * Note that the extension will have permission to read
     * and write any files or directories under <CODE>codeRoot</CODE>
     * (recursively). The extension will not have permissions for
     * any other files or directories.
     *
     * @param def      The extension def, which contains configuration
     *                 info and initialization params.
     * @param codeRoot The root directory where this extension
     *                 should install and look for any files relating to itself. The
     *                 subdirectory structure under codeRoot is left up to the
     *                 extension implementation. Must not be <CODE>null</CODE>.
     * @throws PSExtensionException     If the codeRoot does not exist,
     *                                  or is not accessible. Also thrown for any other initialization
     *                                  errors that will prohibit this extension from doing its job
     *                                  correctly, such as invalid or missing properties.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
        getInstance();
    }

    /**
     * Should create an instance of class that implements
     * org.apache.lucene.analysis.Analyzer and return based on the supplied
     * locale.
     *
     * @param language The language string in the form of two letter language
     *                 code hyphen two letter country code. For example en-us. The
     *                 intended caller of this method is {@link PSLuceneAnalyzerFactory}
     * @return An object of the class that implements
     * org.apache.lucene.analysis.Analyzer. May be <code>null</code>.
     * @throws PSExtensionProcessingException if an exception occurs which
     *                                        prevents the proper handling of this request.
     */
    @Override
    public Analyzer getAnalyzer(String language) throws PSExtensionProcessingException {

       Locale loc = PSSearchUtils.getJavaLocale(language);

        switch (loc.getLanguage()) {
            case "ar":
                return new ArabicAnalyzer();
            case "bg":
                return new BulgarianAnalyzer();
            case "bn":
                return new BengaliAnalyzer();
            case "br":
                return new BrazilianAnalyzer();
            case "ca":
                return new CatalanAnalyzer();
            case "zh":
            case "ko":
            case "ja":
                return new CJKAnalyzer();
            case "ku":
                return new SoraniAnalyzer();
            case "cz":
                return new CzechAnalyzer();
            case "da":
                return new DanishAnalyzer();
            case "de":
                return new GermanAnalyzer();
            case "el":
                return new GreekAnalyzer();
            case "es":
                return new SpanishAnalyzer();
            case "eu":
                return new BasqueAnalyzer();
            case "fa":
                return new PersianAnalyzer();
            case "fi":
                return new FinnishAnalyzer();
            case "fr":
                return new FrenchAnalyzer();
            case "ga":
                return new IrishAnalyzer();
            case "gl":
                return new GalicianAnalyzer();
            case "hi":
                return new HindiAnalyzer();
            case "hu":
                return new HungarianAnalyzer();
            case "hy":
                return new ArmenianAnalyzer();
            case "id":
                 return new IndonesianAnalyzer();
            case "it":
                return  new ItalianAnalyzer();
            case "lt":
                return new LithuanianAnalyzer();
            case "lv":
                return new LatvianAnalyzer();
            case "nl":
                return new DutchAnalyzer();
            case "no":
                return new NorwegianAnalyzer();
            case "pt":
                return new PortugueseAnalyzer();
            case "ro":
                return new RomanianAnalyzer();
            case "ru":
                return new RussianAnalyzer();
            case "sv":
                return new SwedishAnalyzer();
            case "th":
                return new ThaiAnalyzer();
            case "tr":
                return new TurkishAnalyzer();
            default:
                return new EnglishAnalyzer();
        }

    }

}
