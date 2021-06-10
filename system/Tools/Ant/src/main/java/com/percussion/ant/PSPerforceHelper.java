/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
package com.percussion.ant;

import com.perforce.api.Change;
import com.perforce.api.CommitException;
import com.perforce.api.Env;
import com.perforce.api.FileEntry;
import com.perforce.api.PerforceException;
import com.perforce.api.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;



/**
 * Some helper methods that wrap around the P4 Java API to
 * facilitate editing and submitting a file in perforce.
 */
public class PSPerforceHelper
{

    private static final Logger log = LogManager.getLogger(PSPerforceHelper.class);

    /**
     * Private constructor so that this class cannot be instantiated.
     */
    private PSPerforceHelper(){}


    /**
     * Creates a new changelist in perforce
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param desc the description to use, can be <code>null</code>.
     * @return the changelist number as a string
     * @throws PerforceException on any error
     */
    public static String newChangeList(Env env, String desc)
            throws PerforceException
    {

        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        if(desc == null || desc.trim().length() == 0)
            desc = "Created by PSPerforceHelper.";
        try
        {
            Change change = new Change(env);
            change.setDescription(desc);
            change.commit();
            return Integer.toString(change.getNumber());
        }
        catch (CommitException e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }

    }

    /**
     * Opens a file for edit in the changelist specified, it also syncs
     * the file to head.
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param file the file path, cannot be <code>null</code> or empty.
     * @param change the change list number, cannot be <code>null</code> or
     * empty.
     * @param lock indicating that the file should be locked when opened.
     * @throws PerforceException on any error.
     */
    public static void openForEdit(
            Env env, String file, String change, boolean lock)
            throws PerforceException
    {
        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        if(file == null || file.trim().length() == 0)
            throw new IllegalArgumentException(
                    "File path cannot be null or empty.");
        if(change == null || change.trim().length() == 0)
            throw new IllegalArgumentException(
                    "Change number cannot be null or empty.");
        try
        {
            FileEntry.openForEdit(
                    env, file, true, lock, Change.getChange(change));
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }
    }

    /**
     * Opens a file for delete in the changelist specified.
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param file the file path, cannot be <code>null</code> or empty.
     * @param change the change list number, cannot be <code>null</code> or
     * empty.
     * @param lock indicating that the file should be locked when opened.
     * @throws PerforceException on any error.
     */
    public static void openForDelete(
            Env env, String file, String change, boolean lock)
            throws PerforceException
    {
        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        if(file == null || file.trim().length() == 0)
            throw new IllegalArgumentException(
                    "File path cannot be null or empty.");
        if(change == null || change.trim().length() == 0)
            throw new IllegalArgumentException(
                    "Change number cannot be null or empty.");
        try
        {
            FileEntry fe = new FileEntry(env, file);
            fe.openForDelete(Change.getChange(change));
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }
    }

    /**
     * Reverts an open file, discarding all changes.
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param file the file path, cannot be <code>null</code> or empty.
     * @return <code>true</code> if the file was successfully reverted,
     * <code>false</code> otherwise.
     * @throws PerforceException on any error.
     */
    public static boolean revert(Env env, String file) throws PerforceException
    {

        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        try
        {
            List<String> cmdList = new ArrayList<String>(3);
            cmdList.add("p4");
            cmdList.add("revert");
            if(file != null && file.trim().length() > 0)
                cmdList.add(file);
            String[] cmd = new String[cmdList.size()];
            cmdList.toArray(cmd);
            PSP4Process p = new PSP4Process(env);
            p.exec(cmd);
            return true;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }
    }
    /**
     * Marks the given file for add and adds to the given change list
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param file the file path, cannot be <code>null</code> or empty.
     * @param change the change list number, cannot be <code>null</code> or
     * empty.
     * @throws PerforceException on any error.
     */
    public static void add(Env env, String file, String change) throws PerforceException
    {
        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        if(file == null || file.trim().length() == 0)
            throw new IllegalArgumentException(
                    "File path cannot be null or empty.");
        if(change == null || change.trim().length() == 0)
            throw new IllegalArgumentException(
                    "Change number cannot be null or empty.");

        try
        {
            FileEntry fe = new FileEntry(env, file);
            fe.openForAdd(Change.getChange(change));
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }
    }

