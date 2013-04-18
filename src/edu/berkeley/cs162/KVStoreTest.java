package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;
import java.io.FileNotFoundException;

public class KVStoreTest {

	@Test
	public void testEmptyKeyStoreToXML() {
		String expectedResult =
	            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<KVStore/>";
		try {
			KVStore dataStore = new KVStore();
			assertTrue(expectedResult.trim().equals(dataStore.toXML().trim()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void testNonEmptyKeyStoreToXML() {
		String expectedResult =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
			    + "\n" + "<KVStore>"
			    + "\n  " + "<KVPair>"
			    + "\n    " + "<Key>Test Key</Key>"
			    + "\n    " + "<Value>Test Value</Value>"
			    + "\n  " + "</KVPair>"
			    + "\n" + "</KVStore>";

		try {
			KVStore dataStore = new KVStore();
			dataStore.put("Test Key", "Test Value");

//			System.out.println(expectedResult.trim());
//			System.out.println(dataStore.toXML().trim());

			assertTrue(expectedResult.trim().equals(dataStore.toXML().trim()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testRestoreFromInvalidFile() {

		KVStore dataStore = new KVStore();

		try {
			dataStore.put("Key", "value");
			dataStore.put("Key1", "value1");


			dataStore.restoreFromFile("invalid_file");

		// check to see that the store is unchanged after the restore from invalid file

		assertTrue(dataStore.get("Key").equals("value"));
		assertTrue(dataStore.get("Key1").equals("value1"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDumpAndRestoreFromValidFile() {

		KVStore dataStore = new KVStore();

		try {
			dataStore.put("5", "5");
			dataStore.put("3", "7");
			dataStore.dumpToFile("dump.xml");



			// dump.xml is in the src/edu/berkeley/cs162 dir
			// dump.xml:
			//<?xml version="1.0" encoding="UTF-8" standalone="no"?>
			//<KVStore>
			//  <KVPair>
			//    <Key>5</Key>
			//    <Value>5</Value>
			//  </KVPair>
			//  <KVPair>
			//    <Key>3</Key>
			//    <Value>7</Value>
			//  </KVPair>
			//</KVStore>

			dataStore.put("Key", "value");
			dataStore.put("Key1", "value");

			dataStore.restoreFromFile("dump.xml");

			// check to see that the store is changed after the restore from the valid file
			assertTrue(dataStore.get("5").equals("5"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		


	}
}