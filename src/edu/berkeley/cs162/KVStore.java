/**
 * Persistent Key-Value storage layer. Current implementation is transient,
 * but assume to be backed on disk when you do your project.
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


import java.util.Dictionary;
import java.util.Hashtable;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a dummy KeyValue Store. Ideally this would go to disk,
 * or some other backing store. For this project, we simulate the disk like
 * system using a manual delay.
 *
 */
public class KVStore implements KeyValueInterface {
	private Hashtable<String, String> store 	= null;

	public KVStore() {
		resetStore();
	}

	private void resetStore() {
		store = new Hashtable<String, String>();
	}

	public void put(String key, String value) throws KVException {

		AutoGrader.agStorePutStarted(key, value);

		System.out.println("KVSTORE put called with: " + key + " " + value);

		try {
			putDelay();
			store.put(key, value);
		} finally {
			AutoGrader.agStorePutFinished(key, value);
		}
	}

	public String get(String key) throws KVException {
		AutoGrader.agStoreGetStarted(key);

		try {
			getDelay();
			String retVal = this.store.get(key);
			if (retVal == null) {
			    KVMessage msg = new KVMessage("resp", "key \"" + key + "\" does not exist in store");
			    throw new KVException(msg);
			}
			return retVal;
		} finally {
			AutoGrader.agStoreGetFinished(key);
		}
	}

	public void del(String key) throws KVException {
		AutoGrader.agStoreDelStarted(key);

		try {
			delDelay();
			if(key != null)
				this.store.remove(key);
		} finally {
			AutoGrader.agStoreDelFinished(key);
		}
	}

	private void getDelay() {
		AutoGrader.agStoreDelay();
	}

	private void putDelay() {
		AutoGrader.agStoreDelay();
	}

	private void delDelay() {
		AutoGrader.agStoreDelay();
	}


    public String toXML() {
        // TODO: implement me
        return null;
    }

    public void dumpToFile(String fileName) {

        try {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // create XML document
        Document doc = docBuilder.newDocument();

        // create KVStore root element
        Element rootElement = doc.createElement("KVStore");
        doc.appendChild(rootElement);

        // start iterating through KV Pairs

        for ( String key : store.keySet() ) {

            //create KVPair node
            Element kvPair = doc.createElement("KVPair");
            rootElement.appendChild(kvPair);

            //create Key node
            Element xmlKey = doc.createElement("Key");
            xmlKey.appendChild(doc.createTextNode(key));

            // create Value node
            Element xmlValue = doc.createElement("Value");
            xmlValue.appendChild(doc.createTextNode(store.get(key)));

            kvPair.appendChild(xmlKey);
            kvPair.appendChild(xmlValue);
        }

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // formatting of the XML document that is written to file
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult fileResult = new StreamResult(new File(fileName));

        // Output to console for testing
        StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
        transformer.transform(source, fileResult);

        // System.out.println("File saved!");

      } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
      } catch (TransformerException tfe) {
            tfe.printStackTrace();
      }
    }


    /**
     * Replaces the contents of the store with the contents of a file
     * written by dumpToFile; the previous contents of the store are lost.
     * @param fileName the file to be read.
     */
    public void restoreFromFile(String fileName) {

        try {

            File xmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            String rootElement = doc.getDocumentElement().getNodeName();

            // System.out.println("Root Element: " + rootElement);

            // TODO: Change print statement into exception
            if (rootElement != "KVStore") {
                System.out.println("Root element does not equal KVStore");
                return;
            }

            // attempt to get all the KVPairs in the xml document
            NodeList keyValues = doc.getElementsByTagName("KVPair");

            // TODO: Change print statement into exception
            if (keyValues.getLength() == 0) {
                System.out.println("KVPair node not present in document for restoreFromFile");
                return;
            }


            //reset the store in preparation for iterating through XML
            store.clear();

            //iterate through the KV Pairs in the xml file
            for (int temp = 0; temp < keyValues.getLength(); temp++) {

                Node node = keyValues.item(temp);

                Element KVPair = (Element) node;

                Node keyNode = KVPair.getElementsByTagName("Key").item(0);
                Node valueNode = KVPair.getElementsByTagName("Value").item(0);

                // no XML tags that match key or value
                // TODO: Change print statement into exception
                if (keyNode == null || valueNode == null) {
                    System.out.println("Not valid XML document for restoreFromFile");
                    return;
                }

                String value = keyNode.getTextContent();
                String key = valueNode.getTextContent();

                // System.out.println("Key: " + key);
                // System.out.println("Value: " + value);

                store.put(key, value);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing of KVStore");
        KVStore dataStore = new KVStore();
        try {
            dataStore.put("3","7");
            dataStore.put("5", "5");
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Dumping file store contents to dump1.xml");
        dataStore.dumpToFile("dump1.xml");

        System.out.println("\nRestoring file store contents from file.xml");
        dataStore.restoreFromFile("file.xml");

        System.out.println("\nDumping file store contents to dump2.xml");
        dataStore.dumpToFile("dump2.xml");

        // System.out.println("Store: " + dataStore.store);
    }
}
