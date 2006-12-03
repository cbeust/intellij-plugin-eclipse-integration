package com.javaexpert.intellij.plugins.eclipseclasspath.synchronizer;

import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;

class ConfigurationHelper {
    private Map<String, RegistrationHelper.Registration> loadedListeners = new HashMap<String, RegistrationHelper.Registration>();


    void readExternal(Element element) {
        for (Object o : element.getChildren()) {
            Element e = (Element) o;
            getLoadedListeners().put(e.getAttributeValue("tracedFile"), new RegistrationHelper.Registration(null, e.getAttributeValue("module"), e.getAttributeValue("library")));
        }
    }

    public void writeExternal(Element element, Map<String, RegistrationHelper.Registration> activeListeners) {
        for (String url : activeListeners.keySet()) {
            Element element1 = new Element("eclipse-dependency");
            element1.setAttribute("tracedFile", url);
            element1.setAttribute("module", activeListeners.get(url).moduleName);
            element1.setAttribute("library", activeListeners.get(url).libraryName);
            element.addContent(element1);
        }
    }

    Map<String, RegistrationHelper.Registration> getLoadedListeners() {
        return loadedListeners;
    }
}
