package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

public class KVServerTest {

	KVServer testServer = new KVServer(10, 10);
	
	@Test
	public void test() {
		try{
			testServer.put("Key1", "Value1");
		} catch(KVException kve){
			fail("Unsuccessful 'put': "+kve.getMsg().getMessage());
		}
		
		try{
			testServer.get("Key1");
		} catch(KVException kve){
			fail("Unsuccessful 'get': " +kve.getMsg().getMessage());
		}
		
		try{
			testServer.del("Key1");
		} catch(KVException kve){
			fail("Unsuccessful 'del': " +kve.getMsg().getMessage());
		}
		
		try{
			testServer.del("Key1");
			fail("Bad del behavior when deleting non-existent key");
		} catch(KVException kve){
			if(!kve.getMsg().getMessage().equals("Does not exist")){
				fail("Bad del behavior when deleting non-existent key");
			}
		}
		
		try{
			testServer.put("1", "2");
			testServer.put("1", "3");
			
			if(!testServer.get("1").equals("3")){
				fail("Bad behavior when overwriting!");
			}
		} catch(KVException kve){
			fail ("Exception when overwriting!");
		}
	}

}
