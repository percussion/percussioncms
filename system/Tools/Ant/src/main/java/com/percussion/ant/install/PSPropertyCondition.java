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
package com.percussion.ant.install;

import com.percussion.install.PSLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * PSPropertyCondition is a condition which
 * performs a comparison of the specified property in a properies file to
 * the actual property value in the properties file.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the typedef:
 *
 *  <code>
 *  &lt;typedef name="propertyCondition"
 *              class="com.percussion.ant.install.PSPropertyCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to perform the comparison.
 *
 *  <code>
 *  &lt;condition property="WEBIMAGEFX_1104"&gt;
 *     &lt;propertyCondition
 *          compareOperator="=="
 *          isCaseSensitive="false"
 *          leftSideValue="1,1,0,4"
 *          propertyFile="rx_resources/webimagefx/sys_webimagefx.properties"
 *          propertyName="cVERSION"/&gt;
 *  &lt;/condition&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSPropertyCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      String installDir = getRootDir();

      if ((installDir == null) || (installDir.trim().length() == 0))
         return false;

      if (!installDir.endsWith(File.separator))
         installDir += File.separator;

      String strPropFile = installDir + propertyFile;
      File propFile = new File(strPropFile);
      if (!propFile.exists())
      {
         PSLogger.logInfo("Property file does not exist : " + strPropFile);
         return false;
      }

      boolean isEqual = false;

      String propValue = null;
      try(FileInputStream in = new FileInputStream(propFile)){
         Properties verProp = new Properties();
         verProp.load(in);
         propValue = verProp.getProperty(propertyName);
         if ((propValue == null) || (propValue.trim().length() == 0))
         {
            if (leftSideValue.trim().length() == 0)
               isEqual = true;
         }
         else
         {
            propValue = propValue.trim();
            leftSideValue = leftSideValue.trim();

            if (m_caseSensitive)
            {
               int res = leftSideValue.compareTo(propValue);

               return isMatch(res);
            }
            else
            {
               int res = leftSideValue.compareToIgnoreCase(propValue);

               return isMatch(res);
            }
         }
      }
      catch (Exception ex)
      {
         PSLogger.logInfo("Exception : " + ex.getLocalizedMessage());
         PSLogger.logInfo(ex);
      }
      return isEqual;
   }

  /***************************************************************
  * Mutators and Accessors
  ***************************************************************/

  /**
   * Returns the path of the property file relative to the Rhythmyx root
   * directory.
   *
   * @return the property file path relative to the installation root directory,
   * never <code>null</code> or empty
   */
  public String getPropertyFile()
  {
      return propertyFile;
  }

  /**
   * Sets the path of the property file relative to the Rhythmyx root
   * directory.
   *
   * @param propertyFile the property file path relative to the installation
   * root directory, may not be <code>null</code> or empty
   *
   * @throws IllegalArgumentException if propertyFile is <code>null</code>
   * or empty
   */
   public void setPropertyFile(String propertyFile)
   {
      if ((propertyFile == null) || (propertyFile.trim().length() == 0))
         throw new IllegalArgumentException(
            "propertyFile may not be null or empty");
      this.propertyFile = propertyFile;
   }

  /**
   * Returns the key of the property whose value is to be verified.
   *
   * @return the key of the property whose value is to be verified,
   * never <code>null</code> or empty
   */
  public String getPropertyName()
  {
      return propertyName;
  }

  /**
   * Sets the key of the property whose value is to be verified.
   *
   * @param propertyName the key of the property whose value is to be verified,
   * may not be <code>null</code> or empty
   *
   * @throws IllegalArgumentException if propertyName is <code>null</code>
   * or empty
   */
   public void setPropertyName(String propertyName)
   {
      if ((propertyName == null) || (propertyName.trim().length() == 0))
         throw new IllegalArgumentException(
            "propertyName may not be null or empty");
      this.propertyName = propertyName;
   }

  /**
   * Returns the value of the property which should be compared to the actual
   * value in the properties file.
   *
   * @return the value of the property which should be compared to the actual
   * value in the properties file, never <code>null</code>, may be empty
   */
   public String getLeftSideValue()
   {
      return leftSideValue;
   }

  /**
   * Sets the value of the property which should be compared to the actual
   * value in the properties file.
   *
   * @param propertyValue the value of the property which should be compared to
   * the actual value in the properties file,* may be <code>null</code> or empty,
   * if <code>null</code> then set to empty
   */
   public void setLeftSideValue(String propertyValue)
   {
      if (propertyValue == null)
         propertyValue = "";
      this.leftSideValue = propertyValue;
   }

   /**
    * Returns whether the comparison of the specified property value and the
    * actual value in the property file should be case-sensitive or not.
    *
    * @return <code>true</code> if the comparison of the specified property
    * value and the actual value in the property file should be case-sensitive,
    * otherwise <code>false</code>.
    */
   public boolean getIsCaseSensitive()
   {
      return m_caseSensitive;
   }

   /**
    *
    * @param b
    * @return
    */
   public void setIsCaseSensitive(boolean b)
   {
      m_caseSensitive = b;
   }

   /**
    * Sets whether the comparison of the specified property value and the
    * actual value in the property file should be case-sensitive or not.
    *
    * @param isCaseSensitive <code>true</code> if the comparison of the
    * specified property value and the actual value in the property file should
    * be case-sensitive, otherwise <code>false</code>.
    */
   public void getIsCaseSensitive(boolean isCaseSensitive)
   {
      this.m_caseSensitive = isCaseSensitive;
   }

  /***************************************************************
  * Bean properties
  ***************************************************************/

  /**
   * The path of the properties file relative to the root
   * installation directory. The actual value will be set through the
   * Installshield UI. May not be <code>null</code> or empty.
   */
   private String propertyFile = "someversion.properties";

   /**
    * Key of the property whose value is to be verified. The actual value
    * will be set through Installshield UI. May not be <code>null</code>
    * or empty.
    */
   private String propertyName = "cVERSION";

   /**
    * Value of the property which should be compared to the actual value in the
    * properties file. The actual value will be set through Installshield UI.
    * Never <code>null</code>, may be empty.
    */
   private String leftSideValue = "3,1,0,7";

   /**
    * Whether the comparison of the specified property value and the actual
    * value in the property file should be case-sensitive or not. If
    * <code>true</code> the comparision is case-sensitive, otherwise not.
    */
   private boolean m_caseSensitive;

   /**
    * Getter for the allowed ops.
    * @return
    */
   public String[] getSupportedOperators()
   {
      return m_compareOperators;
   }

   /**
    * Getter for the ip.
    * @return
    */
   public String getCompareOperator()
   {
      return m_compareOp;
   }

   /**
    * Sets only compare op. Validated against the allowed set.
    * @param op
    */
   public void setCompareOperator(String op)
   {
      if (op==null || op.trim().length()==0)
         m_compareOp = m_compareOperators[0];

      boolean isValid = false;
      for (int i = 0; i < m_compareOperators.length; i++)
      {
         if (op.equalsIgnoreCase(m_compareOperators[i]))
         {
            isValid = true;
            break;
         }
      }

      if (!isValid)
         m_compareOp = m_compareOperators[0];
      else
         m_compareOp = op;
   }

   /**
    * Is Match?.
    * @param equalsResult
    * @return
    */
   private boolean isMatch(int equalsResult)
   {

      if (m_compareOp.equalsIgnoreCase("==") && equalsResult==0)
         return true;
      if (m_compareOp.equalsIgnoreCase("!=") && equalsResult!=0)
         return true;
      if (m_compareOp.equalsIgnoreCase("<") && equalsResult < 0)
         return true;
      if (m_compareOp.equalsIgnoreCase(">") && equalsResult > 0)
         return true;
      if (m_compareOp.equalsIgnoreCase("<=") && (equalsResult < 0 || equalsResult == 0))
         return true;
      if (m_compareOp.equalsIgnoreCase(">=") && (equalsResult > 0 || equalsResult == 0))
         return true;

      return false;
   }

   /**
    * Comp. Op.
    */
   private String m_compareOp = "==";

   /**
    * Compare operators to chose from.
    */
   private static final String[] m_compareOperators =
   { "==", "!=",
     "<", "<=",
     ">=" };

}
