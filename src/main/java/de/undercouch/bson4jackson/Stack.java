// Copyright 2012 Leo Accend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.bson4jackson;

class Stack<T> {

  class Node {

    T data;
    Node next;

    Node( T data, Node next ) {
      this.data = data;
      this.next = next;
    }

  }

  private Node head;

  public boolean isEmpty() {
    return head == null;
  }

  public void push( T data ) {
    head = new Node( data, head );
  }

  public T peek() {
    return head == null ? null : head.data;
  }

  public T pop() {
    T data = peek();
    if( head != null ) {
      head = head.next;
    }
    return data;
  }

}
