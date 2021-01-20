# servletutils
This module contains backend support for servlet processing and utility apis for tomcat.

* Provides utility methods such as determining configuration file paths relative to the servlet root directory.
* The input validator filter "intercepts" a pre-defined set of input parameters and validates them, making sure they comply to their restriction type.
* Utility class to load and save HTTP based connectors from a Tomcat server.xml.

## Building
  mvn clean install
