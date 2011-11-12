package org.jahia.configuration.cluster.tool;

import com.jcraft.jsch.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jahia.configuration.cluster.ClusterConfigBean;
import org.jahia.configuration.logging.AbstractLogger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Cluster start Tomcat, waiting for each node to complete startup before starting the next.
 */
public class ClusterSequentialTomcatStart extends AbstractClusterOperation {

    public ClusterSequentialTomcatStart(AbstractLogger logger, ClusterConfigBean clusterConfigBean) {
        super(logger, clusterConfigBean);
    }

    @Override
    public void execute() throws JSchException, SftpException, IOException {
        JSch jSch = new JSch();

        final byte[] prvkey = readPrivateKeyFromFile(); // Private key must be byte array
        final byte[] emptyPassPhrase = new byte[0]; // Empty passphrase for now, get real passphrase from MyUserInfo

        jSch.addIdentity(
            clusterConfigBean.getDeploymentUserName(),    // String userName
            prvkey,          // byte[] privateKey
            null,            // byte[] publicKey
            emptyPassPhrase  // byte[] passPhrase
        );

        for (int i = 0; i < clusterConfigBean.getNumberOfNodes(); i++) {

            Session session = jSch.getSession(clusterConfigBean.getDeploymentUserName(), clusterConfigBean.getExternalHostNames().get(i), 22);

            // username and password will be given via UserInfo interface.
            UserInfo ui = new ClusterUserInfo(logger);
            session.setUserInfo(ui);
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(clusterConfigBean.getStartupCommandLine());

            //channel.setInputStream(System.in);
            channel.setInputStream(null);

            //channel.setOutputStream(System.out);

            //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
            //((ChannelExec)channel).setErrStream(fos);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int j = in.read(tmp, 0, 1024);
                    if (j < 0) break;
                    System.out.print(new String(tmp, 0, j));
                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            channel.disconnect();
            session.disconnect();

            // now we must wait for Tomcat server to become available before continuing to the next node.
            HttpClient httpClient = new DefaultHttpClient();
            String waitForStartupURL = clusterConfigBean.getWaitForStartupURL();
            waitForStartupURL = waitForStartupURL.replaceAll("\\$\\{hostname\\}", clusterConfigBean.getExternalHostNames().get(i));
            boolean available = false;
            while (!available) {
                HttpGet httpGet = new HttpGet(waitForStartupURL);
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == 200) {
                    available = true;
                }
            }

        }
    }
}
