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
package com.percussion.packagemanagement;

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.deployer.server.IPSPackageInstaller;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.packagemanagement.PSPackageFileEntry.PackageFileStatus;
import com.percussion.rx.services.deployer.IPSPackageUninstaller;
import com.percussion.rx.services.deployer.PSPackageUninstall;
import com.percussion.server.PSServer;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

/**
 * Handles installing packages when the server is started
 * 
 * @author JaySeletz
 *
 */
@PSSiteManageBean("startupPackageInstaller")
public class PSStartupPkgInstaller implements IPSNotificationListener, IPSMaintenanceProcess
{

    private String packageFileListPath;
    private String logFilePath;
    private IPSPackageInstaller packageInstaller;
    private IPSPackageUninstaller packageUninstaller;
    private File packageDir = null;
    private File logFile = null;
    private IPSMaintenanceManager maintenanceManager;
    private IPSNotificationService notificationService;
    

    private static final Logger log = LogManager.getLogger(PSStartupPkgInstaller.class);
    private static final String MAINT_PROC_NAME = PSStartupPkgInstaller.class.getName();
    private static final String SAAS_FLAG = "doSAAS";


    public PSStartupPkgInstaller()
    {
        log.info("PSStartupPkgInstaller Bean created");
    }
    /**
     * Set the directory from which packages are installed, used by 
     * unit test, otherwise the runtime root directory is obtained from {@link PSServer#getRxDir()} and
     * the "Packages/Percussion" subdirectory is used.
     * 
     * @param packageDir
     */
    public void setPackageDir(File packageDir)
    {
        this.packageDir = packageDir;
    }

    @Autowired
    public void setPackageInstaller(IPSPackageInstaller packageInstaller)
    {
        this.packageInstaller = packageInstaller;
    }

    @Value("rxconfig/Installer/InstallPackages.xml")
    public void setPackageFileListPath(String packageFileListPath)
    {
        this.packageFileListPath = packageFileListPath;
    }

    @Value("rxconfig/Installer/InstallPackages.log")
    public void setLogFilePath(String logFilePath)
    {
        this.logFilePath = logFilePath;
    }

    public IPSPackageUninstaller getPackageUninstaller() {
        return packageUninstaller;
    }

    public void setPackageUninstaller(IPSPackageUninstaller packageUninstaller) {
        this.packageUninstaller = packageUninstaller;
    }
        
    @Override
    public String getProcessId()
    {
        return MAINT_PROC_NAME;
    }

    /**
     * Upon server startup, it will look through the packagesInstall.xml for any uninstall or revert entries.
     * 
     * For each uninstall entry, it will uninstall the package and remove the entry from the xml.
     * 
     * For each revert, we uninstall the package and then mark the package entry as 'pending'. So that it gets reinstalled.
     */
    public void uninstallPackages()
    {
    	//Starting maintenance now
    	startMaintWork();
    	PSPackageFileList packageFileList = null;
    	    	  	
        appendLogEntry(null, null, false);
        appendLogEntry("Uninstalling packages based on package file list: " + packageFileListPath, null, true);
    	        
        try
        {
        	//Get entries
        	packageFileList = getPackageFileList();
        	List<PSPackageFileEntry> entries = packageFileList.getEntries();
          	List<PSPackageFileEntry> entriesToUninstall = new ArrayList<>();
          	
        	// find uninstall and revert entries
        	for (PSPackageFileEntry entry : entries){
        		if(PackageFileStatus.UNINSTALL.equals(entry.getStatus()) || 
        				PackageFileStatus.REVERT.equals(entry.getStatus())){
        			entriesToUninstall.add(entry);
        		}
        	}
        	
        	//If we don't find any entries then we have no work to do and maintenance is complete.
        	if(entriesToUninstall.isEmpty()){
                maintenanceManager.workCompleted(this);
                appendLogEntry("No packages to uninstall", null, true);
                packageFileList = null; // prevent saving when the finally block activates
                return;
        	}
        	
            for (PSPackageFileEntry entry : entriesToUninstall)
            {
            	// uninstall the package
                packageFileList = uninstallPackage(entry, packageFileList);
            }
        }
        catch(Exception e)
        {
        	//Log the package as failed
        	log.error("Package failed to uninstall. Stack trace:\n {}" , e.getLocalizedMessage(), e);
        	failMaintWork();
        }
        finally
        {
            if (packageFileList != null) {
                savePackageFileList(packageFileList);
            }
        }

        //We are finished.
        completeMaintWork();
        appendLogEntry("Packages successfully uninstalled", null, true);
    }
    
