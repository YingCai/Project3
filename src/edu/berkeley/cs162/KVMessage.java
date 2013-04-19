/**
 * XML Parsing library for the key-value store
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This is the object that is used to generate messages the XML based messages
 * for communication between clients and servers.
 */
public class KVMessage {
	private String msgType = null;
	private String key = null;
	private String value = null;
	private String message = null;

	public final String getKey() {
		return key;
	}

	public final void setKey(String key) {
		this.key = key;
	}

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	public String getMsgType() {
		return msgType;
	}

	/* Solution from http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html */
	private class NoCloseInputStream extends FilterInputStream {
	    public NoCloseInputStream(InputStream in) {
	        super(in);
	    }

	    public void close() {} // ignore close
	}

	public KVMessage helperKVException(String msg) throws KVException {
		return new KVMessage("resp", msg);
	}

	/***
	 *
	 * @param msgType
	 * @throws KVException of type "resp" with message "Message format incorrect" if msgType is unknown
	 */
    //TODO: Eugene. done
	public KVMessage(String msgType) throws KVException {
        if (!checkMsgType(msgType)) {
            throw new KVException(helperKVException("Message format incorrect"));
        }
        this.msgType = msgType;
	}

    //TODO: Eugene. done
	public KVMessage(String msgType, String message) throws KVException {
	    if (!checkMsgType(msgType)) {
            throw new KVException(helperKVException("Message format incorrect"));
        }
        this.msgType = msgType;
        this.message = message;
	}


    //TODO: Eugene. done
    public boolean checkMsgType(String msgType) {
        return (msgType.equals("getreq") || msgType.equals("putreq") || msgType.equals("delreq") || msgType.equals("resp"));
    }

	 /***
     * Parse KVMessage from incoming network connection
     * @param sock
     * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
     * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
     * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
     * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type.
     */
	// TODO: Eugene. done
	public KVMessage(InputStream input) throws KVException {
		DocumentBuilder documentbuilder = null;
		Document document = null;
        NoCloseInputStream filterinput = new NoCloseInputStream(input);

	//create document builder
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			documentbuilder = factory.newDocumentBuilder();
		}
		catch (FactoryConfigurationError e){
			throw new KVException(helperKVException("Unknown Error: Factory Configuration Exception"));
		}
		catch (ParserConfigurationException e){
			throw new KVException(helperKVException("Unknown Error: Parser Configuration Exception"));
		}
		
		System.out.println(filterinput);
		//parse document
		try {
			document = documentbuilder.parse(filterinput); //this is problem
		}
		
		catch (IllegalArgumentException e) {
			throw new KVException(helperKVException("Unknown Error: InputStream is null"));
		}
		catch (IOException e) {
			throw new KVException(helperKVException("Network Error: Could not receive data"));
		}
		catch (SAXException e) {
			System.out.println(e.getMessage());
			throw new KVException(helperKVException("XML Error: Received unparseable message"));
		}

		//check document format
		if (!document.getXmlEncoding().equals("UTF-8")) {
			throw new KVException(helperKVException("Unknown Error: Encoding not UTF-8"));
		}
		if (document.getElementsByTagName("KVMessage").getLength() != 1) {
			throw new KVException(helperKVException("Unknown Error: Multiple KVMessages in one send"));
		}
		Node node = document.getElementsByTagName("KVMessage").item(0);
		Element element = (Element) node;

