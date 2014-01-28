/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.izpack;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.StringInputProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.izforge.izpack.util.VariableSubstitutor;

public class ToolInstalledValidator implements Validator {

    public boolean validate(ProcessingClient pdata) {
        Map<String, String> params = getParams(pdata);
        boolean valid = false;
        try {
            if (pdata.getText() == null || pdata.getText().trim().length() == 0
                    || params == null
                    || !Boolean.valueOf(params.get("validate"))) {
                // no need to validate
                return true;
            }
            boolean shouldBeDir = Boolean.valueOf(params.get("directory"));
            File tool = new File(pdata.getText());
            valid = tool.exists() && (shouldBeDir ? tool.isDirectory()
                    && tool.list().length > 0 : tool.isFile());
            String fileName = params.get("fileName");
            if (fileName != null && fileName.length() > 0) {
                // additionally validate the required file name
                valid = valid && validateFileName(tool, shouldBeDir, fileName);
            }

        } catch (Exception e) {
            System.out.println("validate() Failed: " + e);
        }

        return valid;
    }
    
    private boolean validateFileName(File tool, boolean shouldBeDir, String fileName) {
        if (shouldBeDir) {
            String[] names = fileName.split(",");
            for (String name : names) {
                name = name.trim();
                File file = new File(tool, name);
                if (file.exists() && file.isFile()) {
                    return true;
                }
                if (ExternalToolsPanelAction.isWindows()) {
                    file =  new File(tool, name + ".exe");
                    if (file.exists() && file.isFile()) {
                        return true;
                    }
                }
            }
        } else {
            String toolName = tool.getName();
            String[] names = fileName.split(",");
            for (String name : names) {
                name = name.trim();
                if (toolName.equals(name) || ExternalToolsPanelAction.isWindows() && toolName.equals(name + ".exe")) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> getParams(ProcessingClient client)
    {
        Map<String, String> returnValue = null;
        StringInputProcessingClient context = null;
        AutomatedInstallData idata = getIdata(client);
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        try
        {
            context = (StringInputProcessingClient) client;
            if (context.hasParams())
            {
                Map<String, String> params = context.getValidatorParams();
                returnValue = new HashMap<String, String>();
                Iterator<String> keys = params.keySet().iterator();
                while (keys.hasNext())
                {
                    String key = keys.next();
                    // Feed parameter values through vs
                    String value = vs.substitute(params.get(key), null);
                    // System.out.println("Adding local parameter: "+key+"="+value);
                    returnValue.put(key, value);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("getParams() Failed: " + e);
        }
        return returnValue;
    }

    private AutomatedInstallData getIdata(ProcessingClient client)
    {
        StringInputProcessingClient context = null;
        AutomatedInstallData idata = null;
        try
        {
            context = (StringInputProcessingClient) client;
            idata = (AutomatedInstallData) context.getInstallData();
        }
        catch (Exception e)
        {
            System.out.println("getIdata() Failed: " + e);
        }
        return idata;
    }
    

}
