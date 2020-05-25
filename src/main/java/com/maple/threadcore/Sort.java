package com.maple.threadcore;


public class Sort<E extends Comparable<E>> {
    public void quickSort(E[] arrays){
        if (arrays.length <= 1)
            return;
        quickSort(arrays,0, arrays.length-1);

    }
    private void quickSort(E[] arrays, int start, int end){
        if (start>=end)
            return;
        int partition = partition(arrays,start, end);
        quickSort(arrays, start, partition);
        quickSort(arrays, partition+1, end);
    }
    private int partition(E[] arrays, int start, int end){
        E base = arrays[start];
        while(start <= end){
            while(start <= end && base.compareTo(arrays[end]) <= 0)
                end--;
            if (start <= end){
                arrays[start] = arrays[end];
                end--;
            }
            while(start <= end && base.compareTo(arrays[start]) >= 0)
                start++;
            if(start <= end){
                arrays[end] = arrays[start];
                start++;
            }
        }
        arrays[start] = base;
        return start;
    }

    public void mergeSort(E[] arrays){
        if (arrays.length <= 1)
            return;
        mergeSort(arrays,0, arrays.length-1);
    }
    private void mergeSort(E[] arrays, int start, int end){
        if (start >= end){
            return;
        }
        int middle = start + (end-start) / 2;
        mergeSort(arrays, start, middle);
        mergeSort(arrays, middle+1, end);
        if (arrays[middle].compareTo(arrays[middle+1]) > 0){
            merge(arrays, start, middle, end);
        }
    }
    private void merge(E[] arrays, int start, int middle, int end){
        int i = start, j = middle+1, k = 0;
        Object[] numbers = new Object[end-start+1];
        while(i <= middle && j <= end){
            if (arrays[i].compareTo(arrays[j]) < 0)
                numbers[k++] = arrays[i++];
            else
                numbers[k++] = arrays[j++];
        }
        if (i <= middle) {
            System.arraycopy(arrays, i, numbers, k, middle-i+1 );
            k += middle-i+1;
        }
        if ( j <= end){
            System.arraycopy(arrays, i, numbers, k, end-j+1);
            k += end-j+1;
        }
        System.arraycopy(numbers,0, arrays, start, end-start+1);
    }
}
