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

package org.jahia.utils.maven.plugin.buildautomation;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Map;

/**
 * User: islam
 * Date: 25 juin 2008
 * Time: 11:04:01
 */
public class QuartzConfigurator extends AbstractConfigurator {

    public static void updateConfiguration(String sourceFileName, String destFileName, Map values) throws IOException {

        File sourceConfigFile = new File(sourceFileName);
        File destConfigFile = new File(destFileName);
        if (sourceConfigFile.exists()) {
            // let's load the file's content in memory, assuming it won't be
            // too big.
            String fileContent = FileUtils.readFileToString(sourceConfigFile, "UTF-8");
            String transactionIsolationLevel = getValue(values, "jahia.quartz.selectWithLockSQL");
            String jdbcDelegate = getValue(values, "jahia.quartz.jdbcDelegate");
            String serializable = getValue(values, "jahia.quartz.serializable");
            fileContent = StringUtils.replace(fileContent, "@SELECTWITHLOCKSQL@", transactionIsolationLevel);
            fileContent = StringUtils.replace(fileContent, "@JDBCDELEGATE@", jdbcDelegate);
            fileContent = StringUtils.replace(fileContent, "@SERIALIZABLE@", serializable);
            // we have finished replacing values, let's save the modified
            // file.
            FileUtils.writeStringToFile(destConfigFile, fileContent, "UTF-8");
        }
    }

}
