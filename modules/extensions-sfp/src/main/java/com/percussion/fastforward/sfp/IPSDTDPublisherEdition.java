/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.fastforward.sfp;

/**
 * This interface defines all string constants representing the DTD for the
 * remote publisher edition XML document that is sent part of the SOAP request
 * to the publisher client. The DTD and a typical XML document shall be of the
 * following syntax:
 * <P>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
 * &lt;!DOCTYPE psxpub:pubdata[<br>
 * &lt;!ELEMENT psxpub:pubdata (destsite, publisherconfig, contentlist)
 * &gt;<br>
 * &lt;!ATTLIST psxpub:pubdata xmlns:psxpub CDATA #FIXED "urn:www.percussion.
 * com/publisher" &gt;<br>
 * &lt;!ELEMENT destsite (#PCDATA) &gt;<br>
 * &lt;!ATTLIST destsite siteid #REQUIRED&gt;<br>
 * &lt;!ATTLIST destsite name #IMPLIED&gt;<br>
 * &lt;!ATTLIST destsite ipaddress #REQUIRED&gt;<br>
 * &lt;!ATTLIST destsite port #IMPLIED&gt;<br>
 * &lt;!ATTLIST destsite userid #IMPLIED&gt;<br>
 * &lt;!ATTLIST destsite password #IMPLIED&gt;<br>
 * &lt;!ATTLIST destsite root #IMPLIED&gt;<br>
 * &lt;!ELEMENT publisherconfig (param*) &gt;<br>
 * &lt;!ELEMENT param* (#PCDATA) &gt;<br>
 * &lt;!ATTLIST param name #REQUIRED&gt;<br>
 * &lt;!ELEMENT contentlist (contentitem *) &gt;<br>
 * &lt;!ATTLIST contentlist deliverytype #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist context #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist publicationid #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist editionid #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist publisherid #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist clistid #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist pubstatusid #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist pageindex #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentlist islastpage #REQUIRED&gt;<br>
 * &lt;!ELEMENT contentitem* (title, contenturl, delivery, customproperties?)
 * &gt;<br>
 * &lt;!ATTLIST contentitem contentid #REQUIRED&gt;<br>
 * &lt;!ATTLIST contentitem unpublish #IMPLIED&gt;<br>
 * &lt;!ATTLIST contentitem revision #IMPLIED&gt;<br>
 * &lt;!ATTLIST contentitem variantid #REQUIRED&gt;<br>
 * &lt;!ELEMENT title (#PCDATA) &gt;<br>
 * &lt;!ELEMENT contenturl (#PCDATA) &gt;<br>
 * &lt;!ELEMENT delivery (location*) &gt;<br>
 * &lt;!ELEMENT customproperties (customproperty1, customproperty2) &gt;<br>
 * &lt;!ELEMENT customproperty1 (#PCDATA) &gt;<br>
 * &lt;!ELEMENT customproperty2 (#PCDATA) &gt;<br>
 * &lt;!ELEMENT location (#PCDATA) &gt;<br>
 * ]&gt;<br>
 * &lt;!-- sample document --&gt;<br>
 * &lt;psxpub:pubdata xmlns:psxpub="urn:www.percussion.com/publisher"&gt;<br>
 * &lt;destsite siteid="111" name="site1" ipaddress="yy.yyy.yy.yyy" port="27"
 * userid="ftpuser" password="23dfs54g8j" rootdir="wwwroot/testsite"&gt;site
 * description &lt;/destsite&gt;<br>
 * &lt;publisherconfig&gt;<br>
 * &lt;param name="rxserver"&gt;12.345.567.32 &lt;/param&gt;<br>
 * &lt;param name="rxport"&gt;9992 &lt;/param&gt;<br>
 * &lt;param name="rxsslport"&gt;9443 &lt;/param&gt;<br>
 * &lt;param name="statusurl"&gt;/Rhythmyx/rx_pubMain/updatestatus.xml
 * &lt;/param&gt;<br>
 * &lt;param name="filesystem"&gt;com.percussion.cml.publisher.
 * PSFilePublisherHandler &lt;/param&gt;<br>
 * &lt;param name="ftp"&gt;com.percussion.cml.publisher.PSFtpPublisherHandler
 * &lt;/param&gt;<br>
 * &lt;param name="usserid"&gt;cmsuser &lt;/param&gt;<br>
 * &lt;param name="password"&gt;1sgw437yurg &lt;/param&gt;<br>
 * &lt;/publisherconfig&gt;<br>
 * &lt;contentlist clistid="11" context="1" deliverytype="filesystem" publicationid="222"
 * editionid="100" publisherid="333" pubstatusid="403" pageindex="3"
 * islastpage="false" &gt;<br>
 * &lt;contentitem contentid="1" variantid="101"&gt;<br>
 * &lt;title&gt;testtitle &lt;/title&gt;<br>
 * &lt;contenturl&gt;https://www.percussion.com/rhythmyx/index.htm
 * &lt;/contenturl&gt;<br>
 * &lt;delivery&gt;<br>
 * &lt;location&gt;test/test.htm &lt;/location&gt;<br>
 * &lt;/delivery&gt;<br>
 * &lt;/contentitem&gt;<br>
 * &lt;contentitem contentid="2"&gt;<br>
 * &lt;title&gt;testtitle &lt;/title&gt;<br>
 * &lt;contenturl&gt;http://www.microsoft.com/windows/default.asp
 * &lt;/contenturl&gt;<br>
 * &lt;delivery&gt;<br>
 * &lt;location&gt;test/ms.htm &lt;/location&gt;<br>
 * &lt;/delivery&gt;<br>
 * &lt;/contentitem&gt;<br>
 * &lt;/contentlist&gt;<br>
 * &lt;/psxpub:pubdata&gt;<br>
 *
 */
