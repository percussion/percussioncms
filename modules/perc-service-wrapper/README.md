# perc-service-wrapper
This module contains backend support for the mechanism that controls the startup and shutdown of the percussion services.

Usage: java -jar perc-service-wrapper.jar [options...] [jetty properties...] [jetty configs...]

  The perc-service-wrapper.jar controls the startup and shutdown of the percussion services.
  The location of the root distribution directory is discovered based upon the location of the jar file unless a system
  -Drxdeploydir system property is provided.

  Extra options and properties files can be passed through to the Jetty start.jar if this is not a standalone DTS Server
  this is useful to check the jetty configuration in particular the --list-config option can help diagnosis of problems.

  To see the full list of jetty options you can pass the --jettyHelp option to this command.

* for more details see usage.txt under resources directory in this module.

## Building
  mvn clean install