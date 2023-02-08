
Configure ISAPI Redirector
_____________________________________________________________

.. sectnum::
.. contents:: Table of Contents


This document describes how to install the redirector of IIS to Tomcat 6


Install IIS
===========
Add Window Component - "Appication Server" (from Add / Remove Programs)



Install Tomcat
==============
Install tomcat at ``%CATALINA_HOME%``. In the following steps, you may replace ``%CATALINA_HOME%`` with 
the full path of the actual location, e.g., ``C:\apps\Tomcat6.0``.
You may install "Apache Tomat 6" service



Copy DLL and Property files
===========================

#. Copy ``isapi_redirect.dll``	to 	``%CATALINA_HOME%\bin\win32\i386\isapi_redirect.dll``
#. Copy ``isapi_redirect.properties`` to ``%CATALINA_HOME%\bin\win32\i386\isapi_redirect.properties``
#. Edit the above properties file according to ``%CATALINA_HOME%``
#. Copy ``uriworkermap.properties`` to ``%CATALINA_HOME%\conf\uriworkermap.properties``
#. Copy ``workers.properties``	to ``%CATALINA_HOME%\conf\workers.properties``


Add virtual directory
=====================
Add 'jakarta' virtual directory to the Default Web Site

Using the IIS management console, add a new virtual directory to the Default Web Site. 
The name of the virtual directory must be 'jakarta'. 
Its physical path should be the directory where you placed isapi_redirect.dll 
(in our example it is ``C:\apps\Tomcat6.0\bin\win32``), while creating this new virtual directory assign it the execute access


Add ISAPI Filter
================

Using the IIS management console, add isapi_redirect.dll as a filter in your web site. 
Take Properties for the Default Web Site, and select the ISAPI Filters tab. The name of the filter should be 'jakarta', 
its executable must be ``C:\apps\Tomcat6.0\bin\win32\isapi_redirect.dll``. 

Add Web Service Extension
=========================

(This is for IIS 6 only) Using the IIS management console, 
select Web Service Extensions in the IIS Console Server Tree. 
Add a new extension called Jakarta Tomcat (this can be anything), 
add isapi_redirect.dll as the associated file 
(browse to the file, e.g. ``C:\apps\Tomcat6.0\bin\win32\isapi_redirect.dll``) 
and select Set extension status to Allowed. 

IIS 6 on 64-bit Windows
=======================

If the IIS 6 is running on a 64-bit Windows (such as Windows 2003 server 64-bit), 
and want to use the same 32-bit ISAPI filter, then you need to configure IIS to run 32-bit Web Applications on 64-bit Windows, 
which can be done with the following command:

``script %SystemDrive%\inetpub\AdminScripts\adsutil.vbs set w3svc/AppPools/Enable32bitAppOnWin64 1``

See detail info from:  `Running 32-bit Applications on 64-bit Windows (IIS 6.0)`_


.. _Running 32-bit Applications on 64-bit Windows (IIS 6.0): http://www.microsoft.com/technet/prodtechnol/WindowsServer2003/Library/IIS/0aafb9a0-1b1c-4a39-ac9a-994adc902485.mspx?mfr=true


Test ISAPI Redirector to Tomcat
===============================

All Tomcat example should work from both port 8080 and 80

	http://localhost:8080/examples/jsp/jsp2/tagfiles/hello.jsp
	
	http://localhost/examples/jsp/jsp2/tagfiles/hello.jsp
	
	
Add 'index.html' as default page
================================

This is an optional step if needed. Using the IIS management console, take Properties for the Default Web Site, 
click 'Documents' tab, add 'index.html' into the list and move to the beginning of the list.
