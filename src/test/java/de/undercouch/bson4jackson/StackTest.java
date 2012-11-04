package de.undercouch.bson4jackson;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StackTest {

  private static final String A = "A";
  private static final String B = "B";
  private static final String C = "C";

  Stack<String> stack;

  @Before
  public void createStack() {
    stack = new Stack<String>();
  }

  @Test
  public void isEmptyShouldReturnTrueForNewStack() {
    Assert.assertTrue( stack.isEmpty() );
  }

  @Test
  public void isEmptyShouldReturnFalseAfterPush() {
    stack.push( A );
    Assert.assertFalse( stack.isEmpty() );
  }

  @Test
  public void isEmptyShouldReturnTrueAfterLastItemIsPopped() {
    stack.push( A );
    stack.pop();
    Assert.assertTrue( stack.isEmpty() );
  }

  @Test
  public void popShouldReturnNullForEmptyStack() {
    Assert.assertNull( stack.pop() );
  }

  @Test
  public void peekShouldReturnNullForEmptyStack() {
    Assert.assertNull( stack.peek() );
  }

  @Test
  public void popShouldReturnTheOnlyItem() {
    stack.push( A );
    Assert.assertEquals( A, stack.pop() );
  }

  @Test
  public void peekShouldReturnTheOnlyItem() {
    stack.push( A );
    Assert.assertEquals( A, stack.peek() );
  }

  @Test
  public void popShouldReturnLastItemFirst() {

    stack.push( A );
    stack.push( B );
    stack.push( C );

    Assert.assertEquals( C, stack.pop() );
    Assert.assertEquals( B, stack.pop() );
    Assert.assertEquals( A, stack.pop() );

  }

}
