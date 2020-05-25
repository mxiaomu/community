package com.maple.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MaxHeap<E extends Comparable<E>> {
    private List<E> entities;
    public MaxHeap(int capacity){
        this.entities = new ArrayList<>(capacity);
    }
    public void insert(E value){
        this.entities.add(value);
        shiftUp(this.entities.size()-1);
    }
    private void shiftUp(int index){
        while(index >= 0){
            int parent = getParent(index);
            if (entities.get(parent).compareTo(entities.get(index)) < 0){
                swap(index, parent);
                index = parent;
            }else{
                break;
            }
        }
    }
    public E remove(){
        if (this.entities.size() == 0)
            throw new NullPointerException();
        swap(0, this.entities.size()-1);
        shiftDown(0);
        return this.entities.remove(this.entities.size()-1);
    }

    private void shiftDown(int index){
        int end = getParent(this.entities.size()-1);
        while(index <= end){
            int leftIndex = getLeftChild(index);
            int rightIndex = getRightChild(index);
            int maxIndex = leftIndex;
            if (entities.get(maxIndex).compareTo(entities.get(rightIndex)) < 0){
                maxIndex = rightIndex;
            }
            if (entities.get(maxIndex).compareTo(entities.get(index)) < 0){
                swap(maxIndex, index);
                index = maxIndex;
            }else{
                break;
            }
        }
    }

    private void swap(int index1, int index2){
        Collections.swap(this.entities, index1, index2);
    }

    private int getParent(int index){
        return index / 2;
    }
    private int getLeftChild(int index){
        return index * 2 + 1;
    }
    private int getRightChild(int index){
        return index * 2 + 2;
    }

    public List<E> getEntities() {
        return entities;
    }

    public static void main(String[] args) {
        MaxHeap<Integer> maxHeap = new MaxHeap<>(10);
        maxHeap.insert(10);
        maxHeap.insert(11);
        maxHeap.insert(12);
        maxHeap.insert(15);
        maxHeap.insert(9);


        for (Integer integer : maxHeap.getEntities()) {
            System.out.print(integer + " ");
        }

    }
}
