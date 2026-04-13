package nl.tue.algo.noisygraphpatterns.algorithms.ordering;
import nl.tue.algo.noisygraphpatterns.gui.Data;
import nl.tue.algo.noisygraphpatterns.io.Loading;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Based on https://github.com/GalambosAbel/CSRP/blob/main/cpp/src/NEOSJob.cpp
 * and https://github.com/NEOS-Server/JavaClient/blob/master/src/client/NeosClient.java
 */
public class NEOSJob implements Runnable {

    XmlRpcClient client;
    public final boolean serverUp;

    public String serverUrl;
    public String category;
    public String solver;
    public String inputMethod;
    public String email;
    public String inputType;
    public String dat1;
    public String dat2;
    public String tsp;
    public String algType;
    public String fixed;
    public String plType;
    public String comment;

    private int jobNumber;
    private String password;

    static String defaultEmail = "j.j.h.m.wulms@tue.nl";

    private Data data;

    public NEOSJob(Data data) {
        serverUrl = "https://neos-server.org:3333";
        category = "";
        solver = "";
        inputMethod = "";
        email = defaultEmail;
        inputType = "";
        dat1 = "";
        dat2 = "";
        tsp = "";
        algType = "";
        fixed = "";
        plType = "";
        comment = "";

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        URI url;
        try {
            url = new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            config.setServerURL(url.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        client = new XmlRpcClient();
        client.setConfig(config);

        String status;
        try {
            status = (String) client.execute("ping", new Object[0]);
        } catch (XmlRpcException e) {
            throw new RuntimeException(e);
        }
        if (status.equals("NeosServer is alive\n")) {
            System.out.println("NEOS server is up and running");
            serverUp = true;
        } else {
            System.out.println("NEOS server is down");
            serverUp = false;
        }
        System.out.println("------------------------------");

        this.data = data;
    }

//    @Override
//    public void run() {
//        Object[] params = new Object[2];
//
//        params[0] = this.jobNumber;
//        params[1] = this.password;
//
//        String status;
//
//        try {
//            status = (String) client.execute("getJobStatus", params);
//            while (!status.equals("Done")) {
//                Thread.sleep(1000);
//                status = (String) client.execute("getJobStatus", params);
//            }
//            System.out.println("Job number: " + jobNumber + " is done!");
//
//            String result = new String((byte[]) client.execute("getFinalResults", params));
//
//        } catch (XmlRpcException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public int[] submit() {
        String xml = "";
        xml += "<document>\n";
        xml += "<category>" + category + "</category>\n";
        xml += "<solver>" + solver + "</solver>\n";
        xml += "<inputMethod>" + inputMethod + "</inputMethod>\n";
        xml += "<email><![CDATA[" + email + "]]></email>\n";
        xml += "<inputType><![CDATA[" + inputType + "]]></inputType>\n";
//        xml += "<dat1><![CDATA[" + dat1 + "]]></dat1>\n";
//        xml += "<dat2><![CDATA[" + dat2 + "]]></dat2>\n";
        xml += "<tsp><![CDATA[" + tsp + "]]></tsp>\n";
        xml += "<algorithm><![CDATA[" + algType + "]]></algorithm>\n";
        xml += "<fixed><![CDATA[" + fixed + "]]></fixed>\n";
//        xml += "<PLTYPE><![CDATA[" + plType + "]]></PLTYPE>\n";
//        xml += "<comment><![CDATA[" + comment + "]]></comment>\n";
        xml += "</document>";

        Object[] params = new Object[]{xml};
        Object[] result;
        try {
            result = (Object[]) client.execute("submitJob", params);
        } catch (XmlRpcException e) {
            throw new RuntimeException(e);
        }

        jobNumber = ((Integer) result[0]).intValue();
        password = (String) result[1];

        if (jobNumber == 0) {
            System.out.println("NEOS Server error: " + password);
            return new int[0];
        }
        System.out.println("Job number: " + jobNumber + " " + "Password: " + password);

//        this.run();
        params = new Object[]{this.jobNumber, this.password};
        String status;
        try {
            status = (String) client.execute("getJobStatus", params);
            while (!status.equals("Done")) {
                Thread.sleep(1000);
                status = (String) client.execute("getJobStatus", params);
            }
            System.out.println("Job number: " + jobNumber + " is done!");

            String finalResult = new String((byte[]) client.execute("getFinalResults", params));
//            params = new Object[]{this.jobNumber, this.password, "results.txt"};
//            String finalResult = new String((byte[]) client.execute("getOutputFile", params));
//            System.out.println(finalResult);
            data.addOrdering("Optimal Moran's I", data.graph, Loading.readNEOSOrdering(finalResult));

        } catch (XmlRpcException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new int[1];
    }


    @Override
    public void run() {

    }
}
