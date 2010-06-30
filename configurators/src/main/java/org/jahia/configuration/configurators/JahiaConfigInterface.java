package org.jahia.configuration.configurators;

import java.util.List;

/**
 * Configuration interface used by the configurators.
 *
 * @author loom
 *         Date: May 11, 2010
 *         Time: 1:18:20 PM
 */
public interface JahiaConfigInterface {
    
    String getRelease();

    String getDb_script();

    String getJahiaEtcDiskPath();

    String getJahiaVarDiskPath();

    String getJahiaSharedModulesDiskPath();

    String getJahiaModulesHttpPath();

    String getJahiaEnginesHttpPath();

    String getJahiaJavaScriptHttpPath();

    String getJahiaWebAppsDeployerBaseURL();

    String getCluster_activated();
    
    String getClusterStartIpAddress();

    String getCluster_node_serverId();

    String getProcessingServer();

    String getJahiaImportsDiskPath();

    List<String> getClusterNodes();

    String getDevelopmentMode();

    String getTargetServerDirectory();

    String getTargetServerType();

    String getTargetServerVersion();

    String getDatabaseType();

    String getDatabasePassword();

    String getDatabaseUrl();

    String getDatabaseUsername();

    String getOverwritedb();

    List<String> getSiteImportLocation();

    String getTargetConfigurationDirectory();

    void setTargetConfigurationDirectory(String targetConfigurationDirectory);

    String getJahiaRootUsername();
    
    String getJahiaRootPassword();

    String getJahiaRootFirstname();
    
    String getJahiaRootLastname();
    
    String getJahiaRootEmail();
    
    String getSourceWebAppDir();

    String getExternalConfigPath();

    String getWebAppDirName();
    
    String getMailServer();
    
    String getMailFrom();
    
    String getMailAdministrator();
    
    String getMailParanoia();

    /**
     * Returns the Web application context path Jahia is deployed to. Is empty
     * for ROOT context and starts with a slash in other cases (e.g. /jahia).
     * 
     * @return the Web application context path Jahia is deployed to. Is empty
     *         for ROOT context and starts with a slash in other cases (e.g.
     *         /jahia)
     */
    String getContextPath();
}