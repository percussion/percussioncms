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

package com.percussion.workflow;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Transition
{

   public Transition(Element elemTransition)
   {
      m_ElemTransition = elemTransition;
   }

   public String getID()
   {
      return m_ElemTransition.getAttribute("id");
   }

   public String getLink()
   {
      return m_ElemTransition.getAttribute("link");
   }

   public String getLabel()
   {
      return m_ElemTransition.getAttribute("label");
   }

   public String getTrigger()
   {
      return m_ElemTransition.getAttribute("trigger");
   }

   public String getFrom()
   {
      return m_ElemTransition.getAttribute("from");
   }

   public String getTo()
   {
      return m_ElemTransition.getAttribute("to");
   }

   public String getType()
   {
      return m_ElemTransition.getAttribute("type");
   }

   private String makeGif(String gifName)
   {
      return gifName + "_" + getType() + ".gif";
   }

   public Element makeElement(Element elemTransitions, HashMap statesMap, int height)
   {
      int indexFrom = ((State)statesMap.get(getFrom())).getIndex();
      int indexTo = ((State)statesMap.get(getTo())).getIndex();
      boolean bForward = (indexTo>indexFrom)?true:false;
      int indexBegin = indexFrom;
      int indexEnd = indexTo;
      if(!bForward)
      {
         indexBegin = indexTo;
         indexEnd = indexFrom;
      }

      Document doc = elemTransitions.getOwnerDocument();
      Element elemTransition = (Element) doc.createElement("transition");
      elemTransition.setAttribute("label", getLabel());
      elemTransition.setAttribute("link", getLink());
      elemTransition.setAttribute("type", getType());
      Object [] keys = statesMap.keySet().toArray();
      State state = null;
      String key = "";
      Element elem = null;
      int last = keys.length -1;
      int midWidth = Math.round(20*PreviewWorkflow.scale);
      int ii;
      for(int i=0; i<=last; i++)
      {
         key = keys[i].toString();
         state = (State) statesMap.get(key);
         ii=i;//state.getIndex();
         if(0 == ii)
         {
            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(BLANK));
            elem.setAttribute("width", Integer.toString((state.getWidth()-midWidth)/2));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);
         }

         if(ii == indexBegin && ii == indexEnd) //self transition
         {
            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(SELF));
            elem.setAttribute("width", Integer.toString(midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.setAttribute("xloc", Integer.toString(state.getWidth()*ii+state.getWidth()*1/2 + midWidth));
            elemTransition.appendChild(elem);

            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(BLANK));
            if(ii == last)
               elem.setAttribute("width", Integer.toString((state.getWidth()-midWidth)/2));
            else
               elem.setAttribute("width", Integer.toString(state.getWidth()-midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);
         }
         else if(ii == indexBegin) //beginning of transition
         {
            elem = doc.createElement("draw");
            if(bForward)
               elem.setAttribute("image", makeGif(VLINE));
            else
            {
               elem.setAttribute("image", makeGif(LARROW));
               elemTransition.setAttribute(
                  "xloc",

                  Integer.toString(state.getWidth()*ii+state.getWidth()*3/4));
            }

            elem.setAttribute("width", Integer.toString(midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);

            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(LINE));
            
            if(ii == last)
               elem.setAttribute(
                  "width", Integer.toString((state.getWidth()-midWidth)/2));
            else
               elem.setAttribute(
                  "width", Integer.toString(state.getWidth()-midWidth));

            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);
         }
         else if(ii == indexEnd) //end of the transition
         {
            elem = doc.createElement("draw");
            if(bForward)
            {
               elem.setAttribute("image", makeGif(RARROW));
               elemTransition.setAttribute(
                  "xloc", Integer.toString(state.getWidth()*ii));
            }
            else
               elem.setAttribute("image", makeGif(VLINE));

            elem.setAttribute("width", Integer.toString(midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);

            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(BLANK));
            if(ii == last)
               elem.setAttribute(
                  "width", Integer.toString((state.getWidth()-midWidth)/2));
            else
               elem.setAttribute(
                  "width", Integer.toString(state.getWidth()-midWidth));

            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);
         }
         else if(ii < indexBegin || ii > indexEnd) // no transition
         {
            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(VLINE));
            elem.setAttribute("width", Integer.toString(midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);

            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(BLANK));
            if(ii == last)
               elem.setAttribute(
                  "width", Integer.toString((state.getWidth()-midWidth)/2));
            else
               elem.setAttribute(
                  "width", Integer.toString(state.getWidth()-midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);
         }
         else if(ii > indexBegin && ii < indexEnd) //middle of the transition
         {
            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(MLINE));
            elem.setAttribute("width", Integer.toString(midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);

            elem = doc.createElement("draw");
            elem.setAttribute("image", makeGif(LINE));
            elem.setAttribute(
               "width", Integer.toString(state.getWidth()-midWidth));
            elem.setAttribute("height", Integer.toString(height));
            elemTransition.appendChild(elem);
         }
      }
      return elemTransition;
  }
  Element m_ElemTransition = null;

  static final String LINE = "line";
  static final String MLINE = "mline";
  static final String BLANK = "blank";
  static final String VLINE = "vline";
  static final String SELF = "self";
  static final String RARROW = "rarrow";
  static final String LARROW = "larrow";

}
