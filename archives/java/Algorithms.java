import static java.lang.System.out;

//Sorting Algorithms.

public class Algorithms {

	public static < Q > void printArray(Q[] input){		
		for (Q element : input) {
			out.print(" " + element + " ");
		}	
		System.out.print("\n");
	}

	public static void swap(Integer[] input,int i,int j ){
		//out.println("swapping " + input[i] + " and " + input[j]);
		Integer temp = input[j];
		input[j] = input[i];
		input[i] = temp;
		//out.println("swapped " + input[i] + " and " + input[j]);
	}

	public static void main(String[] args) {
		Integer [] input = new Integer[]{6,3,6,1,2,8,1,6};
		input = new Integer[]{13,19,9,5,12,8,7,4,11,2,6,21};
		input = new Integer[]{9,8,4,5,6,7,2,1};
		Sorter sorter = new BubbleSort();
		sorter = new InsertionSort();
		sorter = new MergeSort();
		sorter = new QuickSortNormal();
		sorter = new HeapSort();
		printArray(input);
		Integer[] output = sorter.sort(input);
		printArray(output);
	}
}

class HeapSort extends Sorter {

	@Override
	Integer[] sort(Integer[] A) {
		heapSort(A);
		return A;
	}

	int getParent(int index) {
		return (int) Math.floor(index/2);
	}

	int getLeftChild(int index) {
		return 2*index;
	}

	int getRightChild(int index) {
		return 2*index + 1 ;
	}

	void maxHeapify(Integer[] A, int index, int heapSize) {
		int leftIndex = getLeftChild(index);
		int rightIndex  = getRightChild(index);		
		int largestIndex = index;

		if(leftIndex < heapSize && A[leftIndex] > A[index]) {
			largestIndex = leftIndex;
		}
		if(rightIndex < heapSize && A[rightIndex] > A[largestIndex]) {
			largestIndex = rightIndex;
		}

		if(largestIndex != index) {
			Algorithms.swap(A,largestIndex,index);
			maxHeapify(A,largestIndex, heapSize);
		}
	}

	void buildMaxHeap(Integer[] A){
		for (int i = (A.length-1)/2; i >= 0; i--) {
			maxHeapify(A,i, A.length);
		}
	}

	void heapSort(Integer[] A) {
		out.println("Before heap sort : ");
		Algorithms.printArray(A);
		buildMaxHeap(A);
		out.println("after build max heap : ");
		Algorithms.printArray(A);
		for (int i = A.length -1 ; i > 0 ; i--) {
			Algorithms.swap(A,i,0);
			maxHeapify(A,0,i);
		}
		out.println("After heap sort : ");
		Algorithms.printArray(A);		
	}

}

class QuickSortNormal extends Sorter {

	@Override
	Integer[] sort(Integer[] A) {
		Integer[] hoare = A;		
		HoarePart(hoare, 0, hoare.length-1);		
		out.println("Hoare>>>> ");
		Algorithms.printArray(hoare);
		out.println("\nSorting using QuickSortNormal Sort!!\n");
		QuickSort(A, 0 , A.length-1);
		return A;
	}

	void HoarePart(Integer[] hoare, int left, int right) {
		if(left < right) {			
			int pivot = hoarePartition(hoare,left,right);
			HoarePart(hoare, left, pivot);
			HoarePart(hoare, pivot + 1, right );
		}
	}


	void QuickSort(Integer[] A, int left, int right) {
		if(left < right) {
			int pivot = Partition(A,left,right);			
			QuickSort(A, left, pivot -1 );
			QuickSort(A, pivot+1, right );
		}
	}

