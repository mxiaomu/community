package com.maple.service;

import java.util.Stack;

public class MaxStack<E extends Comparable<E>> {
    private Stack<E> stack;
    private Stack<E> maxStack;

    public MaxStack(){
        this.stack = new Stack<>();
        this.maxStack = new Stack<>();
    }

    public void push(E value){
        stack.push(value);
        if (this.maxStack.isEmpty() ||
                this.maxStack.peek().compareTo(value) < 0){
            this.maxStack.push(value);
        }
    }

    public E pop(){
        E value = stack.pop();
        if (!maxStack.isEmpty() && maxStack.peek().compareTo(value) == 0){
            maxStack.pop();
        }
        return value;
    }

    public E max(){
        return maxStack.isEmpty() ? null : maxStack.peek();
    }

    public static void main(String[] args) {
        MaxStack<Integer> maxStack = new MaxStack<>();
        maxStack.push(10);
        maxStack.push(3);
        maxStack.push(15);
        maxStack.push(11);
        maxStack.pop();
        maxStack.pop();

        System.out.println(maxStack.max());
    }
}
