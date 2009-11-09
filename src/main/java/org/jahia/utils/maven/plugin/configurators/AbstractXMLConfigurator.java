package org.jahia.utils.maven.plugin.configurators;

import org.jahia.utils.maven.plugin.buildautomation.JahiaPropertiesBean;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.util.Map;
import java.util.List;

/**
 * Abstract configurator that includes helper methods to ease XML DOM manipulation.
 *
 * @author loom
 *         Date: Oct 27, 2009
 *         Time: 3:28:04 PM
 */
public abstract class AbstractXMLConfigurator extends AbstractConfigurator {
    public AbstractXMLConfigurator(Map dbProps, JahiaPropertiesBean jahiaPropertiesBean) {
        super(dbProps, jahiaPropertiesBean);
    }

    /**
     * This method makes it easier to insert an XML DOM element immediately after the element that has been retrieved
     * by the XPath expression. If you are using this method with a document that uses namespaces, you will need to use
     * the "xp" prefix in your XPath expression, as in the following example :
     * /xp:beans/xp:bean[@id="FileListSync"]/xp:property[@name="syncUrl"]
     * Note that it is important that the XPath expression point to an element, not an attribute or other DOM nodes. No
     * sanity checks will be done by the method but an casting exception will be raised in that case.
     * @param jdomDocument the document used to scope the XPath execution
     * @param xPathExpression the xpath expression, including the "xp" prefix if namespaces are used
     * @param element the element to be inserted immediately after the element retrieved by the XPath.
     * @throws JDOMException in case there was an error executing the XPath expression or manipulating the DOM.
     */
    public void insertElementAfter(Document jdomDocument, String xPathExpression, Element element) throws JDOMException {
        Element rootElement = jdomDocument.getRootElement();
        String namespaceURI = rootElement.getNamespaceURI();
        XPath previousElementXPath = XPath.newInstance(xPathExpression);
        if ((namespaceURI != null) && (!"".equals(namespaceURI))) {
            previousElementXPath.addNamespace("xp", namespaceURI);
        }
        Element previousElement = (Element) (previousElementXPath.selectSingleNode(jdomDocument));
        int previousElementIndex = previousElement.getParent().indexOf(previousElement);
        previousElement.getParent().getContent().add(previousElementIndex + 1, element);
    }

    /**
     * Utility method to set an attribute on all elements matching the xPathExpression. Note that this method is
     * namespace aware and will require you to use the "xp" prefix in your XPath queries. For example, an XPath query
     * for a Spring XML configuration will look like this :
     * /xp:beans/xp:bean[@id="FileListSync"]/xp:property[@name="syncUrl"]
     * Currently there is no way to rename the prefix.
     * @param scopeElement the scope in which to execute the XPath query
     * @param xPathExpression the XPath query to select all the elements on which we want to set an attribute
     * @param attributeName the name of the attribute to set on ALL the elements matching the XPath expression
     * @param attributeValue the value of the attribute to set
     * @throws JDOMException raised if there was a problem manipulating the JDOM structure.
     */
    public void setElementAttribute(Element scopeElement, String xPathExpression, String attributeName, String attributeValue) throws JDOMException {
        XPath xPath = XPath.newInstance(xPathExpression);
        String namespaceURI = scopeElement.getDocument().getRootElement().getNamespaceURI();
        if ((namespaceURI != null) && (!"".equals(namespaceURI))) {
            xPath.addNamespace("xp", namespaceURI);
        }
        List<Element> elementList = (List<Element>) xPath.selectNodes(scopeElement);
        for (Element element : elementList) {
            element.setAttribute(attributeName, attributeValue);
        }
    }

    /**
     * Utility method to retrieve an XML element using an XPath expression. Note that this method is
     * namespace aware and will require you to use the "xp" prefix in your XPath queries. For example, an XPath query
     * for a Spring XML configuration will look like this :
     * /xp:beans/xp:bean[@id="FileListSync"]/xp:property[@name="syncUrl"]
     * Currently there is no way to rename the prefix.
     * @param scopeElement the scope in which to execute the XPath query
     * @param xPathExpression the XPath query to select the element we wish to retrieve. In the case where multiple
     * elements match, only the first one will be returned.
     * @return the first element that matches the XPath expression, or null if no element matches.
     * @throws JDOMException raised if there was a problem navigating the JDOM structure.
     */
    public Element getElement(Element scopeElement, String xPathExpression) throws JDOMException {
        XPath xPath = XPath.newInstance(xPathExpression);
        String namespaceURI = scopeElement.getDocument().getRootElement().getNamespaceURI();
        if ((namespaceURI != null) && (!"".equals(namespaceURI))) {
            xPath.addNamespace("xp", namespaceURI);
        }
        return (Element) xPath.selectSingleNode(scopeElement);
    }


}
