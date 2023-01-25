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
package com.percussion.error;

public interface IPSGlobalErrorsMap
{
   
   /**
    * A compilation of Global Errors map. In future we will be having a mechanism
    * of reserving error number range for each subsystem.  
    * <TABLE BORDER="1">
    * <TR><TH>Range</TH><TH>Component</TH></TR>
    * <TR><TD>0     - 21</TD><TD>IPSLoaderErrors interface</TD></TR>
    * <TR><TD>0     - 2</TD><TD>IPSRemoteErrors interface</TD></TR>
    * <TR><TD>1     - 10</TD><TD>IPSJobErrors interface</TD></TR>
    * <TR><TD>1     - 62</TD><TD>IPSDeploymentErrors interface</TD></TR>
    * <TR><TD>100   - 505</TD><TD>IPSHttpErrors interface</TD></TR>
    * <TR><TD>1001  - 2000</TD><TD>IPSBeansErrors interface</TD></TR>
    * <TR><TD>1001  - 2000</TD><TD>IPSServerErrors interface</TD></TR>
    * <TR><TD>1001 - 1400</TD><TD>IPSTableFactoryErrors interface</TD></TR>
    * <TR><TD>1801 - 1850</TD><TD>IPSLocaleErrors interface</TD></TR>
    * <TR><TD>2001  - 3000</TD><TD> IPSObjectStoreErrors interface</TD></TR>
    * <TR><TD>3001  - 3500</TD><TD>IPSConnectionErrors interface</TD></TR>
    * <TR><TD>3501  - 3999</TD><TD>IPSMailErrors interface</TD></TR>
    * <TR><TD>4000  - 4999</TD><TD>IPSCatalogErrors inteface</TD></TR>
    * <TR><TD>5201  - 5400</TD><TD>IPSDataErrors:back-end data processing errors</TD></TR>
    * <TR><TD>5000  - 5999</TD><TD>IPSBackEndErrors interface</TD></TR>
    * <TR><TD>6001  - 7000</TD><TD>IPSDataErrors:XML/general data processing errors</TD></TR>
    * <TR><TD>6001  - 7000</TD><TD>IPSXMLErrors interface</TD></TR>
    * <TR><TD>7001  - 7500</TD><TD>IPSExtensionErrors interface</TD></TR>
    * <TR><TD>9001  - 10000</TD><TD>IPSSecurityErrors interface</TD></TR>
    * <TR><TD>10000 - 11000</TD><TD>IPSUtilErrors interface</TD></TR>
    * <TR><TD>10151 - 10200</TD><TD>IPSServletErrors interface</TD></TR>
    * <TR><TD>13001 - 14000</TD><TD>IPSCmsErrors interface</TD></TR>
    * <TR><TD>14001 - 15000</TD><TD>IPSWebservicesErrors interface</TD></TR>
    * <TR><TD>16001 - 17000</TD><TD>IPSSearchErrors interface, 16301 - 16700:IPSRetrievalWareErrors </TD></TR>
    * <TR><TD>17101 - 17500</TD><TD>IPSContentErrors interface</TD></TR>
    * <TR><TD>17501 - 18000</TD><TD>IPSCloneErrors interface</TD></TR>
    * <TR><TD>20001 - 21000</TD><TD>IPSContentExplorerErrors interface</TD></TR>
    * <TR><TD>70101 - 70500</TD><TD>IPSWebdavErrors interface</TD></TR>
    * <TR><TD>80001 - 80500</TD><TD>IPSPublisherErrors interface</TD></TR>
    * </TABLE>
    */

}