public interface IPSDTDPublisherEdition
{
   /*
    * Element names
    */
   static public final String ELEM_ROOT = "psxpub:pubdata";
   static public final String ELEM_SITE = "destsite";
   static public final String ELEM_CONTENTLIST = "contentlist";
   static public final String ELEM_CONTENTITEM = "contentitem";
   static public final String ELEM_CONTENTTITLE = "title";
   static public final String ELEM_CONTENTURL = "contenturl";
   static public final String ELEM_DELIVERY = "delivery";
   static public final String ELEM_LOCATION = "location";
   static public final String ELEM_MODIFYDATE = "modifydate";
   static public final String ELEM_MODIFYUSER = "modifyuser";
   static public final String ELEM_EXPIREDATE = "expiredate";
   static public final String ELEM_CONTENTTYPE = "contenttype";
   static public final String ELEM_CONFIG = "publisherconfig";
   static public final String ELEM_PARAM = "param";
   static public final String ELEM_CUSTOMPROPERTIES ="customproperties";
   /*
    * Attribute names
    */
   static public final String ATTR_NS = "xmlns:psxpub";
   static public final String ATTR_USERID = "userid";
   static public final String ATTR_PASSWORD = "password";
   static public final String ATTR_IPADDRESS = "ipaddress";
   static public final String ATTR_PORT = "port";
   static public final String ATTR_ROOTDIR = "rootdir";
   static public final String ATTR_NAME = "name";
   static public final String ATTR_EDITIONID = "editionid";
   static public final String ATTR_SRCSITEID = "srcsiteid";
   static public final String ATTR_RECOVERYPUBSTATUSID = "recoverypubstatusid";
   static public final String ATTR_SITEID = "siteid";
   static public final String ATTR_PUBLISHERID = "publisherid";
   static public final String ATTR_PUBLICATIONID = "publicationid";
   static public final String ATTR_PUBSTATUSID = "pubstatusid";
   static public final String ATTR_DELIVERYTYPE = "deliverytype";
   static public final String ATTR_UNPUBLISH = "unpublish";
   static public final String ATTR_CONTENTID = "contentid";
   static public final String ATTR_REVISION = "revision";
   static public final String ATTR_CONTEXT = "context";
   static public final String ATTR_VARIANTID = "variantid";
   static public final String ATTR_CLISTID = "clistid";
   static public final String ATTR_PAGEINDEX = "pageindex";
   static public final String ATTR_ISLASTPAGE = "islastpage";
   public static final String ATTR_ELAPSETIME = "elapsetime";


