/**
 * Client component for generating load for the KeyValue store.
 * This is also used by the Master server to reach the slave nodes.
 *
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 *
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs162;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

/**
 * This class is used to communicate with (appropriately marshalling and unmarshalling)
 * objects implementing the {@link KeyValueInterface}.
 *
 * @param <K> Java Generic type for the Key
 * @param <V> Java Generic type for the Value
 */
public class KVClient implements KeyValueInterface {

    private String server = null;
    private int port = 0;

    /**
     * @param server is the DNS reference to the Key-Value server
     * @param port is the port on which the Key-Value server is listening
     */
    public KVClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    private Socket connectHost() throws KVException {
        Socket socket = null;
        try {
            socket = new Socket(this.server, this.port);
        } catch(UnknownHostException u) {
            throw new KVException(new KVMessage("resp", "Network Error: Could not connect"));
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Network Error: Could not create socket"));
        }
        return socket;
    }

    private void closeHost(Socket sock) throws KVException {
        try {
            sock.close();
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Unknown Error: Couldn’t close connection"));
        }
    }

    public void put(String key, String value) throws KVException {
        Socket socket = this.connectHost();
        KVMessage kvReq = new KVMessage("putreq");
        kvReq.setKey(key);
        kvReq.setValue(value);
        kvReq.sendMessage(socket);
        Boolean success = false;

        try {
            socket.shutdownOutput();
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Unknown Error: Couldn’t shut down output"));
        }

        try {
            InputStream in = socket.getInputStream();
            KVMessage kvResp = new KVMessage(in);
            if(kvResp.getMessage().equals("Success")) {
                success = true;
            } else if(kvResp.getKey() == null){
                throw new KVException(kvResp);
            }
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
        }
        
        // if(!success)
        //     throw new KVException(new KVMessage("resp", "Unknown Error: Put failed"));

        this.closeHost(socket);
    }

    public String get(String key) throws KVException {
        Socket socket = this.connectHost();
        KVMessage kvReq = new KVMessage("getreq");
        kvReq.setKey(key);
        kvReq.sendMessage(socket);
        String result = "";

        try {
            socket.shutdownOutput();
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Unknown Error: Couldn’t shut down output"));
        }

        try {
            InputStream in = socket.getInputStream();
            KVMessage kvResp = new KVMessage(in);
            if(kvResp.getMessage() != null) {
                throw new KVException(kvResp);
            }
            result = kvResp.getValue();
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
        }

        this.closeHost(socket);
        return result;
    }

    public void del(String key) throws KVException {
        Socket socket = this.connectHost();
        KVMessage kvReq = new KVMessage("delreq");
        kvReq.setKey(key);
        kvReq.sendMessage(socket);

        try {
            socket.shutdownOutput();
        } catch(IOException io) {
            throw new KVException(new KVMessage("resp", "Unknown Error: Couldn’t shut down output"));
        }

		// try {
		// 	InputStream in = socket.getInputStream();
		// 	KVMessage kvResp = new KVMessage(in);
  //           if(!kvResp.getMessage().equals("Success"))
  //               throw new KVException(new KVMessage("resp", "Unknown Error: Delete failed"));

		// } catch(IOException io) {
		// 	throw new KVException(new KVMessage("resp", "Network Error: Could not receive data"));
		// }

        this.closeHost(socket);
    }
}
