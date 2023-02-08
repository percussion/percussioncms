
Personalization Delivery Side Installation and Configuration
_____________________________________________________________

.. sectnum::
.. contents:: Table of Contents


Descripition
============

The Personalization Server is a light weight J2EE Web Application contained
in a Java Servlet Container. The preferred and supported servlet container is Tomcat 6.0.29.
The following steps will assume that you are installing with Tomcat. 


Dependencies
============
 
=================== ===============================================================================================
Dependency          Description
=================== ===============================================================================================
soln-p13n.war       The WAR file that is the personalization Web Application.
soln-p13n-ds.zip    SQL scripts and Installation Documentation.
Apache Tomcat       You will need to download Apache Tomcat 6.0.29 http://tomcat.apache.org/download-60.cgi#6.0.29
JDBC Jars           All supported JDBC drivers are in ../resource/jdbc, except derby (which is embedded in soln-p13n.war).
=================== ===============================================================================================


Quick Start Installation
========================

*This setup is not recommended for production but is useful for quickly getting up and running.*
These steps are abbreviated. See the normal installation_ for a more detailed guide.

#. Download Tomcat see http://tomcat.apache.org/download-60.cgi#6.0.29
#. Install Tomcat see http://tomcat.apache.org/tomcat-6.0-doc/setup.html
#. Unzip ``soln-p13n-ds.zip``
#. Copy the ``deliveryDB`` directory to ``$CATALINA_HOME``
#. Modify ``soln.p13n.xml``, see `Embedded Derby Configuration`_  for detail.
#. Copy soln-p13n.war to ``$CATALINA_HOME/webapps``
#. Start Tomcat
#. Navigate to ``http://localhost:8080/soln-p13n/delivery/deliver``
#. Install Personalization CM System Components if you they are not already installed.
#. If you installed Tomcat on the same machine as Percussion CM System (Rhythmyx) then publishing should work with out configuration changes. 

.. _Embedded Derby Configuration: src-sql/readme.htm#configure-for-embedded-derby

Installation
=============

.. _installation:


Install Dependencies
---------------------

1. Install Tomcat 6.0.29 http://tomcat.apache.org/download-60.cgi#6.0.29
2. Download appropriate JDBC drivers for you database. 
   
   This is not required if using the embedded derby database. 
   You may use JDBC drivers in ``resource/jdbc`` for MS SQLServer or Oracle.
   However, you must download JDBC driver for MySQL.


Setup Tomcat
-------------

As a documentation convention the location of where you install Tomcat will be referenced
as ``$CATALINA_HOME`` in the rest of this document per Apache Tomcat Conventions.
 
Logging
........

Its recommended but not required that you setup Log4j logging for Tomcat. See `Use log4j in Tomcat 6`_ for detail.

.. _Use log4j in Tomcat 6: resource/use-log4j-in-tomcat6/readme.htm

Database Drivers
.................

If you are using MySQL, Microsoft SQL or Oracle you will want to copy the database drivers
to ``$CATALINA_HOME/lib``. For testing purpose, you may use JDBS drivers in ``resource/jdbc``.
However, you must get (or download) the JDBC driver for MySQL database yourself (due to license issues).

Configuring Personalization
----------------------------

.. _configuration:

Personalization is configured through Java properties.

Properties are retrieved from the following locations (in order of precedence):

1. System Properties passed throught the command line. You can do this by setting the JAVA_OPTS environment variable before starting tomcat::

	Example (windows): SET JAVA_OPTS='-DpropName=propValue' 
	Example (UNIX): export JAVA_OPTS='-DpropName=propValue'

2. Default properties file: ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``.
	
3. Servlet Context Parameters

   a. Deployment Parameters ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``

      Refer to `Database Configuration`_ for detail. **Must comment out** (or remove) related properties in ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``
   
   b. Application Parameters (soln.p13n.war/WEB-INF/web.xml). *It is recommended that you do not put your configuration here*.

.. _Database Configuration: src-sql/readme.htm

Personalization Configuration Properties
.........................................

The configuration properties need to be modified based on the type of database used by ``soln-p13n`` web application. 
See `Database Configuration Properties`_ in detail.

.. _Database Configuration Properties: src-sql/readme.htm#database-configation-properties


Create Database Schema
-----------------------

The derby database is included in ``soln-p13n-ds.zip``. 
All database schema can be manually created with the provided SQL scripts 
in ``Solution-P13N-Ds/src-sql/*.DB_TYPE.sql`` where DB_TYPE is your RDBMS, 
or it can be created by Apache Ant script. 

Refer to `Database Configuration`_ for detail.


Deploy soln-p13n.war
---------------------

You can deploy ``soln-p13n.war`` **only after you have**:

#. Installed Tomcat
#. Configured Personalization
#. Created the Database schema


To Deploy ``soln-p13n.war`` to Tomcat you should:

#. Shutdown Tomcat if its running.
#. Copy ``soln-p13n.war`` to ``$CATALINA_HOME/webapps``
#. Start Tomcat.

Validate Deployment of ``soln-p13n.war``

#. Assume you have configured with log4j; otherwise refer to `Use log4j in Tomcat 6`_

#. View the log file ``$CATALINA_HOME/logs/tomcat.log``.

   Make sure there is no error or exception. 
   If there is any error or exception, it may caused by your database configuration, 
   fix the configuration and restart Tomcat, ...etc.


Adjust Personalization Publishing Configuration
-----------------------------------------------
Assuming ``RHYTHMYX_HOME`` is the location of your Rhythmyx installation,
edit the file ``RHYTHMYX_HOME/AppServer/server/rx/deploy/rxapp.ear/rxapp.war/WEB-INF/config/user/spring/p13n-pub-beans.xml``.

Look for two XML properties with name ``wsdlDocumentUrl``.
::

	    <property name="wsdlDocumentUrl">
	      <value>http://localhost:8080/soln-p13n/xfire/DeliveryDataService?WSDL</value>
	    </property>
	    
Replace ``localhost:8080`` with your the hostname and port of your personalization server (Tomcat).
So if you Personalization Server's hostname is *p13n* and the server is running on port *8090* you would change
the xml to::

	    <property name="wsdlDocumentUrl">
	      <value>http://p13n:8090/soln-p13n/xfire/DeliveryDataService?WSDL</value>
	    </property>
	    
Restart Rhythmyx once you make the changes.


Setting Tomcat with an existing Web Server
-------------------------------------------

If your site uses IIS or Apache you will need to hook in tomcat. You may refer to `IIS-Tomcat`_ and/or `Tomcat Connector`_.
The goal is to make all URLS with the pattern ``http://www.yoursite.com/soln-p13n/*`` handled by Tomcat.

.. _IIS-Tomcat: resource/IIS-Tomcat/readme.htm
.. _Tomcat Connector: http://tomcat.apache.org/connectors-doc/index.html.