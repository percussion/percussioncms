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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets the properties of keyword.
 * @author bjoginipally
 *
 */
public class PSKeywordSetter extends PSSimplePropertySetter
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (! (obj instanceof PSKeyword))
         throw new IllegalArgumentException("obj type must be IPSAssemblyTemplate.");
      
      PSKeyword kw = (PSKeyword) obj;
      if (CHOICES_PAIRS.equals(propName))
      {
         setChoices(kw, propValue);
      }
      else
      {
         super.applyProperty(obj, state, aSets, propName, propValue);
      }
      
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (CHOICES_PAIRS.equals(propName))
      {
         PSKeyword kw = (PSKeyword) obj;
         List<PSPair<String, String>> choices = new ArrayList<PSPair<String, String>>();
         for (PSKeywordChoice c : kw.getChoices())
         {
            choices
                  .add(new PSPair<String, String>(c.getLabel(), c.getValue()));
         }
         return choices;
      }
      
      return super.getPropertyValue(obj, propName);
   }
      
   /**
    * Creates choice for each supplied pair and adds the choices to the keyword. 
    * @param kw The object of the keyword assumed not <code>null</code>.
    * @param propValue The choice values object.
    */
   @SuppressWarnings("unchecked")
   private void setChoices(PSKeyword kw, Object propValue)
   {
      if (!(propValue instanceof List))
         throw new PSConfigException("The value type of the " + CHOICES_PAIRS
               + " must be a List");

      List<PSPair<String, String>> choices = 
         (List<PSPair<String, String>>) propValue;
      List<PSKeywordChoice> kwchoices = new ArrayList<PSKeywordChoice>();
      for (int i = 0; i < choices.size(); i++)
      {
         PSPair<String, String> pair = choices.get(i);
         PSKeywordChoice ch = new PSKeywordChoice();
         ch.setLabel(pair.getFirst());
         ch.setValue(pair.getSecond());
         ch.setSequence(i);
         kwchoices.add(ch);
      }
      kw.setChoices(kwchoices);
   }
   
   /**
    * The property name for the keyword choices.
    */
   public static final String CHOICES_PAIRS = "choicePairs";
   
}
