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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.i18n;

import com.percussion.i18n.rxlt.PSLocaleRxResourceCopyHandler;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.exceptions.PSORMException;

import java.io.File;
import java.util.Iterator;

/**
 * A singleton class that provides Locale persistence services.  Locales can be
 * restored and saved, and their resource files can be cataloged.
 */
public class PSLocaleManager
{
   /**
    * Gets the singleton instance of this class.  If one has not been 
    * constructed, it is done so at this time.  A reference to this instance
    * should be maintained to avoid gargage colleciton.
    * 
    * @return The instance, never <code>null</code>.
    * 
    * @throws PSLocaleException If there is an error initializing the manager.
    */
   public static PSLocaleManager getInstance() throws PSLocaleException
   {
      if (m_instance == null)
         m_instance = new PSLocaleManager();
         
      return m_instance;
   }

   /**
    * Private ctor to enforce singleton pattern.
    * 
    * @throws PSLocaleException If there is an error initializing the manager.
    */
   private PSLocaleManager() throws PSLocaleException
   {
      try 
      {
         String rootDir = 
            (String) PSRhythmyxInfoLocator.getRhythmyxInfo().getProperty(
               IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
         m_curDir = new File(rootDir).getAbsolutePath();
      }
      catch (Exception e) 
      {
         throw new PSLocaleException(IPSLocaleErrors.LOCALE_MGR_INIT, 
            e.getLocalizedMessage());
      }
   }


   /**
    * Get the locale object represented by the supplied language string.
    * 
    * @param languageString The unique identifier for the locale, may not be
    * <code>null</code> or empty.
    * 
    * @return The specified locale, may be <code>null</code> if the specified 
    * locale cannot be located.
    * 
    * @throws IllegalArgumentException if <code>languageString</code> is 
    * <code>null</code> or empty.
    * @throws PSLocaleException if there are any errors retrieving the locale.
    */
   public PSLocale getLocale(String languageString) throws PSLocaleException 
   {
      if (languageString == null || languageString.trim().length() == 0)
         throw new IllegalArgumentException(
            "languageString may not be null or empty");

      try
      {
         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         PSLocale locale = mgr.findLocaleByLanguageString(languageString);
         if (locale == null)
         {
            throw new PSLocaleException(
                  IPSLocaleErrors.LOCALE_MGR_UNEXPECTED_ERROR,
                  "Cannot find locale: " + languageString);
         }
         return locale;
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new PSLocaleException(
            IPSLocaleErrors.LOCALE_MGR_UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   
   /**
    * Get the locale object represented by the supplied locale id.
    * 
    * @param localeId The unique identifier for the locale
    * 
    * @return The specified locale, may be <code>null</code> if the specified 
    * locale cannot be located.
    * 
    * @throws IllegalArgumentException if <code>languageString</code> is 
    * <code>null</code> or empty.
    * @throws PSLocaleException if there are any errors retrieving the locale.
    */
   public PSLocale getLocaleById(int localeId) throws PSLocaleException 
   {
      if ( localeId < 1 )
         throw new IllegalArgumentException(
            "locale id may not be less than 1");

      try
      {
         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         return mgr.loadLocale(localeId);         
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new PSLocaleException(
            IPSLocaleErrors.LOCALE_MGR_UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   
   /**
    * Gets all locales defined in the repository.
    * 
    * @return An iterator over zero or more <code>PSLocale</code> objects
    * 
    * @throws PSLocaleException if there are any errors retrieving the locales.
    */
   public Iterator<PSLocale> getLocales() throws PSLocaleException 
   {
      try
      {
         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         return mgr.findAllLocales().iterator();         
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new PSLocaleException(
            IPSLocaleErrors.LOCALE_MGR_UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   /**
    * Gets file references to all resource files for the supplied language 
    * string.  
    * 
    * @param languageString The unique identifier of the locale for which 
    * resource files are to be returned, may not be <code>null</code> or empty.
    * 
    * @return An iterator over zero or more <code>File</code> objects, 
    * never <code>null</code>.  Paths are relative to the Rhythmyx root.
    */
   public Iterator getResourceFiles(String languageString)
   {
      if (languageString == null || languageString.trim().length() == 0)
         throw new IllegalArgumentException(
            "languageString may not be null or empty");
      
      PSLocaleRxResourceCopyHandler copyHandler = 
         new PSLocaleRxResourceCopyHandler(m_curDir, languageString);
      
      return copyHandler.getResourceFiles();
   }

   /**
    * Saves the provided locale, overwriting any existing locale with the same 
    * language string.  
    * 
    * @param locale The locale to save, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>locale</code> is 
    * <code>null</code>.
    * @throws PSLocaleException if there are any errors saving the locale.
    */
   public void saveLocale(PSLocale locale) throws PSLocaleException
   {
      if (locale == null)
         throw new IllegalArgumentException("locale may not be null");
         
      try
      {
         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         mgr.saveLocale(locale);
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new PSLocaleException(
            IPSLocaleErrors.LOCALE_MGR_UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (PSORMException e)
      {
         throw new PSLocaleException(
            IPSLocaleErrors.LOCALE_MGR_UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }      
   }
   
   /**
    * Singleton instance of this class, <code>null</code> until the first call
    * to {@link #getInstance()}, never <code>null</code> modified after that.
    */
   private static PSLocaleManager m_instance = null;
   
   /**
    * The current directory, assumed to be the Rhythymyx root, initialized
    * during construction, never <code>null</code> or modified after that.
    */
   private String m_curDir;
}
