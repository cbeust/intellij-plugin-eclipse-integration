package com.javaexpert.intellij.plugins.eclipseclasspath.synchronizer;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.javaexpert.intellij.plugins.eclipseclasspath.EclipseTools;
import com.javaexpert.intellij.plugins.eclipseclasspath.synchronizer.Registry.Registration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DependencySynchronizerImpl implements ModuleComponent, JDOMExternalizable, DependencySynchronizer {
    private static final String ECLIPSE_DEPENDENCIES_SUFFIX = "-eclipse_dependencies";

    private Module module;
    private LibraryHelper libraryHelper;
    private Configuration configuration;
    private Registry registry;
    private UI ui;
    private EclipseTools eclipseTools;

    public DependencySynchronizerImpl(Module module) {
        this.setModule(module);
    }

    public void projectOpened() {
        registerLoadedListeners();
    }

    public void projectClosed() {
        registry.unregisterAllListeners();
    }

    @NotNull
    public String getComponentName() {
        return "DependencySynchronizer";
    }

    public void initComponent() {
        setLibraryHelper(new LibraryHelper(module));
        setConfiguration(new Configuration());
        setRegistry(new Registry());
        setUi(new UI());
        setEclipseTools(new EclipseTools());
    }

    public void stopTracingChanges(VirtualFile file) {
        String libraryName = registry.getLibraryName(file);
        registry.unregisterFileSystemListener(file);
        libraryHelper.removeDependencyBetweenModuleAndLibraryAndDeleteLibrary(libraryName);
    }

    public void traceChanges(VirtualFile classpathVirtualFile) {
        if (module == null) {
            ui.displayNoProjectSelectedWarnning();
            return;
        }

        String libraryName = ui.getLibraryNameFromUser(module.getProject(), computeEclipseDependenciesLibraryDefaultName(module));
        if (libraryName == null) return;

        registerListener(classpathVirtualFile, module, libraryName);
        detectedClasspathChanges(classpathVirtualFile);
    }

    public boolean isFileTraced(VirtualFile file) {
        return registry.isFileRegistered(file);
    }

    private void registerLoadedListeners() {
        for (Map.Entry<String, Registration> e : configuration.getLoadedListeners().entrySet()) {
            try {
                registerListener(
                        VirtualFileManager.getInstance().findFileByUrl(e.getKey())
                        , module, e.getValue().libraryName);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void registerListener(VirtualFile classpathVirtualFile, Module currentModule, String libraryName) {
        registry.registerClasspathFileModificationListener(classpathVirtualFile, libraryName, new ClasspathFileModificationListener(classpathVirtualFile), currentModule.getName());
    }


    private Library syncDependencies(VirtualFile classpathVirtualFile) {
        List<String> jars = eclipseTools.extractJarsFromEclipseDotClasspathFile(classpathVirtualFile.getPath());
        return libraryHelper.createOrRefreshLibraryWithJars(jars, registry.getLibraryName(classpathVirtualFile), classpathVirtualFile.getParent().getPath());
    }

    private String computeEclipseDependenciesLibraryDefaultName(Module currentModule) {
        return currentModule.getName() + ECLIPSE_DEPENDENCIES_SUFFIX;
    }

    public void readExternal(Element element) throws InvalidDataException {
        configuration.readExternal(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        configuration.writeExternal(element, registry.getRegistrations());
    }

    public void disposeComponent() {
        // do nothing
    }

    public void moduleAdded() {
        // do noting
    }

    private void detectedClasspathChanges(VirtualFile classpathVirtualFile) {
        Library library = syncDependencies(classpathVirtualFile);
        ui.displayInformationDialog(library.getUrls(OrderRootType.CLASSES));
    }

    protected void setModule(Module module) {
        this.module = module;
    }

    protected void setLibraryHelper(LibraryHelper libraryHelper) {
        this.libraryHelper = libraryHelper;
    }

    protected void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected void setRegistry(Registry registry) {
        this.registry = registry;
    }

    protected void setUi(UI ui) {
        this.ui = ui;
    }

    protected void setEclipseTools(EclipseTools eclipseTools) {
        this.eclipseTools = eclipseTools;
    }

    private class ClasspathFileModificationListener extends VirtualFileAdapter {
        private final VirtualFile classpathVirtualFile;

        public ClasspathFileModificationListener(VirtualFile classpathVirtualFile) {
            this.classpathVirtualFile = classpathVirtualFile;
        }

        public void contentsChanged(VirtualFileEvent event) {
            if (classpathVirtualFile.getPath().equals(event.getFile().getPath())) {
                detectedClasspathChanges(classpathVirtualFile);
            }
        }

        public VirtualFile getClasspathVirtualFile() {
            return classpathVirtualFile;
        }
    }
}
