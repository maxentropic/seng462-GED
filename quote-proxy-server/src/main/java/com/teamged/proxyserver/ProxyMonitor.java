package com.teamged.proxyserver;

import com.teamged.deployment.deployments.QuoteProxyServerDeployment;
import com.teamged.proxyserver.serverthreads.ProxyServerThread;
import com.teamged.proxyserver.serverthreads.QuoteProxyProcessingThread;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by DanielF on 2016-03-09.
 */
public class ProxyMonitor {

    private static final Object syncObject = new Object();
    private static final QuoteProxyServerDeployment PROXY_DEPLOY = ProxyMain.Deployment.getProxyServer();

    private static final ArrayList<ProxyServerThread> proxyThreads = new ArrayList<>();
    // TODO: Pre-fetch threads

    public static void runServer() {
        InternalLog.Log("Launching quote proxy server socket listeners");
        QuoteProxyProcessingThread qppThread;
        try {
            qppThread = new QuoteProxyProcessingThread(PROXY_DEPLOY.getPort(), PROXY_DEPLOY.getInternals().getThreadPoolSize(), syncObject);
            proxyThreads.add(qppThread);
            new Thread(qppThread).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        do {
            synchronized (syncObject) {
                try {
                    syncObject.wait();

                    /*
                    check thread status, restart threads if necessary
                     */
                } catch (InterruptedException e) {
                    // Close threads?
                    e.printStackTrace();
                    break;
                }
            }
        } while (!proxyThreads.isEmpty());
    }
}