package org.jahia.utils.maven.plugin.osgi.parsers;

import org.jahia.utils.maven.plugin.osgi.PackageUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.util.List;

/**
 * XML file parser for JPDL file types
 */
public class JpdlXmlFileParser extends AbstractXmlFileParser {

    @Override
    public boolean canParse(String fileName, Element rootElement) {
        return fileName.toLowerCase().endsWith(".jpdl.xml");
    }

    @Override
    public void parse(String fileName, Element rootElement, ParsingContext parsingContext, boolean externalDependency) throws JDOMException {
        getLogger().debug("Processing workflow definition file (JBPM JPDL) " + fileName + "...");
        List<Attribute> classAttributes = getAttributes(rootElement, "//@class");
        for (Attribute classAttribute : classAttributes) {
            getLogger().debug(fileName + " Found class " + classAttribute.getValue() + " package=" + PackageUtils.getPackageFromClass(classAttribute.getValue()));
            parsingContext.addPackageImport(PackageUtils.getPackageFromClass(classAttribute.getValue()));
        }
    }
}
