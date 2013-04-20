package edu.berkeley.cs162;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.Test;

public class KVMessageTest {

	@Test
	public void constructorTest() {
		print("1 ARG CONSTRUCTORTEST");
		KVMessage getreq = null;
		KVMessage putreq = null;
		KVMessage delreq = null;
		KVMessage resp = null;
		try {
			getreq = new KVMessage("getreq");
			putreq = new KVMessage("putreq");
			delreq = new KVMessage("delreq");
			resp = new KVMessage("resp");
		}
		catch (KVException e) {
			fail("Incorrectly rejected valid msgtype");
		}
		assertEquals(getreq.getMsgType(),"getreq");
		assertEquals(putreq.getMsgType(),"putreq");
		assertEquals(delreq.getMsgType(),"delreq");
		assertEquals(resp.getMsgType(),"resp");
		
		try {
			KVMessage bad = new KVMessage("wat");
			fail("allowed invalid msgType");
		}
		catch (KVException e) {
		}
		print("passed");
		
	}
	
	@Test
	public void constructor2Test() {
		print("2 ARG CONSTRUCTORTEST");
		KVMessage resp = null;
		try {
			resp = new KVMessage("resp","D");
		}
		catch (KVException e) {
			fail("Incorrectly rejected valid msgtype");
		}
		assertEquals(resp.getMsgType(),"resp");
		assertEquals(resp.getMessage(),"D");
		
		try {
			KVMessage bad = new KVMessage("wat");
			fail("allowed invalid msgType");
		}
		catch (KVException e) {
		}
		
		print("passed");
		
	}
	
	@Test
	public void constructor3Test() {
		print("INPUTSTREAM CONSTRUCTORTEST");
		
	    String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>key</Key></KVMessage>";
	    InputStream inp = null;
		try {
			inp = new ByteArrayInputStream(msg.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			fail("something went wrong with encoding");
		}
	      try {
			KVMessage kvm = new KVMessage(inp);
		    assertTrue(kvm.getMsgType().equals("getreq"));
		    assertTrue(kvm.getKey().equals("key"));
		} catch (KVException e) {
			fail("could not parse");
		}
	      
	    String invalid_msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?<KVMessage type=\"getreq\"><Key>key</Key></KVMessage>";
	    try {
			inp = new ByteArrayInputStream(invalid_msg.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			fail("something went wrong with encoding");
		}
	    try {
			KVMessage kvm = new KVMessage(inp);
			fail("allowed bad msg");
		} catch (KVException e) {
		}
		
		print("passed");
		
	}
	
	@Test
	public void toXMLTest() {
		print("TOXML CONSTRUCTORTEST");
		
		String xml = null;
		KVMessage getreq = null;
		KVMessage putreq = null;
		KVMessage delreq = null;
		KVMessage resp = null;
		KVMessage resp_kv = null;
		try {
			getreq = new KVMessage("getreq");
			putreq = new KVMessage("putreq");
			delreq = new KVMessage("delreq");
			resp = new KVMessage("resp","Success");
			resp_kv = new KVMessage("resp");
		}
		catch (KVException e) {
			fail("Incorrectly rejected valid msgtype");
		}
		
		char ch = 'a';
		char[] chars = new char[260];
		Arrays.fill(chars, ch);
		String oversizedKey = new String(chars);
		
		char[] chars2 = new char[260000];
		Arrays.fill(chars2, ch);
		String oversizedValue = new String(chars2);

		try {
			getreq.setKey("A");
			xml = getreq.toXML();
			assertTrue(xml.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>A</Key></KVMessage>"));
		} catch (KVException e) {
			fail("toXML() thew an exception");
		}
		try {
			getreq.setKey(oversizedKey);
			xml = getreq.toXML();
			fail("allowed oversized key");
		} catch (KVException e) {
		}
		
		try {
			putreq.setKey("B");
			putreq.setValue("2");
			xml = putreq.toXML();
			assertTrue(xml.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>B</Key><Value>2</Value></KVMessage>"));
		} catch (KVException e) {
			fail("toXML() thew an exception");
		}
		try {
			putreq.setKey(oversizedKey);
			xml = putreq.toXML();
			fail("allowed oversized key");
		} catch (KVException e) {
		}
		try {
			putreq.setKey(oversizedValue);
			xml = putreq.toXML();
			fail("allowed oversized value");
		} catch (KVException e) {
		}
		
		
		try {
			delreq.setKey("C");
			xml = delreq.toXML();
			assertTrue(xml.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"delreq\"><Key>C</Key></KVMessage>"));
		} catch (KVException e) {
			fail("toXML() thew an exception");
		}
		try {
			delreq.setKey(oversizedKey);
			xml = delreq.toXML();
			fail("allowed oversized key");
		} catch (KVException e) {
		}		
	
		
		try {
			resp_kv.setKey("D");
			resp_kv.setValue("4");
			xml = resp_kv.toXML();
			assertTrue(xml.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"resp\"><Key>D</Key><Value>4</Value></KVMessage>"));
		} catch (KVException e) {
			fail("toXML() thew an exception");
		}
		try {
			resp_kv.setKey(oversizedKey);
			xml = resp_kv.toXML();
			fail("allowed oversized key");
		} catch (KVException e) {
		}
		try {
			resp_kv.setKey(oversizedValue);
			xml = resp_kv.toXML();
			fail("allowed oversized value");
		} catch (KVException e) {
		}
		
		try {
			xml = resp.toXML();
			assertTrue(xml.equals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"resp\"><Message>Success</Message></KVMessage>"));
		} catch (KVException e) {
			fail("toXML() thew an exception");
		}
			
		
		print("passed");
		
	}

	private void print(String x) {
		System.out.println(x);
	}
}
