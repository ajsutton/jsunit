package net.jsunit;

import net.jsunit.configuration.Configuration;
import net.jsunit.model.DistributedTestRunResult;
import net.jsunit.model.TestRunResult;
import net.jsunit.model.TestRunResultBuilder;
import org.jdom.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DistributedTestRunManager {

    private Logger logger = Logger.getLogger("net.jsunit");
    private RemoteRunnerHitter hitter;
    private Configuration configuration;
    private String overrideURL;
    private DistributedTestRunResult distributedTestRunResult = new DistributedTestRunResult();

    public DistributedTestRunManager(Configuration configuration) {
        this(new RemoteMachineRunnerHitter(), configuration);
    }

    public DistributedTestRunManager(RemoteRunnerHitter hitter, Configuration configuration) {
        this(hitter, configuration, null);
    }

    public DistributedTestRunManager(RemoteRunnerHitter hitter, Configuration configuration, String overrideURL) {
        this.hitter = hitter;
        this.configuration = configuration;
        this.overrideURL = overrideURL;
    }

    public void runTests() {
        List<Thread> threads = new ArrayList<Thread>();
        for (final URL baseURL : configuration.getRemoteMachineURLs())
            threads.add(new Thread("Run JsUnit tests on " + baseURL) {
                public void run() {
                    runTestsOnRemoteMachine(baseURL);
                }
            });
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("One of the test threads was interrupted.");
            }
        }
    }

    private void runTestsOnRemoteMachine(URL baseURL) {
        TestRunResult testRunResult = null;
        try {
            URL fullURL = buildURL(baseURL);
            logger.info("Requesting run on remove machine URL " + baseURL);
            Document documentFromRemoteMachine = hitter.hitURL(fullURL);
            logger.info("Received response from remove machine URL " + baseURL);
            testRunResult = new TestRunResultBuilder(configuration).build(documentFromRemoteMachine);
        } catch (IOException e) {
            if (configuration.shouldIgnoreUnresponsiveRemoteMachines())
                logger.info("Ignoring unresponsive machine " + baseURL.toString());
            else {
                logger.info("Remote machine URL is unresponsive: " + baseURL.toString());
                testRunResult = new TestRunResult(baseURL);
                testRunResult.setUnresponsive();
            }
        }
        if (testRunResult != null) {
            testRunResult.setURL(baseURL);
            //noinspection SynchronizeOnNonFinalField
            synchronized (distributedTestRunResult) {
                distributedTestRunResult.addTestRunResult(testRunResult);
            }
        }
    }

    private URL buildURL(URL url) throws UnsupportedEncodingException, MalformedURLException {
        String fullURLString = url.toString();
        fullURLString += "/runner";
        if (overrideURL != null)
            fullURLString += "?url=" + URLEncoder.encode(overrideURL, "UTF-8");
        else if (configuration.getTestURL() != null)
            fullURLString += "?url=" + URLEncoder.encode(configuration.getTestURL().toString(), "UTF-8");
        return new URL(fullURLString);
    }

    public DistributedTestRunResult getDistributedTestRunResult() {
        return distributedTestRunResult;
    }

    public String getOverrideURL() {
        return overrideURL;
    }

    public void setOverrideURL(String overrideURL) {
        this.overrideURL = overrideURL;
    }
}