    /**
     * 
     * @param entry The package to uninstall.
     * @param packageFileList The list that is keeping track of all of our package entries.
     * @return The changed list or null if we run into an exception.
     */
    private PSPackageFileList uninstallPackage(PSPackageFileEntry entry, PSPackageFileList packageFileList){
    	
        String packageName = entry.getPackageName();
        boolean isRevertEntry = entry.getStatus() == PackageFileStatus.REVERT;
        
        try
        {
            appendLogEntry("Uninstalling package: " + packageName + "...", null, false);
            // uninstall package
            doPackageUninstall(packageName, isRevertEntry);
            
            //remove from package list if we are not reverting, otherwise set it to pending.
            if(!PackageFileStatus.REVERT.equals(entry.getStatus()))
            {
            	packageFileList.getEntries().remove(entry);	
            	appendLogEntry(packageName + " uninstalled successfully", null, false);
            }
            else
            {
            	entry.setStatus(PackageFileStatus.PENDING);
            	appendLogEntry("Setting package to 'PENDING' to be reinstalled: " + packageName, null, false);
            }
            return packageFileList;
        }
        catch (Exception e)
        {
            appendLogEntry("Package: " + packageName + " failed to uninstall: " + e.getLocalizedMessage(), e, true);
            packageFileList = null; // prevent saving when the finally block activates in the parent method
            failMaintWork();
            return packageFileList;
        }
    }

    /**
     * Uninstalls a package
     * @param packageName the name of the package to uninstall, i.e. perc.widget.form
     */
    protected void doPackageUninstall(String packageName) throws PSNotFoundException {
        doPackageUninstall(packageName, false);
    }
    
    /**
     * Uninstalls a package
     * @param packageName the name of the package to uninstall, i.e. perc.widget.form
     * @param isRevertEntry <code>true</code> if the package is marked for REVERT in InstallPackages.xml
     */
    protected void doPackageUninstall(String packageName, boolean isRevertEntry) throws PSNotFoundException {
        packageUninstaller.uninstallPackages(packageName, isRevertEntry);
    }

    /**
     * When the server starts, it notifies the listener which calls upon install packages.
     * 
     * This method will then perform maintenance work and attempt to install each package marked pending.
     * 
     * If it fails, it will set the specified package as failed, and not continue.
     * 
     * Maintenance mode will not end until all packages pass during next server startup.
     */
    public void installPackages()
    {
        startMaintWork();
        
        PSPackageFileList packageFileList = null;
        appendLogEntry(null, null, false);
        appendLogEntry("Starting package installation using package file list: " + packageFileListPath, null, true);
        
        try
        {
            packageFileList = getPackageFileList();
            List<PSPackageFileEntry> entries = packageFileList.getEntries();
            List<PSPackageFileEntry> entriesToInstall = new ArrayList<>();
            for (PSPackageFileEntry entry : entries)
            {
            	if (!PackageFileStatus.INSTALLED.equals(entry.getStatus()) && 
            			!PackageFileStatus.UNINSTALL.equals(entry.getStatus()))
            	{
            		entriesToInstall.add(entry);
            	}
            }

            if (entriesToInstall.isEmpty())
            {
                notifyComplete();
                maintenanceManager.workCompleted(this);
                appendLogEntry("All packages are up to date.", null, true);
                packageFileList = null; // prevent save in finally
                return;
            }
            
            boolean completed = true;
            for (PSPackageFileEntry entry : entriesToInstall)
            {
                String pkgName = entry.getPackageName();
                
                try
                {
                    appendLogEntry("Installing package: " + pkgName + "...", null, false);
                    File pkgFile = getPackageFile(pkgName);
                    // if the package is being reverted, we are forcing install of older package
                    packageInstaller.installPackage(pkgFile, entry.getStatus() != PackageFileStatus.REVERT);
                    entry.setStatus(PackageFileStatus.INSTALLED);
                    appendLogEntry(pkgName + " installed successfully", null, false);
                }
                catch (Exception e)
                {
                    entry.setStatus(PackageFileStatus.FAILED);
                    appendLogEntry("Package: " + pkgName + " failed to install: " + e.getLocalizedMessage(), e, true);
                    completed = false;
                }
            }
            
            //If this is a SAAS install, copy the object store to the var directory after installing the packages
            copyImmutableObjectStore();
            
            if (completed)
            {
                notifyComplete();
                completeMaintWork();
                appendLogEntry("Package installation completed", null, true);                
            }
            else
            {
                failMaintWork();
                appendLogEntry("Package installation aborted due to errors", null, true);
            }
        }
        catch (Exception e)
        {
            failMaintWork();
            
            log.error("Package installation failed: {}" , e.getLocalizedMessage(), e);
        }
        finally
        {
            if (packageFileList != null) {
                savePackageFileList(packageFileList);
            }
        }
    }

