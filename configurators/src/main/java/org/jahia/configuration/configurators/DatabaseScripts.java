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

// $Id: DatabaseScripts.java 26186 2009-03-27 08:31:06Z bpapez $
//
//  DatabaseScripts
//
//  30.03.2001  AK  added in jahia.
//  01.04.2001  AK  change the package.
//

package org.jahia.configuration.configurators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.FilenameFilter;


/**
 * desc:  This class is used by the installation and the administration
 * to get all informations required from database scripts, like msaccess.script
 * or hypersonic.script, from the jahia database script path (a jahiafiles
 * subfolder).
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public final class DatabaseScripts {


    /**
     * Default constructor.
     * @author  Alexandre Kraft
     */
    private DatabaseScripts()
    {
        super();
    }


    /**
     * Retrieves SQL statement for schema creation, by way of the database
     * dependent configuration file.
     * @param fileObject File the database configuration file
     * @throws java.io.IOException thrown if there was an error opening or parsing
     * the files
     * @return Iterator an Iterator of String objects containing the
     * schema creation SQL statements.
     */
    public static List<String> getSchemaSQL( File fileObject )
        throws IOException {
        FileInputStream scriptInputStream = new FileInputStream(fileObject.getPath());
        Properties      scriptProperties  = new Properties();
        scriptProperties.load( scriptInputStream );


        String scriptLocation = scriptProperties.getProperty("jahia.database.schemascriptdir");
        File parentFile = fileObject.getParentFile();
        File schemaDir = new File(parentFile, scriptLocation);

        List<String> result = getSQLStatementsInDir(schemaDir, ".sql");

        return result;
    }

    /**
     * Retrieves all the statement in a directory for files with a
     * specific extension (usually ".sql")
     * @param sqlDir File the directory in which to search for SQL files.
     * @param extension String extension for files, in lowercase. May be null
     * in which case all the files will be used.
     * @throws java.io.IOException
     * @return ArrayList
     */
    public static List<String> getSQLStatementsInDir (File sqlDir, final String extension)
        throws IOException {
        List<String> result = new ArrayList<String>();
        File[] schemaFiles = sqlDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir,
                                  String name) {
                if (extension != null) {
                    return name.toLowerCase().endsWith(extension);
                } else {
                    return true;
                }
            }
        });
        if (schemaFiles == null) {
            return result;
        }
        List<File> indexFiles = new ArrayList<File>();
        for (int i=0; i < schemaFiles.length; i++) {
            File sqlFile = schemaFiles[i];
            if(sqlFile.getName().endsWith("index.sql")) {
                indexFiles.add(sqlFile);
            } else {
                System.out.println("Loading statements from script file " + sqlFile);
                List<String> curFileSQL = getScriptFileStatements(sqlFile);
                result.addAll(curFileSQL);
            }
        }
        for (int i = 0; i < indexFiles.size(); i++) {
            File indexFile = indexFiles.get(i);
            System.out.println("Loading statements from index script file " + indexFile);
            List<String> curFileSQL = getScriptFileStatements(indexFile);
            result.addAll(curFileSQL);
        }
        return result;
    }

    /**
     * Get a Iterator containing all lines of the sql runtime from a
     * database script. This database script is getted in parameter like
     * a File object. The method use the BufferedReader object on a
     * FileReader object instanciate on the script file name.
     * @author  Alexandre Kraft
     *
     * @param   fileObject   File object of the database script file.
     * @return  Iterator containing all lines of the database script.
     */
    private static List<String> getScriptFileStatements( File fileObject )
    throws IOException
    {
        return org.jahia.commons.DatabaseScripts.getScriptStatements(new FileReader(fileObject));
    }

}