package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class KVClientTest {

        private class ServerSetupJob implements Runnable {
                KVServer server;
                SocketServer sockServer;

                public ServerSetupJob(KVServer s, SocketServer ss) {
                        this.server = s;
                        this.sockServer = ss;
                }

                public void run() {
                        NetworkHandler netHandler = new KVClientHandler(this.server);
                        this.sockServer.addHandler(netHandler);

                        try {
                                System.out.println("Attempting to setup server...");
                                this.sockServer.connect();

                                System.out.println("Attempting to start server...");
                                this.sockServer.run();

                                System.out.println("Server started");
                        }
                        catch(IOException e) {
                                e.printStackTrace();
                        }
                }
        }

        private String addr = "localhost";
        private int port = 8080;
        private KVClient client = new KVClient(addr, port);
        private KVServer server = new KVServer(100, 10);
        private SocketServer sockServer = new SocketServer(addr, port);
        private Thread thread = new Thread(new ServerSetupJob(server, sockServer));

        public void setupConnection() {
                this.thread.start();
        }

        public void teardownConnection() {
                this.sockServer.stop();
                this.thread.stop();
        }

        @Test
        public void testPutGet() {
                String key = "testKey";
                String value = "testValue";
                String value2 = "testValue2";
                String getResult = null;

                setupConnection();
                System.out.println("Finished connection set up");

                try {
                        System.out.println("Putting client");
                        client.put(key, value);
                        System.out.println("Put client");
                } catch (KVException e) {
                        fail("Unsuccessful 'put': "+e.getMsg().getMessage());
                }
                try {
                        getResult = client.get(key);
                } catch (KVException e1) {
                        fail("Unsuccessful 'get': "+e1.getMsg().getMessage());
                }
                assertEquals("Put/Get commands using key of "+key+" and value of "+value+".", getResult, value);

                try {
                        client.put(key, value2);
                } catch (KVException e2) {
                        fail("Unsuccessful 'put': "+e2.getMsg());
                }
                try {
                        getResult = client.get(key);
                } catch (KVException e3) {
                        fail("Unsuccessful 'get': "+e3.getMsg());
                }
                assertEquals("Rewrote value for key of "+key+" to value of "+value+".", getResult, value2);

                //teardownConnection();
        }

        @Test
        public void testDel() {
                String key = "testKey";
                String value = "testValue";
                String value2 = "testValue2";
                String getResult = null;

                setupConnection();

                try {
                        client.put(key, value);
                } catch (KVException e) {
                        fail("Unsuccessful 'put': "+e.getMsg().getMessage());
                }
                try {
                        client.del(key);
                } catch (KVException e1) {
                        fail("Unsuccessful 'del': "+e1.getMsg().getMessage());
                }
                try {
                        getResult = client.get(key);
                } catch (KVException e2) {
                        fail("Unsuccessful 'get': "+e2.getMsg().getMessage());
                }
                assertEquals("Deleting value for key of "+key+".", getResult, null);

                try {
                        client.put(key, value);
                } catch (KVException e3) {
                        fail("Unsuccessful 'put': "+e3.getMsg().getMessage());
                }
                try {
                        client.put(key, value2);
                } catch (KVException e4) {
                        fail("Unsuccessful 'put': "+e4.getMsg().getMessage());
                }
                try {
                        client.del(key);
                } catch (KVException e5) {
                        fail("Unsuccessful 'del: "+e5.getMsg().getMessage());
                }
                try {
                        getResult = client.get(key);
                } catch (KVException e6) {
                        fail("Unsuccessful 'get': "+e6.getMsg().getMessage());
                }
                assertEquals("Deleting value for key of "+key+" after overwriting it.", getResult, null);

                //teardownConnection();
        }

}