package com.imdp.instaclimb;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HelpersTest extends TestCase {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testToCamelCase() {
    assertTrue(Helpers.Do.toCamelCase("aaa bbb ccc", " ", null).equals("Aaa Bbb Ccc"));
    assertTrue(Helpers.Do.toCamelCase("aaa+bbb+ccc", "+", null).equals("Aaa+Bbb+Ccc"));
    assertTrue(Helpers.Do.toCamelCase("aaazbbbzccc", "z", null).equals("AaazBbbzCcc"));
    assertTrue(Helpers.Do.toCamelCase("aaayzbbbyzccc", "yz", "-").equals("Aaa-Bbb-Ccc"));
    assertTrue(Helpers.Do.toCamelCase("aaaxyzbbbxyzccc", "xyz", " - ").equals("Aaa - Bbb - Ccc"));
    assertTrue(Helpers.Do.toCamelCase("aaa1bbb1ccc", "1", ".").equals("Aaa.Bbb.Ccc"));
    assertTrue(Helpers.Do.toCamelCase("aaa_bbb_ccc", "_", " * ").equals("Aaa * Bbb * Ccc"));
  }
}