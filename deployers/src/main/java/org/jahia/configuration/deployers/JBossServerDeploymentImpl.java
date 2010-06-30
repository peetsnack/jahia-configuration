/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.configuration.deployers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * JBoss 4.2.x server deployer implementation.
 * @author Serge Huber
 * Date: 26 d�c. 2007
 * Time: 14:18:34
 */
public class JBossServerDeploymentImpl extends AbstractServerDeploymentImpl {

    public JBossServerDeploymentImpl(String targetServerDirectory) {
        super(targetServerDirectory);
    }

    protected String getSharedLibraryDirectory() {
        return "server/default/lib";
    }

    public boolean validateInstallationDirectory(String targetServerDirectory) {
        return new File(targetServerDirectory, "server/default/deploy/jboss-web.deployer/server.xml").exists();
    }

    public boolean deploySharedLibraries(String targetServerDirectory,
                                         List<File> pathToLibraries) throws IOException {
        File targetDirectory = new File(targetServerDirectory, getSharedLibraryDirectory());
        for (File currentLibraryPath : pathToLibraries) {
            FileUtils.copyFileToDirectory(currentLibraryPath, targetDirectory);
        }
        return true;
    }

    public boolean undeploySharedLibraries(String targetServerDirectory,
                                           List<File> pathToLibraries) throws IOException {
        File targetDirectory = new File(targetServerDirectory, getSharedLibraryDirectory());
        for (File currentLibraryPath : pathToLibraries) {
            File targetFile = new File(targetDirectory, currentLibraryPath.getName());
            targetFile.delete();
        }
        return true;
    }
    
    public String getDeploymentBaseDir() {
        return "server/default/deploy";
    }

    public String getDeploymentDirPath(String name, String type) {
        type = "jboss-sar".equals(type) ? "sar" : type;
        return getDeploymentBaseDir() + "/" + name + (name.endsWith("." + type) ? "" : "." + type);
    }

    public String getDeploymentFilePath(String name, String type) {
        type = "jboss-sar".equals(type) ? "sar" : type;
        return getDeploymentBaseDir() + "/" + name + "." + type;
    }

    @Override
    public String getWarExcludes() {
        return (String) getDeployersProperties().get("jboss");
    }

    @Override
    public boolean isAutoDeploySupported() {
        return true;
    }
}