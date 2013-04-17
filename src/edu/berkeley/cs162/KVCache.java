/**
 * Implementation of a set-associative cache.
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

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.*;

import java.io.StringWriter;

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
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on the eviction policy.
 */
public class KVCache implements KeyValueInterface {
	private int numSets = 100;
	private int maxElemsPerSet = 10;

	private int maxKey = 256;
	private int maxVal = 262144;

	//private ReentrantReadWriteLock.WriteLock[] setlocks;
	private LinkedList<String>[] sets;
	private HashMap<String,Boolean> usedbits = new HashMap<String,Boolean>();
	private HashMap<String,String> contents = new HashMap<String,String>();
	private HashMap<String,Boolean> validbits = new HashMap<String,Boolean>();
	private ReentrantReadWriteLock[] setlocks = new ReentrantReadWriteLock[numSets];

	/**
	 * Creates a new LRU cache.
	 * @param cacheSize	the maximum number of entries that will be kept in this cache.
	 */
	public KVCache(int numSets, int maxElemsPerSet) {
		numSets = numSets;
		maxElemsPerSet = maxElemsPerSet;
		sets = (LinkedList<String>[]) new LinkedList[numSets];
		// TODO: Implement Me!
		for (int i = 0 ; i < numSets ; i++){
			setlocks[i] = new ReentrantReadWriteLock();
			sets[i] = new LinkedList<String>();
		}
	}

	/**
	 * Retrieves an entry from the cache.
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key the key whose associated value is to be returned.
	 * @return the value associated to this key, or null if no value with this key exists in the cache.
	 */
	public String get(String key) {
		// Must be called before anything else
		AutoGrader.agCacheGetStarted(key);
		AutoGrader.agCacheGetDelay();
		// TODO: Implement Me!

		String retval = null;
		int setId = getSetId(key);
		LinkedList set = sets[setId];

		if (set.contains(key)){
			set.remove(key);
			set.add(key);
			usedbits.put(key,true);
			retval = contents.get(key);
		}

		// Must be called before returning
		AutoGrader.agCacheGetFinished(key);
		return retval;
	}

	/**
	 * Adds an entry to this cache.
	 * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
	 * If the cache is full, an entry is removed from the cache based on the eviction policy
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key	the key with which the specified value is to be associated.
	 * @param value	a value to be associated with the specified key.
	 * @return true is something has been overwritten
	 */
	public void put(String key, String value) {
		// Must be called before anything else
		AutoGrader.agCachePutStarted(key, value);
		AutoGrader.agCachePutDelay();

		if (key.length() > maxKey || value.length() > maxVal ){
			System.out.println("The key or Value is too big!!!");
		}

		int setId = getSetId(key);
		LinkedList set = sets[setId];

		if (set.contains(key)){
			set.remove(key);
			set.add(key);
			contents.put(key,value);
			usedbits.put(key,false);
		}
		else{
			if(set.size() == maxElemsPerSet){
				boolean finished = false;
				for (int i = 0 ; i < maxElemsPerSet ; i++){
					String oldkey = (String)set.get(i);
					if(!usedbits.get(oldkey)){
						finished = true;
						set.remove(oldkey);
						set.add(key);
						contents.remove(oldkey);
						contents.put(key, value);
						break;
					}
				}
				if (!finished){
					String oldkey = (String)set.get(0);
					usedbits.put(oldkey,false);
					set.remove(oldkey);
					set.add(key);
					contents.remove(oldkey);
					contents.put(key, value);
				}
			}
			else{
				set.add(key);
				usedbits.put(key,false);
				contents.put(key,value);
			}
		}

		// Must be called before returning
		AutoGrader.agCachePutFinished(key, value);
	}

	/**
	 * Removes an entry from this cache.
	 * Assumes the corresponding set has already been locked for writing.
	 * @param key	the key with which the specified value is to be associated.
	 */
	public void del (String key) {
		// Must be called before anything else
		AutoGrader.agCacheGetStarted(key);
		AutoGrader.agCacheDelDelay();

		// TODO: Implement Me!
		int setId = getSetId(key);
		LinkedList set = sets[setId];

		if (set.contains(key)){
			set.remove(key);
			usedbits.put(key,false);
			contents.remove(key);
		}

		// Must be called before returning
		AutoGrader.agCacheDelFinished(key);
	}

	/**
	 * @param key
	 * @return	the write lock of the set that contains key.
	 */
	public WriteLock getWriteLock(String key) {
		// TODO: Implement Me!
		int setId = getSetId(key);
		return setlocks[setId].writeLock();
	}

	/**
	 *
	 * @param key
	 * @return	set of the key
	 */
	private int getSetId(String key) {
		return Math.abs(key.hashCode()) % numSets;
	}

	public String toXML() {

		try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // create XML document
            Document doc = docBuilder.newDocument();

            // create KVStore root element
            Element rootElement = doc.createElement("KVCache");
            doc.appendChild(rootElement);

            return KVStore.toString(doc);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            throw new RuntimeException("Error parsing document");
        } catch (Exception ex) {
            throw new RuntimeException("Error generating document");
        }

	}

	public static void main (String[] args) {

		System.out.println("TESTING KVCACHE");
		KVCache cache = new KVCache(100, 10);

		System.out.println("\ncalling toXML()");
		System.out.println(cache.toXML());

	}

}