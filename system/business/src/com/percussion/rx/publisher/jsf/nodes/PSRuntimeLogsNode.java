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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;

import java.util.List;

/**
 * Represents the all publishing logs node in the runtime tree.
 */
public class PSRuntimeLogsNode extends PSLogNode
{
   /**
    * Default constructor.
    */
   public PSRuntimeLogsNode() 
   {
      super("Publishing Logs", "pub-runtime-all-logs");
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.jsf.nodes.PSLogNode#getStatusLogs()
    */
   @Override
   public List<IPSPubStatus> getStatusLogs()
   {
      IPSPublisherService psvc = PSPublisherServiceLocator
         .getPublisherService();
      return psvc.findAllPubStatus();
   }

   /**
    * Determines if the site column need to be rendered or not.
    * @return <code>true</code> if the site column need to be rendered.
    */
   @Override
   public boolean isShowSiteColumn()
   {
      return true;
   }
   
   @Override
   public String getHelpTopic()
   {
      return "AllPubLogs";
   }
}
