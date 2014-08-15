/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2014 Jahia Limited. All rights reserved.
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

package org.jahia.configuration.configurators;

import java.io.File;
import java.util.Map;
import java.io.IOException;
import java.util.Properties;

import org.codehaus.plexus.util.PropertyUtils;

/**
 * Property configuration for the jahia.properties file.
 * User: islam
 * Date: 16 juil. 2008
 * Time: 10:09:49
 */
public class JahiaPropertiesConfigurator extends AbstractConfigurator {

    private PropertiesManager properties;

    public JahiaPropertiesConfigurator(Map dbProperties, JahiaConfigInterface jahiaConfigInterface) {
        super(dbProperties, jahiaConfigInterface);
    }

    public void updateConfiguration(ConfigFile sourceJahiaPath, String targetJahiaPath) throws IOException {
        properties = new PropertiesManager(sourceJahiaPath.getInputStream());
        properties.setUnmodifiedCommentingActivated(true);

        File targetJahiaFile = new File(targetJahiaPath);
        Properties existingProperties = new Properties();
        if (targetJahiaFile.exists()) {
            existingProperties.putAll(PropertyUtils.loadProperties(targetJahiaFile));
            for (Object key : existingProperties.keySet()) {
                String propertyName = String.valueOf(key);
                properties.setProperty(propertyName, existingProperties.getProperty(propertyName));
            }
        }

        // jahia tools manager
        properties.setProperty("jahiaToolManagerUsername", jahiaConfigInterface.getJahiaToolManagerUsername());
        properties.setProperty("jahiaToolManagerPassword", JahiaGlobalConfigurator.encryptPassword(jahiaConfigInterface.getJahiaToolManagerPassword()));
        
        properties.setProperty("jahiaVarDiskPath", jahiaConfigInterface.getJahiaVarDiskPath());
        properties.setProperty("jahiaModulesDiskPath", jahiaConfigInterface.getJahiaModulesDiskPath());
        properties.setProperty("jahiaWebAppsDeployerBaseURL", jahiaConfigInterface.getJahiaWebAppsDeployerBaseURL());
        properties.setProperty("jahiaImportsDiskPath", jahiaConfigInterface.getJahiaImportsDiskPath());
        properties.setProperty("db_script", jahiaConfigInterface.getDb_script());
        properties.setProperty("operatingMode", jahiaConfigInterface.getOperatingMode());

        properties.setProperty("hibernate.dialect", getDBProperty("jahia.database.hibernate.dialect"));
        
        if (jahiaConfigInterface.getJahiaProperties() != null) {
            for (Map.Entry<String, String> entry : jahiaConfigInterface.getJahiaProperties().entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
        }
        
        configureScheduler();
        
        if (jahiaConfigInterface.getJahiaAdvancedProperties() != null) {
            for (Map.Entry<String, String> entry : jahiaConfigInterface.getJahiaAdvancedProperties().entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
        }

        properties.storeProperties(sourceJahiaPath.getInputStream(), targetJahiaPath);
    }

    private void configureScheduler() {
        String delegate = (String) dbProperties.get("jahia.quartz.jdbcDelegate");
        if (delegate == null || delegate.length() == 0) {
            delegate = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        }

        if (jahiaConfigInterface.getTargetServerType().startsWith("weblogic")) {
            delegate = "org.quartz.impl.jdbcjobstore.WebLogicDelegate";
            if (jahiaConfigInterface.getDatabaseType().equals("oracle")) {
                delegate = "org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate";
            }
        }
        
        if (jahiaConfigInterface.getTargetServerType().startsWith("was")
                && jahiaConfigInterface.getDatabaseType().equals("oracle")) {
            delegate = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        }

        properties.setProperty("org.quartz.driverDelegateClass", delegate);
    }
}