    /**
     * Resolves and submits a changelist
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param change the change list number, cannot be <code>null</code> or
     * empty.
     * @throws PerforceException on any error.
     */
    public static void submitChange(Env env, String change)
            throws PerforceException
    {
        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        if(change == null || change.trim().length() == 0)
            throw new IllegalArgumentException(
                    "Change number cannot be null or empty.");
        try
        {
            Change changelist = Change.getChange(env, change, true);
            changelist.resolve(true);
            changelist.submit();

        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }

    }

    /**
     * Syncs the client to the head revision
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @throws PerforceException on any error.
     */
    public static void syncClient(Env env)
            throws PerforceException
    {
        syncClient(env, null);
    }

    /**
     * Syncs the client to the head revision
     * @param env the perforce environment to use, cannot be <code>null</code>.
     * @param path the specific depot path to be synched, May be
     * <code>null</code> or empty.
     * @throws PerforceException on any error.
     */
    public static void syncClient(Env env, String path)
            throws PerforceException
    {
        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        try
        {
            List<String> cmdList = new ArrayList<String>(3);
            cmdList.add("p4");
            cmdList.add("sync");
            if(path != null && path.trim().length() > 0)
                cmdList.add(path);
            String[] cmd = new String[cmdList.size()];
            cmdList.toArray(cmd);
            PSP4Process p = new PSP4Process(env);
            p.exec(cmd);


        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }

    }

    /**
     * Returns a perforce environment from the properties
     * file passed in.
     * @param path the path to the perforce environment
     * properties file. Cannot be <code>null</code> or empty.
     * @return the perforce environment, may be <code>null</code>.
     * @throws PerforceException
     */
    public static Env getEnv(String path)
            throws PerforceException
    {
        if(path == null || path.trim().length() == 0)
            throw new IllegalArgumentException("Path cannot be null or empty.");
        return new Env(path);
    }

    /**
     * Recursive method, checks out all files under the srcDirectory in perforce
     * @param env Cannot be <code>null</code> or empty.
     * @param srcDirectory Cannot be <code>null</code> or empty.
     * @param change Cannot be <code>null</code> or empty.
     * @param lock Cannot be <code>null</code> or empty.
     * @throws PerforceException
     */
    public static void checkOutSourceDirectory(Env env, String view, String change, boolean lock)
            throws PerforceException
    {
        if(env == null)
            throw new IllegalArgumentException(
                    "Perforce environment cannot be null.");
        try
        {
            List<String> cmdList = new ArrayList<String>(3);
            cmdList.add("p4");
            cmdList.add("edit");
            cmdList.add("-c");
            cmdList.add(change);
            if(view != null && view.trim().length() > 0)
                cmdList.add(view);
            String[] cmd = new String[cmdList.size()];
            cmdList.toArray(cmd);
            PSP4Process p = new PSP4Process(env);
            p.exec(cmd);
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }
    }

    /**
     * Adds all the files in the source directory to the passed in changelist and marks for add
     * @param env Cannot be <code>null</code> or empty.
     * @param srcDirectory Cannot be <code>null</code> or empty.
     * @param change Cannot be <code>null</code> or empty.
     * @throws PerforceException
     */
    public static void addSourceDirectory(Env env, String srcDirectory, String change) throws PerforceException
    {
        try
        {
            List<String> cmdList = new ArrayList<String>(3);
            cmdList.add("p4");
            cmdList.add("add");
            cmdList.add("-c");
            cmdList.add(change);
            if(srcDirectory != null && srcDirectory.trim().length() > 0)
                cmdList.add(srcDirectory);
            String[] cmd = new String[cmdList.size()];
            cmdList.toArray(cmd);
            PSP4Process p = new PSP4Process(env);
            p.exec(cmd);
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new PerforceException(e.getMessage());
        }
        finally
        {
            Utils.cleanUp();
        }
    }
}
