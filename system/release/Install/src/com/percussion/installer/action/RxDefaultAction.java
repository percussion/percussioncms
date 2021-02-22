package com.percussion.installer.action;

import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.util.IOTools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RxDefaultAction extends RxIAAction
{

   @Override
   protected void execute()
   {

      m_strRootDir = getInstallValue(RxVariables.INSTALL_DIR);

      String configDir = m_strRootDir + "/rxconfig";
      String installerCfgDir = configDir + "/Installer";
      String installPropsFile = installerCfgDir + '/'
            + RxFileManager.INSTALLATION_PROPERTIES_FILE;
      String repositoryPropsFile = installerCfgDir + '/'
            + RxFileManager.REPOSITORY_FILE;
      String serverPropsPath = configDir + "/Server/"
            + RxFileManager.SERVER_PROPERTIES_FILE;

      File serverPropsFile = new File(serverPropsPath);
      File installFile = new File(installPropsFile);
      File repositoryFile = new File(repositoryPropsFile);

      serverPropsFile.getParentFile().mkdirs();

      URL serverPropsUrl = getResource("$RX_DIR$/config/Default/server.properties");
      URL installPropsUrl = getResource("$RX_DIR$/config/Default/installation.properties");
      URL repositoryPropsUrl = getResource("$RX_DIR$/config/Default/rxrepository.properties");

      createDefaultFiles(serverPropsUrl, serverPropsFile);
      createDefaultFiles(installPropsUrl, installFile);
      createDefaultFiles(repositoryPropsUrl, repositoryFile);

   }

   private void createDefaultFiles(URL stringUrl, File defaultFile)
   {
      try( InputStream stringInputStream = stringUrl.openStream()){
         IOTools.copyStreamToFile(stringInputStream, defaultFile);
         stringInputStream.close();
      }
      catch (IOException e)
      {
         RxLogger.logError(e.getMessage());
         RxLogger.logError(e);
      }

   }

   /**
    * Root dir of this installation.
    */
   private String m_strRootDir = "";

}
