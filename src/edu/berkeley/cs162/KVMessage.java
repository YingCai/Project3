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

	/*TODO: EUGENE - HELPER METHODS --------------------------------------------------*/
	public KVMessage helperKVException(String msg) throws KVException {
		return new KVMessage("resp", msg);
	}
	

    public boolean checkMsgType(String msgType) {
        return (msgType.equals("getreq") || msgType.equals("putreq") || msgType.equals("delreq") || msgType.equals("resp"));
    }
    

	public String parseElement(Element kvmessage, String key) {
		if (kvmessage.getElementsByTagName(key).getLength() != 0) {
			Element value = (Element) kvmessage.getElementsByTagName(key).item(0);
			return value.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	public boolean isValidKey(String k) {
		return (k.length() > 0 && k.length() <= 256);
	}
	
	public boolean isValidValue(String v) {
		return (v.length() > 0 && v.length() <= 256000);
	}

	//THROWS AN UNCHECKED DOM-EXCEPTION
	public void addToKVM(String k, String v, Element kvm, Document doc) {
		Element new_element = doc.createElement(k);
		new_element.appendChild(doc.createTextNode(v));
		kvm.appendChild(new_element);
	}
    /*--------------------------------------------------------------------------------*/

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
		
		//parse document
		try {
			document = documentbuilder.parse(filterinput); //this is problem
		}
		
		catch (IllegalArgumentException e) {
			throw new KVException(helperKVException("Network Error: Could not receive data"));
		}
		catch (IOException e) {
			throw new KVException(helperKVException("Network Error: Could not receive data"));
		}
		catch (SAXException e) {
			//Q: why do we keep getting this?
			//A: looks like this is often given when either S or C crashes and leaves unclosed socket
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
		Element kvmessage = (Element) node;

		//parse the KVMessage
        //The variable element is the KVMessage
		String new_msgType = kvmessage.getAttribute("type");
		if (new_msgType.equals("resp")) {
			String new_key = parseElement(kvmessage, "Key");
			String new_value = parseElement(kvmessage, "Value");
			if (new_key != null && new_value != null ) {
				if (!isValidKey(new_key)) {
		            throw new KVException(helperKVException("Oversized key"));
				}
				if (!isValidValue(new_value)) {
		            throw new KVException(helperKVException("Oversized value"));
				}
				else {
					this.msgType = new_msgType;
					this.key = new_key;
					this.value = new_value;
				}
			}
			else {
				String new_message = parseElement(kvmessage, "Message");
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
				String new_key = parseElement(kvmessage, "Key");
				if (new_key != null){
					if (!isValidKey(new_key)) {
			            throw new KVException(helperKVException("Oversized key"));
					}
					else {
						this.msgType = new_msgType;
						this.key = new_key;
					}
				}
				else {
		            throw new KVException(helperKVException("Message format incorrect"));
				}
			}
			else if (new_msgType.equals("putreq")) {
				String new_key = parseElement(kvmessage, "Key");
				String new_value = parseElement(kvmessage, "Value");
				if (new_key != null && new_value != null ) {
					if (!isValidKey(new_key)) {
			            throw new KVException(helperKVException("Oversized key"));
					}
					if (!isValidValue(new_value)) {
			            throw new KVException(helperKVException("Oversized value"));
					}
					else {
						this.msgType = new_msgType;
						this.key = new_key;
						this.value = new_value;
					}
				}
				else {
					throw new KVException(helperKVException("Message format incorrect"));
				}
			}
			else {
				throw new KVException(helperKVException("Unknown Error: msgType Invalid"));
			}
		}
	}

	/**
	 * Generate the XML representation for this message.
	 * @return the XML String
	 * @throws KVException if not enough data is available to generate a valid KV XML message
	 */
	// TODO: Eugene. Done
	public String toXML() throws KVException {
		//check msgType
		if (msgType == null || !checkMsgType(msgType)) {
			throw new KVException(helperKVException("Unkown Error: msgType Invalid"));
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
		Element kvmessage = null;
		try {
			kvmessage = document.createElement("KVMessage");
			Attr type = null;
			if (checkMsgType(this.msgType)){
				type = document.createAttribute("type");
				type.setValue(msgType);
			}
			else {
				throw new KVException(helperKVException("Unkown Error: msgType Invalid"));
			}

			kvmessage.setAttributeNode(type);
			document.appendChild(kvmessage);

			//PARSE VERBOSELY. EACH TYPE 1 AT A TIME
			if (msgType.equals("resp")) {
				if (this.message != null) {
					addToKVM("Message", this.message, kvmessage, document);
				}
				else if (this.key != null && this.value != null){
					if (!isValidKey(this.key)) {
			            throw new KVException(helperKVException("Oversized key"));
					}
					if (!isValidValue(this.value)) {
			            throw new KVException(helperKVException("Oversized value"));
					}
					else {
						addToKVM("Key", this.key, kvmessage, document);
						addToKVM("Value", this.value, kvmessage, document);
					}
				}
				else {
					throw new KVException(helperKVException("Message format incorrect"));
				}
			}
			else if (msgType.equals("getreq") || msgType.equals("delreq")) {
				if (this.key != null) {
					if (!isValidKey(this.key)) {
			            throw new KVException(helperKVException("Oversized key"));
					}
					else {
						addToKVM("Key", this.key, kvmessage, document);
					}
				}
				else {
					throw new KVException(helperKVException("Message format incorrect"));
				}

			}
			else if (msgType.equals("putreq")) {
				if (this.key != null && this.value != null){
					if (!isValidKey(this.key)) {
			            throw new KVException(helperKVException("Oversized key"));
					}
					if (!isValidValue(this.value)) {
			            throw new KVException(helperKVException("Oversized value"));
					}
					else {
						addToKVM("Key", this.key, kvmessage, document);
						addToKVM("Value", this.value, kvmessage, document);
					}
				}
				else {
					throw new KVException(helperKVException("Message format incorrect"));
				}
			}
			else {
				throw new KVException(helperKVException("Unkown Error: msgType Invalid"));
			}

		}
		catch (DOMException e){
			throw new KVException(helperKVException("Unknown Error: DOM parse error"));
		}

		//Prepare to write out
		Transformer transformer = null;
		try {
			TransformerFactory transformerfactory = TransformerFactory.newInstance();
			transformer = transformerfactory.newTransformer();

			DOMSource dom = new DOMSource(kvmessage);
			StringWriter stringwriter = new StringWriter();
			StreamResult streamresult = new StreamResult(stringwriter);
			transformer.transform(dom, streamresult);
			String xml = stringwriter.toString();
			return xml;
		}
		catch (TransformerException e) {
			throw new KVException(helperKVException("Unkown Error: Transformer Configuration Error"));
		}
		catch (DOMException e){
			throw new KVException(helperKVException("Unknown Error: DOM parse error"));
		}

	}

	// TODO: Eugene. Done
	public void sendMessage(Socket sock) throws KVException {

		try {
			OutputStream outputStream = sock.getOutputStream();
			String xml = toXML();
			//TODO: Check this line
			outputStream.write(xml.getBytes("UTF-8"));
			outputStream.flush();
		}
		catch (IOException e) {
			throw new KVException(helperKVException("Network Error: Could not send data"));
		}
	}
}