	int hoarePartition(Integer[] A, int left, int right) {
		
		Algorithms.printArray(A);
		int pivot = A[left];
		int i = left - 1 ;
		int j = right + 1;
		while(true){
			do {
				j--;
			}while(A[j] > pivot);

			do{
				i++;
			}while(A[i] < pivot);

			if(i < j) {
				System.out.println("\nSwapping : " + A[i] + " (at index "+ i +") and " + A[j] + " (at index " + j+")");
				System.out.println("Pivot : " + pivot + " left : " + A[left] + " right : " + A[right]);
				Algorithms.swap(A,i,j);
				Algorithms.printArray(A);
			}else {
				System.out.println(">Pivot : " + pivot + " left : " + A[left] + " right : " + A[right]);
				return j ;
			}
		}
	}

	int Partition(Integer[] A, int left, int right) {
		int pivot = A[right];

		int i = left - 1;
		for (int j = i+1 ; j < right ; j++) {
			if(A[j] < pivot) {				
				i++;
				Algorithms.swap(A,i,j);
			}
		}
		Algorithms.swap(A, i+1, right);
		return i+1;
	}

}


class MergeSort extends Sorter {
		
	int totalInversions = 0;

	@Override
	Integer[] sort(Integer[] A) {	
		out.println("\nSorting using Merge Sort!!\n");						
		Integer[] tmp = new Integer[A.length];
		mergeSort(A,tmp,0,A.length-1);
		System.out.println("totalInversions : " + totalInversions);
		return A;
	}

	void mergeSort(Integer[] a, Integer[] tmp, int left, int right) {
		if(left < right) {
			int mid = ( left + right ) / 2 ; 
			mergeSort(a, tmp, left, mid);
			mergeSort(a, tmp, mid+1, right);
			totalInversions = totalInversions + merge(a,tmp, left, mid+1, right);
		}
	}

	int merge(Integer[] a, Integer[] tmp, int leftStart, int rightStart, int rightEnd)	{
		int leftEnd = rightStart - 1; 
		int inversionCount = 0;
		int k = leftStart ; 
		// Total count of numbers that will be merged. 
		int numbers = rightEnd - leftStart + 1;		

		boolean firstRightArrayElementFound = false;

		while ( leftStart <= leftEnd && rightStart <= rightEnd ) {			
			if (a[leftStart] <= a[rightStart]) {										
				tmp[k++] = a[leftStart++];
			} else {			
				inversionCount = inversionCount + (leftEnd - leftStart + 1);
				tmp[k++] = a[rightStart++];
			}
		}

		while( leftStart <= leftEnd ) {			
			tmp[k++] = a[leftStart++];			
		}
		while(rightStart <= rightEnd) {
			tmp[k++] = a[rightStart++];
		}

		// Copy from tmp to actual array.
		for (int i = 0; i < numbers ; i++,rightEnd--) {
			a[rightEnd] = tmp[rightEnd];
		}

		return inversionCount;
	} 

}

class InsertionSort extends Sorter{
	@Override
	Integer[] sort(Integer[] A) {
		out.println("\nSorting using InsertionSort!!\n");
		for(int i = 1; i < A.length ; i++) {
			Integer key = A[i];
			int j = i-1;
			// > means in place sort.
			while(j >= 0 && A[j] > key) {
				A[j+1] = A[j];
				j--;
			}
			// Add 1 because while loop fails only when j < 0
			A[j+1] = key;
		}
		return A;
	}
}

class BubbleSort extends Sorter{
	@Override
	Integer[] sort(Integer[] input) {
		out.println("\nSorting using BubbleSort!!\n");		
		for (int i = 0 ; i < input.length ; i++ ) {
			boolean swapped = false;
			for (int j = input.length-1; j > i ; j--) {
				if(input[j] < input[j-1]){
					
					Algorithms.swap(input,j,j-1);
					//input[j-1] = input[j-1]+input[j]-(input[j]=input[j-1]);
					swapped = true;
				}
			}
			// Not even a single swap happened, so break the loop.
			if(!swapped)break;
		}
		return input;
	}
}

abstract class Sorter{
	abstract Integer[] sort(Integer[] input);
}