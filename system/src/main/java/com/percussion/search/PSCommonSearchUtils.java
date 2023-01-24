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

package com.percussion.search;

import com.percussion.cms.objectstore.PSSProperty;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import java.text.MessageFormat;

public  class PSCommonSearchUtils {

   /** Advanced Search **/

    /**
     * This is the property name used to transfer the 'Expand query with
     * synonyms' checkbox value.
     */
    public static final String PROP_SYNONYM_EXPANSION = "synonym_expansion";

    /**
     * Constant for the "yes" boolean value.
     */
    public static final String BOOL_YES = "yes";

    /**
     * Constant for the "no" boolean value.
     */
    public static final String BOOL_NO = "no";

    /** Field Editor **/
    public static final String EDITOR_I18N_PREFIX="com.percussion.search.ui.PSSearchFieldEditor;";
    /**
     * Constant for the one external operator we currently support, not exposed
     * to the end user.
     */
    public static final String EXT_OP = "CONCEPT";

    //non-text
    public static String OP_EQUALS =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Equals");
    public static String OP_GREATER_THAN =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX +
                            "@Greater than");
    public static String OP_LESS_THAN =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Less than");
    public static String OP_ON =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@On");
    public static String OP_AFTER =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@After");
    public static String OP_BEFORE =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Before");
    public static String OP_BETWEEN =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Between");

    /* These are used by the inner classes, but you can't have statics in
      inner classes, so we place them here. They are grouped together as
      constants so they can be easily internationalized. */
    public static String OP_STARTS_WITH =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX +
                            "@Starts with");
    public static String OP_CONTAINS =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Contains");
    public static String OP_ENDS_WITH =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Ends with");
    public static String OP_EXACT =
            PSI18NTranslationKeyValues.getInstance().
                    getTranslationValue(EDITOR_I18N_PREFIX + "@Exact");

    /**
     * Search Simple
     */
    public static final String SEARCH_SIMPLE_I18N_PREFIX="com.percussion.search.ui.PSSearchSimplePanel";

    /**
     * Validates that the value supplied for the full text query.
     *
     * @param query The query, may be <code>null</code> or emtpy.
     * @param translator the translator used to internationalize the error
     * message, if <code>null</code> is supplied, <code>PSI18nUtils</code>
     * will be used as translator along with the specified locale.
     * @param locale the locale for which to internationalize the error message,
     * may be <code>null</code> or empty in which case the default locale is
     * used. Ignored if the <code>translator</code> is not <code>null</code>.
     *
     * @return <code>null</code> if the query is valid, otherwise a non-
     * <code>null</code> internationalized error message.
     */
    public static String validateFTSSearchQuery(String query,
                                                PSI18NTranslationKeyValues translator, String locale)
    {
        String msg = null;

        if (query.length() > PSSProperty.VALUE_LENGTH)
        {
            String key = SEARCH_SIMPLE_I18N_PREFIX +
                    "@Search query too long";
            if (translator == null)
            {
                if (locale != null && locale.trim().length() > 0)
                    msg = PSI18nUtils.getString(key, locale);
                else
                    msg = PSI18nUtils.getString(key);
            }
            else
                msg = translator.getTranslationValue(key);

            msg = MessageFormat.format(msg, new Object[] {String.valueOf(
                    PSSProperty.VALUE_LENGTH)});
        }

        return msg;
    }

}
