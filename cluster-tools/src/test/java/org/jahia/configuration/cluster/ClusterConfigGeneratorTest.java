package org.jahia.configuration.cluster;

import com.google.common.io.Files;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.jahia.configuration.cluster.tool.ClusterConfigBean;
import org.jahia.configuration.cluster.tool.operations.ClusterConfigGenerator;
import org.jahia.configuration.logging.AbstractLogger;
import org.jahia.configuration.logging.ConsoleLogger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Unit test for cluster configuration generator
 */
public class ClusterConfigGeneratorTest extends TestCase {

    AbstractLogger logger;

    public void testConfigGeneration() throws Exception {
        this.logger = new ConsoleLogger();

        File tempDir = Files.createTempDir();

        copyResourcesToDir(tempDir, "cluster", "cluster");

        File clusterDir = new File(tempDir, "cluster");

        ClusterConfigBean clusterConfigBean = new ClusterConfigBean(logger, clusterDir);
        ClusterConfigGenerator clusterConfigGenerator = new ClusterConfigGenerator(logger, clusterConfigBean);
        clusterConfigGenerator.execute();

        // now let's validate the generated configuration.
        for (int i=0; i < clusterConfigBean.getNumberOfNodes(); i++) {
            File nodeDir = new File(clusterConfigBean.getNodesDirectoryName() + File.separator + clusterConfigBean.getNodeId(i));
            assertTrue("Node directory " + nodeDir + " not found !", nodeDir.exists());

            // now we need to check that all the template files were copied properly.

            // @todo

            // now let's check the generated configuration.
            Properties jahiaAdvancedProperties = new Properties();
            jahiaAdvancedProperties.load(new FileReader(new File(nodeDir, clusterConfigBean.getJahiaAdvancedPropertyRelativeFileLocation())));

            assertEquals("Cluster configuration is not activated !", "true", jahiaAdvancedProperties.getProperty("cluster.activated"));
            assertEquals("Node server Id is not correct", clusterConfigBean.getNodeId(i), jahiaAdvancedProperties.getProperty("cluster.node.serverId"));

            if ("processing".equals(clusterConfigBean.getNodeType(i))) {
                assertEquals("Node should be a processing server", "true", jahiaAdvancedProperties.getProperty("processingServer"));
            } else {
                assertEquals("Node should not be a processing server", "false", jahiaAdvancedProperties.getProperty("processingServer"));
            }

            // we should add more checks here but for the time being it shall do.
            for (String fileNameToFilter : clusterConfigBean.getFilesToFilter()) {
                File fileToFilter = new File(nodeDir, fileNameToFilter);
                assertTrue("Couldn't find filtered file " + fileToFilter, fileToFilter.exists());

                File templateDir = new File(clusterConfigBean.getTemplateDirectoryName());
                File originalFile = new File(templateDir, fileNameToFilter);
                assertTrue("Coulnd't find original file source at " + originalFile, originalFile.exists());

                String originalFileContents = FileUtils.readFileToString(originalFile);
                String filteredFileContents = FileUtils.readFileToString(fileToFilter);
                assertTrue("Original and filtered files are the same ! Filtering did not work", !originalFileContents.equals(filteredFileContents));
            }

        }

        // finally let's cleanup
        FileUtils.deleteDirectory(tempDir);
    }

    private void copyResourcesToDir(File targetDirectory, String originalRelativeLocation, String targetRelativeLocation) throws IOException {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource[] dataTablesResources = patternResolver.getResources("classpath*:"+originalRelativeLocation+"/**/*");
        File targetCompleteDirectory = new File(targetDirectory, targetRelativeLocation);
        if (!targetCompleteDirectory.exists()) {
            targetCompleteDirectory.mkdirs();
        }
        logger.info("Copying " + originalRelativeLocation + " files to " + targetCompleteDirectory + "...");
        for (Resource resource : dataTablesResources) {
            String relativePath = resource.getURI().toString();
            int relativeLocation = relativePath.lastIndexOf(originalRelativeLocation + File.separator);
            if (relativeLocation > -1) {
                relativePath = relativePath.substring(relativeLocation + originalRelativeLocation.length() + 1);
            }
            File destFile = new File(targetCompleteDirectory, relativePath);
            logger.debug("Copying " + relativePath + " to " + destFile);
            if (!relativePath.endsWith("/")) {
                destFile.getParentFile().mkdirs();
                FileUtils.copyInputStreamToFile(resource.getInputStream(), destFile);
            } else {
                destFile.mkdirs();
            }
        }
    }

}
