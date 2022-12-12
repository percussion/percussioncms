# Redirect Conversion Tool
A command line utility for converting exported Redirects from Percussion Software's Redirect Manager service to different web server formats.

## Supported Formats
* IIS - Rewrite Rule format
* Apache - mod_rewrite format
* NGINX - rewrite rule format
* JSON - redirect.json format

## Command line syntax

### Help
``
java -jar perc-redirect-tool-8.1.1-SNAPSHOT.jar -h
``

### Generate IIS Redirects
``
java -jar perc-redirect-tool-8.1.1-SNAPSHOT.jar -csv <exported csv file name> -i
``

### Generate Apache Redirects
``
java -jar perc-redirect-tool-8.1.1-SNAPSHOT.jar -csv <exported csv file name> -a
``
### Generate NGINX Redirects
``
java -jar perc-redirect-tool-8.1.1-SNAPSHOT.jar -csv <exported csv file name> -n

### Generate JSON Redirects
``
java -jar perc-redirect-tool-8.1.1-SNAPSHOT.jar -csv <exported csv file name> -j
``
### Applying the redirects to IIS
IIS stores its redirects in the web.config file in the root of the IIS site. 
In IIS Manager, add a dummy redirect to generate the redirect configuration.
Copy and paste the generated redirects from the output file into the <Rules> section
of the web.config file. 

NOTE:  IIS requires that all rules have a name and that the name is unique.  The names are generated from the redirect from url (all special characters are stripped).  This may cause errors if duplicate names like <rule name="TEST"> vs <rule name="test">.  If you get an error like this, simply change the names so that they are unique and on't conflict. 

## How to Build
The project uses Apache Maven for dependency management / building.

After cloning the git repository, run:

``cd modules/redirect-tool
mvn clean install``

When the build completes the executable jar shoul dbe generated in the target subfolder.