    private void copyImmutableObjectStore() {
        if(PSServer.getServerProps() != null 
        		&& StringUtils.equals(PSServer.getServerProps().getProperty(SAAS_FLAG), "true")) {
	        File mutableDir = new File(PSServer.getRxDir(), "var");
	        File objectStoreDir = new File(PSServer.getRxDir(), "ObjectStore");
	        File mutableObjectStoreDir = new File(mutableDir, "ObjectStore");
	        
	        if (!mutableObjectStoreDir.exists()) {
	        	try {
	        		mutableObjectStoreDir.mkdirs();
				} catch (Exception e) {
					log.error("Unable to create mutable object store directory: {}" , mutableObjectStoreDir.getAbsolutePath());
					throw new RuntimeException("Unable to create mutable object store directory: " + mutableObjectStoreDir.getAbsoluteFile());
				}
	        }
	        
	        try {
		        FileUtils.copyDirectory(objectStoreDir, mutableObjectStoreDir);
	        }
		    catch (Exception e) {
	            log.error(e);
	            if (e instanceof RuntimeException) {
	            	throw (RuntimeException) e;
	            }
	            throw new RuntimeException("Failed to copy content type to mutables");
	        }
        }
	}

	private void notifyComplete()
    {
        if (notificationService != null) {
            notificationService.notifyEvent(new PSNotificationEvent(EventType.STARTUP_PKG_INSTALL_COMPLETE, null));
        }
    }

    private void completeMaintWork()
    {
        if (maintenanceManager != null)
        {
            maintenanceManager.workCompleted(this);
        }
    }

    private void failMaintWork()
    {
        if (maintenanceManager != null)
        {
            maintenanceManager.workFailed(this);
        }
    }

    private void startMaintWork()
    {
        if (maintenanceManager != null)
        {
            maintenanceManager.startingWork(this);
        }
    }

    /**
     */
    private void appendLogEntry(String msg, Exception ex, boolean logToServer)
    {
        boolean isError = (ex != null);
        
        String output;
        if (msg == null) {
            output = "\n";
        }
        else
        {
            output = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()) + ": ";
            if (isError) {
                output += "ERROR: ";
            }
            output += msg;
            output += "\n";
        }
        
        File file = getLogFile();
        if (file == null)
        {
            System.out.println(output);
            return;
        }
        
        Writer writer = null;
        try
        {
            writer = new FileWriter(file, true);
            IOUtils.write(output, writer);
        }
        catch (IOException e)
        {
            log.error("Failed to log entry to log file {}: {}" , file.getAbsolutePath() , e.getLocalizedMessage());
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
        
        if (logToServer && msg != null)
        {
            if (isError) {
                log.error(msg, ex);
            }
            else {
                log.info(msg);
            }
        }
    }


    private File getLogFile()
    {
        if (logFilePath == null) {
            return null;
        }

        if (logFile == null)
        {
            logFile = new File(PSServer.getRxDir(), logFilePath);
        }
        
        return logFile;
    }

    private File getPackageFile(String packageName) throws IOException
    {
        File file = new File(getPackageDir(), packageName + ".ppkg");
        if (!file.exists()) {
            throw new IOException("Package file does not exist: " + file.getPath());
        }

        return file;
    }

    private File getPackageDir()
    {
        if (packageDir == null) {
            packageDir = new File(PSServer.getRxDir(), "Packages/Percussion");
        }

        return packageDir;
    }


    private PSPackageFileList getPackageFileList() throws IOException
    {
        try(FileInputStream in = new FileInputStream(new File(PSServer.getRxDir(),packageFileListPath))){
            String xmlString = IOUtils.toString(in);
            return PSPackageFileList.fromXml(xmlString);
        }
    }
    

    /**
     * @param packageFileList
     */
    private void savePackageFileList(PSPackageFileList packageFileList)
    {
        try(FileOutputStream out =  new  FileOutputStream(new File(PSServer.getRxDir(),packageFileListPath))){
           IOUtils.write(packageFileList.toXml(), out);
        }
        catch (Exception e)
        {
            log.error("Failed to save package installer results to file {}:{}" , packageFileListPath , e.getLocalizedMessage(), e);
        }

    }

    @Override
    public void notifyEvent(PSNotificationEvent notification)
    {
        if (EventType.CORE_SERVER_POST_INIT.equals(notification.getType()))
        {
            PSItemDefManager itemDefManager = PSItemDefManager.getInstance();
            
            try {
                itemDefManager.deferUpdateNotifications();

                // Use a new package uninstaller
                setPackageUninstaller(new PSPackageUninstall());

                // CMS-3561
                Scheduler scheduler = (Scheduler)getWebApplicationContext().getBean("org.springframework.scheduling.quartz.SchedulerFactoryBean");
                log.info("Pausing Quartz Scheduler...");
                scheduler.pauseAll();
                
                uninstallPackages();
                installPackages();
                
                log.info("Resuming Quartz Scheduler...");
                scheduler.resumeAll();
                
            } 
            catch (SchedulerException e) {
                log.error("Error pausing/resuming Quartz with message: {}" , e.getMessage());
            } 
            finally {
                itemDefManager.commitUpdateNotifications();
            }
        }
    }

    @Autowired
    public void setNotificationService(IPSNotificationService notificationService)
    {
        notificationService.addListener(EventType.CORE_SERVER_POST_INIT, this);
        this.notificationService = notificationService;
    }

    @Autowired
    public void setMaintenanceManager(IPSMaintenanceManager maintenanceManager)
    {
        this.maintenanceManager = maintenanceManager;
    }
}