   /**
    * The name for the attribute holding the publisher user identification,
    * never <code>null</code>.
    */
   static public final String ATTR_PUBUID = "pubuid";

   /**
    * The name for the attribute holding the publisher password, never
    * <code>null</code>.
    */
   static public final String ATTR_PUBPW = "pubpw";

   /**
    * The parameter name for the rhythmyx server name.
    */
   static public final String PARAM_RXSERVER = "rxserver";

   /**
    * The parameter name for the rhythmyx server port.
    */
   static public final String PARAM_RXPORT = "rxport";

   /**
    * The parameter name for the rhythmyx server SSL port.
    */
   static public final String PARAM_RXSSLPORT = "rxsslport";

   /**
    * The parameter name for the rhythmyx server user name.
    */
   static public final String PARAM_USERID = "userid";

   /**
    * The parameter name for the rhythmyx server password.
    */
   static public final String PARAM_PASSWORD = "password";

   /**
    * The parameter name for the publisher user name parameter.
    */
   static public final String PARAM_PUBUID = "pubuid";

   /**
    * The parameter name for the publisher password.
    */
   static public final String PARAM_PUBPW = "pubpw";

   /**
    * The parameter name for the publisher SSL port. Use this name to specify
    * the publisher SSL port in the publisher setup form.
    */
   static public final String PARAM_SSLPORT = "sslport";

   /**
    * The parameter name for the publisher log location. Use this name to
    * specify the log location in the publisher setup form.
    */
   static public final String PARAM_LOG_LOCATION = "loglocation";

   /**
    * The parameter name for the publisher soap request file. Use this name to
    * specify the soap request file in the publisher setup form.
    */
   static public final String PARAM_SOAP_REQUEST = "soaprequest";

   /**
    * The parameter name for the database publisher jdbc context factory used
    * for jndi lookups. Use this name to specify the database publisher jdbc
    * context factory in the publisher setup form.
    */
   static public final String PARAM_JDBC_CONTEXTFACTORY = "jdbccontextfactory";

   /**
    * The parameter name for the database publisher jndi provider url to use.
    * Use this name to specify the database publisher jndi provider url in the
    * publisher setup form.
    */
   static public final String PARAM_JNDI_PROVIDERURL = "jndiproviderurl";


   /**
    * The parameter name for the number of items after which the FTP client will
    * log out of and then log back into a server session. Defaults to
    * Integer.MAX_INT if not found in the database.
    */
   static public final String PARAM_FTP_RELOGIN_ITEMCOUNT =
      "ftpreloginitemcount";

   /**
    * The parameter name for the time (in seconds) that the FTP Publisher
    * client will wait for the complete content list to arrive. The client
    * will time out if this request is not complete in the specified number
    * of seconds. Defaults to 0 if not found in the database.
    */
   static public final String PARAM_SERVER_REQUEST_TIMEOUT =
      "serverrequesttimeout";

   /**
    * The parameter name for the field that indicates whether to use
    * Active mode (false) or Passive mode (true) in FTP Publisher requests.
    * Defaults to false (use Active mode) if not found in the database.
    */
      static public final String PARAM_FTP_USE_PASSIVE_MODE =
      "enablepassivemode";

  /** Parameter name for the time (in seconds) to wait for data to arrive on FTP
    * sockets when publishing. Note that timeouts should be rare,
    * especially since most of the time we're tranmitting data, not
    * receiving it.
    *
    * Defaults to 60 seconds. 0 means no timeout is set.
    *
    * @see net.oroinc.ftp.FtpClient#setDataTimeout
    * @see net.oroinc.net.SocketClient#setSoTimeout
    */
      static public final String PARAM_FTP_RCV_TIMEOUT =
         "ftprcvtimeout";
}
