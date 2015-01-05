package org.jahia.utils.osgi.parsers;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a parser interface that will parse the contents of a file to find package references inside of it.
 */
public interface FileParser {

    public Logger getLogger();

    public void setLogger(Logger logger);

    public int getPriority();

    public boolean canParse(String fileName);

    public boolean parse(String fileName, InputStream inputStream, String fileParent, boolean externalDependency, boolean optionalDependency, String version, ParsingContext parsingContext) throws IOException;

}
