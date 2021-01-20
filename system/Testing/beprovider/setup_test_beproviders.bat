REM Add the back-end instances to the server's config.xml file
java -cp \\hera\development\e2\Testing\shared\classes;\\hera\development\e2\tools\xml4j\xml4j.jar com.percussion.xml.PSXmlDocumentMerger \\hera\development\e2\Testing\beprovider\BackEndTestProviders.xml rxconfig\server\config.xml SecurityProviders
