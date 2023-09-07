This 8.1.2.1 patch release provides a mix of security updates and bug fixes to the 8.1.2 release version of PercussionCMS.  This update is only applicable to the 8.1.2 release version.  All updates in this "hotfix" will be included in the 8.1.3 release version.

-----------------
Security Updates
-----------------

 - Apache Shiro for CVE-2022-40664

 A library used by the Dashboard components has been updated to mitigate the above CVE.

 - Spring Security for CVE-2022-31692

 A library used by the "Secure Sites" feature to add LDAP authentication to a Tomcat hosted website has   been updated to mitigate the above CVE.  This feature is only active if the secure sites feature is configured and the published web site is hosted with a Java application server like Apache Tomcat. 

 - Commons Text for CVE-2022-42889

A library used by various CMS components was updated to mitigate the above CVE.
 
-----------------
Bug Fixes
-----------------
 - CMS #890 WARNING: Problem finding the navon - probably previewing a managed nav slot outside of a site is unclear.

A new warning is showing up post upgrade that does not include the id or template of the Page that is being logged. After the update, the page GUID and template name will be listed.  This warning is typically logged for Pages that have a Navigation component on the Page or Template, but the Page does not exist in a Navigation section so the Nav component can't be rendered.  Future updates may convert this warning into a debug statement.  Quieting this warning in the logs can be accomplished by adding the following to the <InstallDir>/jetty/base/resources/log4j2.xml file, before the </Loggers> line:

        <AsyncLogger name="com.percussion.services.assembly.impl.finder.PSNavFinderUtils" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger> 


 - CMS #762 When Debug Tools are enabled: the SQL editor fails to load with a 500 error
   https://github.com/percussion/percussioncms/issues/762

A bug was fixed where when debug tools were enabled in the rxconfig/Server/server.properties file by setting enableDebugTools=true, the https://cmsurl/test/sql.jsp tool would fail to load.  The patch resolves that issue.  NOTE:  We recommend that the debug tools be disabled for production systems, only enabled temporarily for troubleshooting or debugging by system administrators.  If you turn debug tools on to validate the fix, please turn them off before re-scanning you server.

 - CMS #851 Can't save workflow role permissions
   https://github.com/percussion/percussioncms/issues/851

   A bug was fixed that was preventing Admin's from editing the Workflow Role permissions.  After the patch is applied, users should be able to edit Workflow Role permissions again in the PercussionCMS ui.  NOTE: Rhythmyx Workflow Roles are not editable via the Percussion CMS UI and need to be updated in the legacy Workflow Editor or with the Server Administrator tool.


-------------------
Linux Installation
-------------------
Replace <InstallDir> with the path to your percussion cms installation below.

1. Download the patch zip file:

   wget <patch url>

2. Move the patch to the patch folder under the Percussion installation tree:
   
   mkdir -p <InstallDir>/Patch
   mv v8.1.2.1-PATCH-5-25-23.zip <InstallDir>/Patch/

3. Uncompress the patch:
   cd <InstallDir>/Patch/
   unzip v8.1.2.1-PATCH-5-25-23.zip

4.  Stop the CMS service:

    service PercussionCMS stop
    ps -ef | grep jetty

5.  Install the patch:
    cd v8.1.2.1-PATCH-5-25-23/
    chmod +x *.sh
    ./install.sh <InstallDir>/

6.  Start the CMS service:

    service PercussionCMS start

--------------------
Windows Installation
--------------------
1. Download the patch zip file
2. Extract the zip file to the <InstallDir>\Patch folder.  If the Patch folder does not exist, create it.
3. Start a Command Prompt as Adminstrator:
4. cd <InstallDir>\Patch\v8.1.2.1-PATCH-5-25-23\
5. Stop the CMS service:
   net stop PercussionCMS

6. Install the patch:

install.bat <InstallDir>\   (trailing backslash is required)

7. Start the CMS service:

net start PercussionCMS

-----------------------
List of Files Replaced:
-----------------------

The install.bat and install.sh scripts will try to automatically update the files listed below. The same operations can be performed manually in the event of a script error.  In the list below, <InstallDir> is the PercussionCMS installation directory, and <PatchDir> is the directory that this patch was unzipped to.

--------------------------------
Commons Text for CVE-2022-42889
---------------------------------
- File: <InstallDir>\sys_resources\webapps\secure\WEB-INF\lib\commons-text-1.9.jar 
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar

- File: <InstallDir>\jetty\defaults\lib\perc\commons-text-1.9.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar

- File: <InstallDir>\rxconfig\SiteConfigs\$log$\lib\commons-text-1.9.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar


- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.9.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\commons-text-1.10.0.jar

-------------------------------------------------
Updates to Spring Security for CVE-2022-31692
-------------------------------------------------

- File: <InstallDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-config-5.6.2.jar
  Replaced By: <PatchDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-config-5.6.9.jar

- File: <InstallDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.2.jar
  Replaced By: <PatchDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-core-5.6.9.jar

- File: <InstallDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-crypto-5.6.2.jar
  Replaced By: <PatchDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-crypto-5.6.9.jar

- File: <InstallDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-ldap-5.6.2.jar
  Replaced By: <PatchDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-ldap-5.6.9.jar

- File: <InstallDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-web-5.6.2.jar
  Replaced By: <PatchDir>\sys_resources\webapps\secure\WEB-INF\lib\spring-security-web-5.6.9.jar

-----------------------------------------
Updates for Apache Shiro for CVE-2022-40664
-----------------------------------------

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-cache-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-cache-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-core-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-core-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-ogdl-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-config-ogdl-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-core-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-cipher-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-cipher-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-core-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-core-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-hash-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-crypto-hash-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-event-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-event-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-lang-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-lang-1.10.0.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-web-1.7.1.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\shiro-web-1.10.0.jar

--------------------------------------------------------------------------------
Updates to Percussion application to resolve issues: #890, #762, #851...
--------------------------------------------------------------------------------

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\test\sql.jsp
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\test\sql.jsp

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\perc-system-8.1.2.1.jar

- File: <InstallDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.jar
  Replaced By: <PatchDir>\jetty\base\webapps\Rhythmyx\WEB-INF\lib\sitemanage-8.1.2.1.jar



