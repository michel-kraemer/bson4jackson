package de.undercouch.bson4jackson;

public class Stack<T> {

  private static class Node<T> {

    public final T data;
    public Node<T> next;

    public Node( T data ) {
      this( data, null );
    }

    public Node( T data, Node<T> next ) {
      this.data = data;
      this.next = next;
    }

  }

  private Node<T> head;

  public T peek() {
    return head == null ? null : head.data;
  }

  public void push( T item ) {
    if( head != null ) {
      head = new Node<T>( item, head );
    } else {
      head = new Node<T>( item );
    }
  }

  public T pop() {
    if( head == null ) {
      return null;
    }
    T data = head.data;
    head = head.next;
    return data;
  }

  public boolean isEmpty() {
    return head == null;
  }

}
