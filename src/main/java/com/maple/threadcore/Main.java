package com.maple.threadcore;

public class Main {
    public static void main(String[] args) {
        Sort<Integer> sort = new Sort<>();
        Integer[] arrays = new Integer[]{2,3,1,5,4};
        sort.mergeSort(arrays);

        for (int value : arrays){
            System.out.print(value + " ");
        }
    }
}
