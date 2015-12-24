/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.configuration.configurators;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * desc:  This class provides to you a *super interface* for properties.
 * It allows you to create a new properties file, set properties, remove
 * properties, get properties, get properties from an existing properties
 * object or from a flat file, get a complete properties object, and you can
 * store the new properties object where you want on the filesystem. The store
 * method keep your base properties file design (not like the store() method
 * from java properties object)!
 * <p/>
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class PropertiesManager {
    private Map<String,Object> properties = new LinkedHashMap<String, Object>();
    private Set<String> modifiedProperties = new HashSet<String>();
    private boolean loadedFromInputStream = false;
    private Set<String> removedPropertyNames = new HashSet<String>();
    private boolean unmodifiedCommentingActivated = false;
    private String additionalPropertiesMessage = "The following properties were added.";
    private Map<String,PropertyLayout> propertyLayouts = new LinkedHashMap<String,PropertyLayout>();
    boolean additionalMessageAdded = false;

    public class PropertyLayout {
        List<String> comments = new ArrayList<String>();
        String name;
        String nameLine;
        String separator = "=";
        List<String> valueLines = new ArrayList<String>();

        public PropertyLayout() {
        }

        public List<String> getComments() {
            return comments;
        }

        public void setComments(List<String> comments) {
            this.comments = comments;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            StringBuilder valueBuilder = new StringBuilder();
            for (String valueLine : valueLines) {
                valueBuilder.append(StringUtils.stripStart(unescapeNonASCII(valueLine), null));
            }
            return valueBuilder.toString();
        }

        public String getNameLine() {
            return nameLine;
        }

        public void setNameLine(String nameLine) {
            this.nameLine = nameLine;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public List<String> getValueLines() {
            return valueLines;
        }

        public void setValueLines(List<String> valueLines) {
            this.valueLines = valueLines;
        }
    }

    /**
     * Default constructor.
     *
     */
    public PropertiesManager() {
        // do nothing :o)
    } // end constructor


    /**
     * Construct a properties manager from an existing input stream that references a properties file.
     * @param sourcePropertiesInputStream
     * @throws IOException
     */
    public PropertiesManager(InputStream sourcePropertiesInputStream) throws IOException {
        this.loadProperties(sourcePropertiesInputStream);
    } // end constructor

    /**
     * Default constructor.
     *
     * @param properties The properties object used to define base properties.
     */
    public PropertiesManager(Properties properties) {
        this.properties.clear();
        for (String propertyName : properties.stringPropertyNames()) {
            setProperty(propertyName, properties.getProperty(propertyName));
        }
    } // end constructor


    /**
     * Load a complete properties file in memory by its filename.
     *
     * @param sourcePropertiesInputStream
     */
    private void loadProperties(InputStream sourcePropertiesInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(sourcePropertiesInputStream, "ISO-8859-1"));

        // parse the file...
        String currentLine = null;
        int lineNo = 0;
        boolean inMultilineValue = false;
        PropertyLayout currentPropertyLayout = new PropertyLayout();
        while ((currentLine = reader.readLine()) != null) {
            int valueSeparatorPos = -1;
            if (!inMultilineValue) {
                valueSeparatorPos = getValueSeparatorPos(currentLine);
            }
            if (inMultilineValue && currentLine.trim().equals("")) {
                if (currentPropertyLayout.getValueLines().size() > 0) {
                    currentPropertyLayout.getValueLines().add(currentLine);
                    properties.put(currentPropertyLayout.getName(), currentPropertyLayout.getValue());
                    propertyLayouts.put(currentPropertyLayout.getName(), currentPropertyLayout);
                    currentPropertyLayout = new PropertyLayout();
                }
                inMultilineValue = false;
                continue;
            }
            if (currentLine.trim().equals("") ||
                    currentLine.trim().startsWith("#") ||
                    currentLine.trim().startsWith("!")) {
                // empty natural line or comment line, we just skip it
                currentPropertyLayout.getComments().add(currentLine);
                lineNo++;
                continue;
            }
            if (valueSeparatorPos >= 0) {
                if (currentPropertyLayout.getNameLine() != null) {
                    if (currentPropertyLayout.getValueLines().size() > 0) {
                        properties.put(currentPropertyLayout.getName(), currentPropertyLayout.getValue());
                        propertyLayouts.put(currentPropertyLayout.getName(), currentPropertyLayout);
                        currentPropertyLayout = new PropertyLayout();
                    }
                }
                currentPropertyLayout.setNameLine(unescapeNonASCII(currentLine.substring(0, valueSeparatorPos)));
                currentPropertyLayout.setSeparator(currentLine.substring(valueSeparatorPos, valueSeparatorPos+1));
                currentPropertyLayout.setName(currentPropertyLayout.getNameLine().trim());
                String lastPropertyLine = unescapeNonASCII(currentLine.substring(valueSeparatorPos + 1));
                if (lastPropertyLine == null) {
                    lastPropertyLine = "";
                }
                if (lastPropertyLine.trim().endsWith("\\")) {
                    inMultilineValue = true;
                    lastPropertyLine = StringUtils.stripEnd(lastPropertyLine, "\\");
                    currentPropertyLayout.getValueLines().add(lastPropertyLine);
                } else {
                    currentPropertyLayout.getValueLines().add(lastPropertyLine);
                    properties.put(currentPropertyLayout.getName(), currentPropertyLayout.getValue());
                    propertyLayouts.put(currentPropertyLayout.getName(), currentPropertyLayout);
                    inMultilineValue = false;
                    currentPropertyLayout = new PropertyLayout();
                }
            } else {
                if (inMultilineValue) {
                    if (currentLine.trim().endsWith("\\")) {
                        // multi-value is continuing
                        currentLine = unescapeNonASCII(StringUtils.stripEnd(currentLine, "\\"));
                        currentPropertyLayout.getValueLines().add(currentLine);
                    } else {
                        // multi-value is (probably) stopped
                        currentPropertyLayout.getValueLines().add(unescapeNonASCII(currentLine));
                        properties.put(currentPropertyLayout.getName(), currentPropertyLayout.getValue());
                        propertyLayouts.put(currentPropertyLayout.getName(), currentPropertyLayout);
                        inMultilineValue = false;
                        currentPropertyLayout = new PropertyLayout();
                    }
                } else {
                    currentPropertyLayout.setNameLine(unescapeNonASCII(currentLine));
                    currentPropertyLayout.setName(currentPropertyLayout.getNameLine().trim());
                    properties.put(currentPropertyLayout.getName(), "");
                    propertyLayouts.put(currentPropertyLayout.getName(), currentPropertyLayout);
                    inMultilineValue = false;
                    currentPropertyLayout = new PropertyLayout();
                }
            }
            lineNo++;
        }

        IOUtils.closeQuietly(reader);
        loadedFromInputStream = true;
    } // end loadProperties


    /**
     * Get a property value by its name.
     *
     * @param propertyName The property name to get its value.
     * @return Returns a String containing the value of the property name.
     */
    public String getProperty(String propertyName) {
        return toString(properties.get(propertyName));
    } // end getProperty

    public Object getRawProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Set a property value by its name.
     *
     * @param propertyName The property name to set.
     * @param propvalue    The property value to set.
     */
    public void setProperty(String propertyName,
                            String propvalue) {
        String oldValue = (String) properties.put(propertyName, propvalue);
        if (!StringUtils.equals(oldValue, propvalue)) {
        	// set "modified" flag only if the value was effectively changed
        	modifiedProperties.add(propertyName);
        }
        PropertyLayout propertyLayout = propertyLayouts.get(propertyName);
        if (propertyLayout == null) {
            propertyLayout = new PropertyLayout();
            propertyLayout.setName(propertyName);
            propertyLayout.setNameLine(propertyName);
            if (getAdditionalPropertiesMessage() != null && !additionalMessageAdded) {
                propertyLayout.getComments().add("# " + getAdditionalPropertiesMessage());
                additionalMessageAdded = true;
            }
        }
        propertyLayout.getValueLines().clear();
        propertyLayout.getValueLines().add(" " + propvalue);
        propertyLayouts.put(propertyName, propertyLayout);
    } // end setProperty

    public void setProperty(String propertyName, Object[] propertyValues) {
        Object oldValue = properties.put(propertyName, propertyValues);
        String oldValueString = toString(oldValue);
        String newValueString = toString(propertyValues);
        if (!StringUtils.equals(oldValueString,newValueString)) {
            // set "modified" flag only if the value was effectively changed
            modifiedProperties.add(propertyName);
        }
        PropertyLayout propertyLayout = propertyLayouts.get(propertyName);
        if (propertyLayout == null) {
            propertyLayout = new PropertyLayout();
            propertyLayout.setName(propertyName);
            propertyLayout.setNameLine(propertyName);
            if (getAdditionalPropertiesMessage() != null && !additionalMessageAdded) {
                propertyLayout.getComments().add("# " + getAdditionalPropertiesMessage());
                additionalMessageAdded = true;
            }
        }
        propertyLayout.getValueLines().clear();
        int i = 0;
        for (Object propertyValue : propertyValues) {
            if (i==0) {
                propertyLayout.getValueLines().add(" " + propertyValue.toString());
            } else {
                propertyLayout.getValueLines().add("  " + propertyValue.toString());
            }
        }
        propertyLayouts.put(propertyName, propertyLayout);
    }

    /**
     * Remove a property by its name.
     *
     * @param propertyName The property name to remove.
     * @author Alexandre Kraft
     */
    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
        propertyLayouts.remove(propertyName);
        removedPropertyNames.add(propertyName);

    } // end removeProperty

    /**
     * Store new properties and values in the properties file.
     * If the file where you want to write doesn't exists, the file is created.
     *
     * @param sourcePropertiesInputStream The source input stream used to load the properties from, or null if it
     * doesn't exist.
     * @param destPropertiesFilePath   The filesystem path where the file is saved.
     * @author Alexandre Kraft
     * @author Khue N'Guyen
     */
    public void storeProperties(InputStream sourcePropertiesInputStream, String destPropertiesFilePath)
            throws IOException {
        if (loadedFromInputStream && sourcePropertiesInputStream == null) {
            throw new UnsupportedOperationException("If loaded from an input stream, it must be provided when storing the file");
        }
        List<String> outputLineList = new ArrayList<String>();
        String currentLine = null;

        File propertiesFileObject = new File(destPropertiesFilePath);
        File propertiesFileFolder = propertiesFileObject.getParentFile();

        // check if the destination folder exists and create it if needed...
        if (!propertiesFileFolder.exists()) {
            propertiesFileFolder.mkdirs();
            propertiesFileFolder = null;
        }

        // try to create a file object via the propertiesFilePath...


        if (loadedFromInputStream) {
            for (Map.Entry<String,PropertyLayout> propertyLayoutEntry : propertyLayouts.entrySet()) {
                for (String comment : propertyLayoutEntry.getValue().getComments()) {
                    outputLineList.add(comment);
                }
                int i=0;
                if (propertyLayoutEntry.getValue().getValueLines().size() == 0) {
                    outputLineList.add(propertyLayoutEntry.getValue().getNameLine());
                }
                for (String valueLine : propertyLayoutEntry.getValue().getValueLines()) {
                    StringBuilder lineBuilder = new StringBuilder();
                    if (unmodifiedCommentingActivated && !modifiedProperties.contains(propertyLayoutEntry.getKey())) {
                        lineBuilder.append("#");
                    }
                    if (i==0) {
                        lineBuilder.append(escapeNonASCII(propertyLayoutEntry.getValue().getNameLine(), true));
                        lineBuilder.append(propertyLayoutEntry.getValue().getSeparator());
                    }
                    lineBuilder.append(escapeNonASCII(valueLine, false));
                    if (propertyLayoutEntry.getValue().getValueLines().size() > 1 && i < propertyLayoutEntry.getValue().getValueLines().size() -1) {
                        lineBuilder.append("\\");
                    }
                    outputLineList.add(lineBuilder.toString());
                    i++;
                }
            }

            /*
            BufferedReader buffered = new BufferedReader(new InputStreamReader(sourcePropertiesInputStream));
            int lineNo = 0;

            Set<String> remainingPropertyNames = new HashSet<String>(properties.keySet());
            boolean inMultiLine = false;
            String currentPropertyName = null;

            // parse the file...
            while ((currentLine = buffered.readLine()) != null) {
                lineNo++;
                try {
                    int valueSeperatorPos = -1;
                    if (!inMultiLine) {
                        valueSeperatorPos = getValueSeparatorPos(currentLine);
                    }
                    if (!currentLine.trim().equals("") &&
                            !currentLine.trim().startsWith("#") &&
                            !currentLine.trim().startsWith("!") &&
                            (valueSeperatorPos >= 0)) {
                        currentPropertyName = currentLine.substring(0, valueSeperatorPos).trim();
                        if (currentLine.trim().endsWith("\\")) {
                            inMultiLine = true;
                        }
                        outputProperty(outputLineList, currentLine, remainingPropertyNames, currentPropertyName, valueSeperatorPos);
                    } else {
                        if (inMultiLine) {
                            if (currentLine.trim().endsWith("\\")) {
                                // multiline is continuing, we ignore the line
                                if (!modifiedProperties.contains(currentPropertyName) && !unmodifiedCommentingActivated) {
                                    outputLineList.add(currentLine);
                                }
                            } else {
                                // multiline ended here.
                                outputProperty(outputLineList, currentLine, remainingPropertyNames, currentPropertyName, valueSeperatorPos);
                                inMultiLine = false;
                            }
                        } else {
                            // this is a special line only for layout, like a comment or a blank line...
                            outputLineList.add(currentLine);
                            inMultiLine = false;
                        }
                    }
                } catch (IndexOutOfBoundsException ioobe) {
                } catch (PatternSyntaxException ex1) {
                    ex1.printStackTrace();
                } catch (IllegalArgumentException ex2) {
                    ex2.printStackTrace();
                }
            }

            // add not found properties at the end of the file (and the jahia.properties layout is kept)...
            if ((remainingPropertyNames.size() > 0) && (additionalPropertiesMessage != null)) {
                outputLineList.add("# " + additionalPropertiesMessage);
            }
            Iterator remainingPropNameIterator = remainingPropertyNames.iterator();
            while (remainingPropNameIterator.hasNext()) {
                String remainingPropertyName = (String) remainingPropNameIterator.next();
                StringBuffer specialLineBuffer = new StringBuffer();
                specialLineBuffer.append(escapeNonASCII(remainingPropertyName, true));
                for (int i = 0; i < 55 - remainingPropertyName.length(); i++) {
                    specialLineBuffer.append(" ");
                }
                specialLineBuffer.append("=   ");
                specialLineBuffer.append(escapeValue(remainingPropertyName, properties.get(remainingPropertyName)));
                outputLineList.add(specialLineBuffer.toString());
            }

            // close the buffered filereader...
            buffered.close();
            */

            // write the file...
            writeTheFile(destPropertiesFilePath, outputLineList);
        } else {
            FileOutputStream outputStream = new FileOutputStream(destPropertiesFilePath);
            getPropertiesObject().store(outputStream, "This file has been written by Jahia.");
            outputStream.close();
        }
    } // end storeProperties

    public void outputProperty(List<String> outputLineList, String currentLine, Set<String> remainingPropertyNames, String currentPropertyName, int equalPosition) {
        if (remainingPropertyNames.contains(currentPropertyName)) {
            Object propValue = properties.get(currentPropertyName);
            remainingPropertyNames.remove(currentPropertyName);
            StringBuilder lineBuilder = new StringBuilder();
            if (modifiedProperties.contains(currentPropertyName)) {
                lineBuilder.append(escapeNonASCII(currentLine.substring(0, equalPosition + 1), true));
                lineBuilder.append(" ");
                lineBuilder.append(escapeValue(currentPropertyName, propValue));
            } else {
                if (unmodifiedCommentingActivated) {
                    // this property was not modified, we comment it out
                    lineBuilder.append("#");
                    lineBuilder.append(escapeNonASCII(currentLine.substring(0, equalPosition + 1), true));
                    lineBuilder.append(" ");
                    lineBuilder.append(escapeValue(currentPropertyName, propValue));
                } else {
                    // property was not modified and commenting is not activated, we use the original line(s)
                    lineBuilder.append(currentLine);
                }
            }
            outputLineList.add(lineBuilder.toString());
        } else if (removedPropertyNames.contains(currentPropertyName)) {
            // the property must be removed from the file, we do not add it to the output.
        }
    }


    /**
     * Write the file composed by the storeProperties() method, using
     *
     * @param propertiesFilePath The filesystem path where the file is saved.
     * @param bufferList         List containing all the string lines of the new file.
     * @author Alexandre Kraft
     */
    private void writeTheFile(String propertiesFilePath,
                              List bufferList) {

        File thisFile = null;
        FileWriter fileWriter = null;
        StringBuffer outputBuffer = null;

        try {
            thisFile = new File(propertiesFilePath);
            fileWriter = new FileWriter(thisFile);
            outputBuffer = new StringBuffer();

            for (int i = 0; i < bufferList.size(); i++) {
                outputBuffer.append((String) bufferList.get(i));
                outputBuffer.append("\n");
            }

            fileWriter.write(outputBuffer.toString());
        } catch (java.io.IOException ioe) {
        } finally {
            try {
                fileWriter.close();
            } catch (java.io.IOException ioe2) {
            }
            fileWriter = null;
            thisFile = null;
        }
    } // end writeTheFile


    /**
     * Get the properties object for the instance of this class.
     *
     * @return The properties object for the instance of this class.
     * @author Alexandre Kraft
     */
    public Properties getPropertiesObject() {
        Properties propertiesObject = new Properties();
        for (Map.Entry<String,Object> propertiesEntry : properties.entrySet()) {
            propertiesObject.put(propertiesEntry.getKey(), toString(propertiesEntry.getValue()));
        }
        return propertiesObject;
    } // end getPropertiesObject

    public boolean isUnmodifiedCommentingActivated() {
        return unmodifiedCommentingActivated;
    }

    /**
     * If activated, non-modified properties will be commented out when saving the modifications
     * @param unmodifiedCommentingActivated
     */
    public void setUnmodifiedCommentingActivated(boolean unmodifiedCommentingActivated) {
        this.unmodifiedCommentingActivated = unmodifiedCommentingActivated;
    }

    public String getAdditionalPropertiesMessage() {
        return additionalPropertiesMessage;
    }

    /**
     * The message to use in a comment before the list of properties that were not present in the
     * original properties file and that were added dynamically. Set this message to null to avoid
     * the generation of this message.
     * @param additionalPropertiesMessage
     */
    public void setAdditionalPropertiesMessage(String additionalPropertiesMessage) {
        this.additionalPropertiesMessage = additionalPropertiesMessage;
    }

    private String toString(Object propertyValue) {
        if (propertyValue == null) {
            return null;
        }
        if (propertyValue instanceof String) {
            return (String) propertyValue;
        }
        if (propertyValue instanceof Object[]) {
            Object[] propertyValues = (Object[]) propertyValue;
            StringBuilder stringValues = new StringBuilder();
            int counter = 0;
            for (Object objectValue : propertyValues) {
                stringValues.append(objectValue.toString());
                if (counter < propertyValues.length - 1) {
                    stringValues.append(",");
                }
                counter++;
            }
            return stringValues.toString();
        } else {
            return propertyValue.toString();
        }
    }

    /**
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     * 
     * @see Properties
     */
    private String escapeValue(String propertyName, Object propertyValue) {
        if (propertyValue == null) {
            return null;
        }
        if (propertyValue instanceof String) {
            String theString = (String) propertyValue;
            if (theString == null || theString.length() == 0 || !theString.contains("\\")) {
                return escapeNonASCII(theString, false);
            }
            return escapeNonASCII(StringUtils.replace(theString, "\\", "\\\\"), false);
        } else if (propertyValue instanceof Object[]) {
            StringBuilder valueString = new StringBuilder();
            Object[] objectValues = (Object[]) propertyValue;
            List<String> stringValues = new ArrayList<String>();
            long totalValueLength = 0;
            for (Object objectValue : objectValues) {
                String stringValue = objectValue.toString();
                totalValueLength += stringValue.length();
                stringValues.add(stringValue);
            }
            boolean multiLineSplit = false;
            if (totalValueLength > 50 || (propertyLayouts.get(propertyName) != null && propertyLayouts.get(propertyName).getValueLines().size() > 1)) {
                multiLineSplit = true;
            }
            int counter = 0;
            for (String stringValue : stringValues) {
                valueString.append(stringValue);
                if (counter < stringValues.size() - 1) {
                    if (multiLineSplit) {
                        valueString.append(",\\\n    ");
                    } else {
                        valueString.append(",");
                    }
                }
                counter++;
            }
            return escapeNonASCII(valueString.toString(), false);
        } else {
            return escapeNonASCII(propertyValue.toString(), false);
        }
    }

    /**
     * @param input
     * @return
     */
    private String escapeNonASCII(String input, boolean encodeSeparators) {
        if (input == null || input.length() == 0 || input.trim().length() == 0) {
            return input;
        }
        StringBuilder output = new StringBuilder();
        for (int i=0; i < input.length(); i++) {
            char curChar = input.charAt(i);
            if ((int) curChar > 127) {
                // must escape using unicode
                output.append("\\u");
                output.append(String.format("%04X", (int) curChar));
            } else if ((int) curChar < 32) {
                switch (curChar) {
                    case '\b' :
                        output.append("\\b");
                        break;
                    case '\n' :
                        output.append("\\n");
                        break;
                    case '\t' :
                        // output.append("\\t");
                        output.append(curChar);
                        break;
                    case '\f' :
                        output.append("\\f");
                        break;
                    case '\r' :
                        output.append("\\r");
                        break;
                    default:
                        output.append("\\u");
                        output.append(String.format("%04X", (int) curChar));
                }
            } else {
                switch (curChar) {
                    case '"' :
                        output.append('\\');
                        output.append('"');
                        break;
                    case '\\' :
                        output.append('\\');
                        output.append('\\');
                        break;
                    case ':' :
                    case '=' :
                        if (encodeSeparators) {
                            output.append("\\");
                            output.append(curChar);
                        } else {
                            output.append(curChar);
                        }
                        break;
                    default :
                        output.append(curChar);
                        break;
                }
            }
        }
        return output.toString();
    }

    private String unescapeNonASCII(String input) {
        if (input == null || input.length() == 0 || input.trim().length() == 0) {
            return input;
        }
        StringBuilder output = new StringBuilder();
        boolean processingSlash = false;
        boolean processingUnicode = false;
        StringBuilder unicodeCharacters = new StringBuilder(4);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (processingUnicode) {
                unicodeCharacters.append(ch);
                if (unicodeCharacters.length() == 4) {
                    try {
                        int value = Integer.decode("0x" + unicodeCharacters.toString());
                        output.append((char) value);
                        unicodeCharacters = new StringBuilder(4);
                        processingUnicode = false;
                        processingSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Invalid unicode : " + unicodeCharacters + " in string : " + input, nfe);
                    }
                }
                continue;
            }

            if (processingSlash) {
                // handle an escaped value
                processingSlash = false;
                switch (ch) {
                    case '\\' :
                        output.append('\\');
                        break;
                    case '\'' :
                        output.append('\'');
                        break;
                    case '\"' :
                        output.append('"');
                        break;
                    case 'r' :
                        output.append('\r');
                        break;
                    case 'f' :
                        output.append('\f');
                        break;
                    case 't' :
                        output.append('\t');
                        break;
                    case 'n' :
                        output.append('\n');
                        break;
                    case 'b' :
                        output.append('\b');
                        break;
                    case 'u' :
                        processingUnicode = true;
                        break;
                    default :
                        output.append(ch);
                        break;
                }
            } else if (ch == '\\') {
                processingSlash = true;
            } else {
                output.append(ch);
            }
        }

        if (processingSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            output.append('\\');
        }

        return output.toString();
    }

    private int getValueSeparatorPos(String input) {
        if (input == null || input.length() == 0) {
            return -1;
        }
        int seperatorPos = getFirstSeperatorPos(input, "=");
        if (seperatorPos > -1) {
            return seperatorPos;
        }
        seperatorPos = getFirstSeperatorPos(input, ":");
        if (seperatorPos > -1) {
            return seperatorPos;
        }
        // now let's check if we have the case of a whitespace seperating the key from the value.
        int position = 0;
        while (Character.isWhitespace(input.charAt(position)) && !(input.charAt(position) == '\n') && !(input.charAt(position) == '\r') && position < input.length()-1) {
            position++;
        }
        if (position == input.length()-1) {
            // no starting whitespace found, the whole string is the key
            return -1;
        }
        while (!Character.isWhitespace(input.charAt(position)) && position < input.length()-1) {
            position++;
        }
        if (position == input.length()-1) {
            // no whitespace found after key
            return -1;
        }
        return position;
    }

    private int getFirstSeperatorPos(String input, String seperator) {
        int startPosition = 0;
        int seperatorPos = -1;
        while ((seperatorPos = input.indexOf(seperator, startPosition)) >= 0 && startPosition < input.length()-1) {
            if (seperatorPos > 0 && input.charAt(seperatorPos - 1) == '\\') {
                // this equals is escaped
                startPosition = seperatorPos + 1;
            } else {
                // we found the first proper equals sign
                if (seperatorPos >= 0) {
                    return seperatorPos;
                }
            }
        }
        return -1;
    }
}
