package edu.berkeley.cs162;

import static org.junit.Assert.*;

import org.junit.Test;

public class KVCacheTest {

	@Test
	public void test() {
		KVCache testCache = new KVCache(3,3);
		
		testCache.put("three","this is three");
		assertEquals("get should return put","this is three",testCache.get("three"));

		testCache.put("three","this is now three");
		assertEquals("three should overwrite","this is now three",testCache.get("three"));
		
		testCache = new KVCache(1,3);
		
		testCache.put("three","this is three");
		testCache.put("four","this is four");
		testCache.put("five","this is five");
		testCache.put("six","this is six");
		//cache should have 4 5 6 with none referenced
		
		assertEquals("three should be kicked",null,testCache.get("three"));
		assertEquals("four should not be kicked","this is four",testCache.get("four"));
		//cache should have 5 6 4 with 4 referenced
		
		testCache.put("seven","this is seven");
		testCache.put("eight","this is eight");
		//cache should have 4 7 8 with 4 referenced
		testCache.put("nine", "this is nine");
		//cache should have 4 8 9 with 4 referenced
		
		assertEquals("four should not be kicked","this is four",testCache.get("four"));
		assertEquals("seven should be kicked",null,testCache.get("seven"));
		assertEquals("eight should be in cache","this is eight",testCache.get("eight"));
		assertEquals("nine should be in cache","this is nine",testCache.get("nine"));
		//cache should have 4 8 9 with all referenced
		
		testCache.put("ten", "this is ten");
		//cache should have 8 9 10 with 8 and 9 referenced
		
		assertEquals("four should be kicked",null,testCache.get("four"));
		
		testCache.del("eight");
		testCache.del("nine");
		testCache.del("ten");
		//cache should now be empty

		assertEquals("eight should be kicked",null,testCache.get("eight"));
		assertEquals("nine should be kicked",null,testCache.get("nine"));
		assertEquals("ten should be kicked",null,testCache.get("ten"));
		
		//Test with multiple sets
		testCache = new KVCache(2,2);
		
		testCache.put("0", "this is zero");
		testCache.put("1", "this is one");
		testCache.put("2", "this is two");
		testCache.put("3", "this is three");
		//two should be in each set
		
		assertEquals("zero should be in cache","this is zero",testCache.get("0"));
		assertEquals("one should be in cache","this is one",testCache.get("1"));
		assertEquals("two should be in cache","this is two",testCache.get("2"));
		assertEquals("three should be in cache","this is three",testCache.get("3"));
		
		testCache.put("4", "this is four");
		testCache.put("5", "this is five");
		//cache should contain [2 4] [3 5]
		
		assertEquals("zero should NOT be in cache",null,testCache.get("0"));
		assertEquals("one should NOT be in cache",null,testCache.get("1"));
		
		assertEquals("two should be in cache","this is two",testCache.get("2"));
		assertEquals("three should be in cache","this is three",testCache.get("3"));
		assertEquals("four should be in cache","this is four",testCache.get("4"));
		assertEquals("five should be in cache","this is five",testCache.get("5"));
		
		testCache.put("6", "this is six");
		testCache.put("7", "this is seven");
		testCache.put("8", "this is eight");
		testCache.put("9", "this is nine");
		//cache should now have [4 8] [5 9]

		assertEquals("two should NOT be in cache",null,testCache.get("2"));
		assertEquals("three should NOT be in cache",null,testCache.get("3"));
		assertEquals("four should be in cache","this is four",testCache.get("4"));
		assertEquals("five should be in cache","this is five",testCache.get("5"));
		assertEquals("six should NOT be in cache",null,testCache.get("6"));
		assertEquals("seven should NOT be in cache",null,testCache.get("7"));
		assertEquals("eight should be in cache","this is eight",testCache.get("8"));
		assertEquals("nine should be in cache","this is nine",testCache.get("9"));
		
		System.out.println("U DA BES");
	}

}