		//parse the KVMessage
		//TODO: WHAT IF THERE IS OTHER STUFF IN THE MESSAGE?!?
		String new_msgType = element.getAttribute("type");
		System.out.println(new_msgType);
		if (new_msgType.equals("resp")) {
			String new_key = parseElement(element, "key");
			String new_value = parseElement(element, "value");
			if (new_key != null && new_value != null){
				this.msgType = new_msgType;
				this.key = new_key;
				this.value = new_value;
			}
			else {
				String new_message = parseElement(element, "message");
				if (new_message != null) {
					this.msgType = new_msgType;
					this.message = new_message;
				}
				else {
		            throw new KVException(helperKVException("Message format incorrect"));
				}
			}
		}
		else {
			if (new_msgType.equals("getreq") || new_msgType.equals("delreq")) {
				String new_key = parseElement(element, "key");
				if (new_key != null){
					this.msgType = new_msgType;
					this.key = new_key;
				}
				else {
		            throw new KVException(helperKVException("Message format incorrect"));
				}
			}
			else if (new_msgType.equals("putreq")) {
				String new_key = parseElement(element, "key");
				String new_value = parseElement(element, "value");
				System.out.println("NEW_KEY IS " + new_key);
				System.out.println("NEW_VALUE IS " + new_value);
				if (new_key != null && new_value != null){
					this.msgType = new_msgType;
					this.key = new_key;
					this.value = new_value;
				}
				else {
					throw new KVException(helperKVException("Message format incorrect"));
				}

			}
			else {
				throw new KVException(helperKVException("Message format incorrect"));
			}
		}
	}

	public String parseElement(Element element, String key) {
		System.out.println(element);
		if (element.getElementsByTagName(key).getLength() != 0) {
			Element value = (Element) element.getElementsByTagName(key).item(0);
			return value.getFirstChild().getNodeValue();
		}
		return null;
	}

	/**
	 * Generate the XML representation for this message.
	 * @return the XML String
	 * @throws KVException if not enough data is available to generate a valid KV XML message
	 */
	// TODO: Eugene. UNIMPLEMENTED
	public String toXML() throws KVException {
		//check msgType
		if (msgType == null || !checkMsgType(msgType)) {
			throw new KVException(helperKVException("Unkown Error: Invalid msgType"));
		}

		//document builder
		DocumentBuilder documentbuilder = null;
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			documentbuilder = factory.newDocumentBuilder();
			document = documentbuilder.newDocument();
		}
		catch (FactoryConfigurationError e){
			throw new KVException(helperKVException("Unknown Error: Factory Configuration Exception"));
		}
		catch (ParserConfigurationException e){
			throw new KVException(helperKVException("Unknown Error: Parser Configuration Exception"));
		}

		//make element
		Element root = null;
		try {
			root = document.createElement("KVMessage");
			Attr msgType_Attr = document.createAttribute("type");

			if (checkMsgType(this.msgType)){
				msgType_Attr.setValue(msgType);
			}
			else {
				throw new KVException(helperKVException("Unkown Error: msgType Invalid"));
			}

			root.setAttributeNode(msgType_Attr);
			document.appendChild(root);

			//PARSE VERBOSELY. EACH TYPE 1 AT A TIME
			if (msgType.equals("resp")) {
				if (this.message != null) {
					addToElement("message", this.message, root, document);
				}
				else if (this.key != null && this.value != null){
					addToElement("key", this.key, root, document);
					addToElement("value", this.value, root, document);
				}
				else {
					throw new KVException(helperKVException("Unknown Error: Message format incorrect"));
				}
			}
			else if (msgType.equals("getreq") || msgType.equals("delreq")) {
				if (this.key != null) {
					addToElement("key", this.key, root, document);
				}
				else {
					throw new KVException(helperKVException("Unknown Error: Message format incorrect"));
				}

			}
			else if (msgType.equals("putreq")) {
				if (this.key != null && this.value != null){
					addToElement("key", this.key, root, document);
					addToElement("value", this.value, root, document);
				}
				else {
					throw new KVException(helperKVException("Unknown Error: Message format incorrect"));
				}
			}
			else {
				throw new KVException(helperKVException("Unkown Error: msgType Invalid"));
			}

		}
		catch (DOMException e){
			throw new KVException(helperKVException("Unkown Error: improper XML values"));
		}

		//Prepare to write out
		Transformer transformer = null;
		try {
			TransformerFactory transformerfactory = TransformerFactory.newInstance();
			transformer = transformerfactory.newTransformer();

			DOMSource dom = new DOMSource(root);
			StringWriter stringwriter = new StringWriter();
			StreamResult streamresult = new StreamResult(stringwriter);
			transformer.transform(dom, streamresult);
			System.out.println(stringwriter.toString());
			return stringwriter.toString();
		}
		catch (TransformerException e) {
			throw new KVException(helperKVException("Unkown Error: Transformer Configuration Error"));
		}
		catch (DOMException e){
			throw new KVException(helperKVException("Unkown Error: improper XML values"));
		}

	}

	//THROWS AN UNCHECKED DOM-EXCEPTION
	public void addToElement(String k, String v, Element element, Document document) {
		Element new_element = document.createElement(k);
		new_element.appendChild(document.createTextNode(v));
		element.appendChild(new_element);
	}

	// TODO: Eugene. Done
	public void sendMessage(Socket sock) throws KVException {

		try {
			OutputStream outputStream = sock.getOutputStream();
			String xml = toXML();
			
			System.out.println("on the socket to send: " + xml);
			//TODO: Check this line
			outputStream.write(xml.getBytes("UTF-8"));
			outputStream.flush();
		}
		catch (IOException e) {
			throw new KVException(helperKVException("Network Error: Could not send data"));
		}
	}
}
