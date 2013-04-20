package edu.berkeley.cs162;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class KVClient_Test {
    private class SetupServer implements Runnable {
        KVServer kvserver;
        SocketServer sserver;

        public SetupServer(KVServer s, SocketServer ss) {
            this.kvserver = s;
            this.sserver = ss;
        }

        @Override
        public void run() {
            NetworkHandler nh = new KVClientHandler(this.kvserver);
            this.sserver.addHandler(nh);

            try {
                System.out.println("Setting up server...");
                this.sserver.connect();
                System.out.println("Starting up server...");
                this.sserver.run();
                System.out.println("Started");
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private String addr = "localhost";
    private int port = 8080;
    private KVClient kvclient = new KVClient(addr, port);
    private KVServer kvserver = new KVServer(100, 10);
    private SocketServer sserver = new SocketServer(addr, port);
    private Thread thread = new Thread(new SetupServer(kvserver, sserver));

    public void setupConnection() {
        this.thread.start();
    }

    public void endConnection() {
        this.sserver.stop();
        this.thread.stop();
    }

    public void testPutAndGet() {
        String key = "key";
        String val1 = "val1";
        String val2 = "val2";
        String result = "";

        try {
            client.put(key, val1);
        } catch (KVException kve1) {
            fail("Put failed");
        }
        try {
            result = kvclient.get(key);
        } catch (KVException kve2) {
            fail("Get failed");
        }
        
        assertEquals("Put&Get Result:"+result+" Value:"+val1, result, val1);

        try {
            kvclient.put(key, val2);
        } catch (KVException kve3) {
            fail("Put #2 failed");
        }
        try {
            result = kvclient.get(key);
        } catch (KVException kve4) {
            fail("Get #2 failed");
        }
        
        assertEquals("Put&Get Result:"+result+" Value2:"+val2, result, val2);
    }


    public void testDel() {
        String key = "key";
        String val = "val1";
        String val2 = "val2";
        String result = null;

        try {
            kvclient.put(key, val1);
        } catch (KVException kve1) {
            fail("Put failed");
        }
        try {
            kvclient.del(key);
        } catch (KVException kve2) {
            fail("Del failed");
        }
        try {
            result = kvclient.get(key);
        } catch (KVException kve3) {
            fail("Get failed");
        }

        assertEquals("Deleted", result, null);

        try {
            kvclient.put(key, val1);
        } catch (KVException kve4) {
            fail("Put #2 failed");
        }
        try {
            kvclient.put(key, val2);
        } catch (KVException kve5) {
            fail("Put #3 failed");
        }
        try {
            kvclient.del(key);
        } catch (KVException kve6) {
            fail("Del #2 failed");
        }
        try {
            result = kvclient.get(key);
        } catch (KVException kve7) {
            fail("Get #2 failed");
        }

        assertEquals("Deleted", result, null);
    }

    @Test
    public void mainTest(){
        setupConnection();
        testPutAndGet();
        testDel();
        teardownConnection();
    }    
}