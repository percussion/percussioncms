
Personalization Delivery Side Database Configuration
_____________________________________________________________

.. sectnum::
.. contents:: Table of Contents


Description
============

The documentation describes how to **configure** and **create schema** for various databases that is used by soln_p13n.war web application.

The default properties is define in ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``, which contains properties for embedded derby database. 
However, you may use context parameter to configure ``soln-p13n.war`` for other databases. Refer to `Tomcat Configuration`_ for detail.
The context parameter for ``soln-p13n.war`` is defined in ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``.

For the parameters defined in ``soln-p13n.xml``, you **must comment out** (or remove) related properties in ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``, such as properties defined in `Database Configation Properties`_.

After configured the desired database, use browser to navigate to ``http://localhost:8080/soln-p13n/delivery/deliver``, which will automatically create other related tables in the database.

Steps below assumed you have done the following:

1. Unzip ``soln-p13n-ds.zip``
2. Open the created directory ``Solution-P13N-Ds``

.. _Tomcat Configuration: http://tomcat.apache.org/tomcat-6.0-doc/config/context.html

Dependencies
============
 
=================== ===============================================================================================
Dependency          Description
=================== ===============================================================================================
soln-p13n.war       The WAR file that is the personalization Web Application.
soln-p13n-ds.zip    SQL scripts and Installation Documentation.
Apache Tomcat       You will need to download Apache Tomcat 6.0.29 http://tomcat.apache.org/download-60.cgi#6.0.29
JDBC Jars           All supported JDBC drivers are in ../resource/jdbc, except derby (which is embedded in soln-p13n.war). Note, mysql driver is not included (due to license issues).
=================== ===============================================================================================

Database Configation Properties
===============================

==============================  =====================
Property Name                   Property Description
==============================  =====================
soln.p13n.jdbc.sql              The type of database, the supported databases are ``mssql``, ``mysql``, ``oracle`` and ``derby``
soln.p13n.jdbc.driverClassName  The full java class name of the JDBC driver.
soln.p13n.jdbc.url              The database URL that is used to connect to the database.
soln.p13n.jdbc.username         The user name of the database. It may be blank for embedded derby database.
soln.p13n.jdbc.password         The password of the database. It may be blank for embedded derby database.
soln.p13n.hibernate.dialect     The hibernate dialect, used by hibernate configuration.
==============================  =====================

Configure for MS SQL Server
===========================

#. Create default visitor & profile tables with the SQL statement in ``visitor_profile.mssql.sql``
#. Copy ``../resource/jdbc/jtds.jar``  to ``$CATALINA_HOME/lib``
#. Copy ``soln-p13n.mssql.xml`` to ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``
#. Modify above ``soln-p13n.xml`` as needed, such as server name, database name, password, ...etc.
#. Modify ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``, to comment out properties defined in `Database Configation Properties`_.

Configure for Oracle
====================

#. Create default visitor & profile tables with the SQL statement in ``visitor_profile.oracle.sql``
#. Copy ``../resource/jdbc/ojdbc14.jar`` to ``$CATALINA_HOME/lib``
#. Copy ``soln-p13n.oracle.xml`` to ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``
#. Modify above ``soln-p13n.xml`` as needed, such as server name, database name, password, ...etc.
#. Modify ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``, to comment out properties defined in `Database Configation Properties`_.

Configure for MySQL Server
===========================

#. Create default visitor & profile tables with the SQL statement in ``visitor_profile.mysql.sql``
#. Download ``mysql-connector-java-5.1.12-bin.jar`` and copy it to ``$CATALINA_HOME/lib``
#. Copy ``soln-p13n.mysql.xml`` to ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``
#. Modify above ``soln-p13n.xml`` as needed, such as server name, database name, password, ...etc.
#. Modify ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``, to comment out properties defined in `Database Configation Properties`_.

Configure for Standalone Derby
==============================

#. The derby database is at ``Solution-P13N-Ds/deliberyDB``.

   You may copy it under ``resource/derby/Repository``. Use ``resource/derby/DatabaseStartup.bat`` to start the standalone derby database.

#. Copy ``soln-p13n.derby.standalone.xml`` to ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``

#. Modify above ``soln-p13n.xml`` as needed, such as server name, database full path, password, ...etc.::

       soln.p13n.jdbc.url=jdbc:derby://localhost:1527/PATH/TO/deliveryDB;create=true

   Where ``/PATH/TO/`` is the file path to where you put ``deliveryDB`` directory.

#. Modify ``soln-p13n.war/WEB-INF/classes/soln-p13n.properties``, to comment out properties defined in `Database Configation Properties`_.
   
Configure for Embedded Derby
============================

#. The derby database is at ``Solution-P13N-Ds/deliberyDB``.

   You may copy it under ``$CATALINA_HOME`` or other desired location.

#. Copy ``soln-p13n.derby.xml`` to ``$CATALINA_HOME/conf/Catalina/localhost/soln-p13n.xml``

#. Modify above ``soln-p13n.xml``.::

       soln.p13n.jdbc.url=jdbc:derby:/PATH/TO/deliveryDB;create=true

   Where ``/PATH/TO/`` is the file path to where you put ``deliveryDB`` directory.

#. How to view embedded derby database.

   You may have trouble to find tools to view the embedded derby database. However, you can always copy the database to a standalone derby environement and view it with tools such as DbVisualizer.

Use Ant to Create Database Schema
=================================

The above steps describe manual steps for creating database schemas. You may skip this if you have already done so.

The following describes how to use ant script to create database schemas.

You will need to download and install Apache Ant 1.7 or greater:
http://ant.apache.org/bindownload.cgi.

Once Ant is installed you must now configure ``Solution-P13N-Ds/soln-p13n.properties`` with the proper database 
information.  *You do not have to modify this file if you are using the embedded database*.

Now from the command line::

	cd Solution-P13N-Ds
	ant -f create-db.xml
	

If you do not change ``Solution-P13N-Ds/soln-p13n.properties`` the default behavior will create a directory ``deliveryDB``
which is a schema for the embedded or standalone database Apache Derby.


