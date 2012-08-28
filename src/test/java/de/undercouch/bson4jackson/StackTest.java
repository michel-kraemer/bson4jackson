package de.undercouch.bson4jackson;

import org.junit.Assert;
import org.junit.Test;

public class StackTest {

  @Test
  public void testBasicStackOperations() {
    Stack<String> stack = new Stack<String>();
    Assert.assertTrue( stack.isEmpty() );
    stack.push( "Hello" );
    Assert.assertFalse( stack.isEmpty() );
    Assert.assertEquals( "Hello", stack.peek() );
    stack.push( "World" );
    Assert.assertEquals( "World", stack.peek() );
    String world = stack.pop();
    Assert.assertEquals( "World", world );
    Assert.assertEquals( "Hello", stack.peek() );
    stack.pop();
    Assert.assertTrue( stack.isEmpty() );
  }

}
