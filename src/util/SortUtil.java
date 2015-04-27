package util;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map.Entry;

import structure.ComparableEntry;

public class SortUtil<E extends Comparable<E>, T extends List<E>> {

	public SortUtil() {

		// must specify a input type and return type and data type

	}

	public void heapSort(T sort) {

		buildMaxHeapify(sort);

		recurHeapSort(sort);

	}

	private void buildMaxHeapify(T sort) {

		int startIndex = getParentIndex(sort.size() - 1);

		for (int i = startIndex; i >= 0; i--) {

			maxHeapify(sort, sort.size(), i);

		}

	}

	private void maxHeapify(T data, int heapSize, int index) {

		int left = getChildLeftIndex(index);

		int right = getChildRightIndex(index);

		int largest = index;

		if (left < heapSize && data.get(index).compareTo(data.get(left)) > 0) {

			largest = left;

		}

		if (right < heapSize

		&& data.get(largest).compareTo(data.get(right)) > 0) {

			largest = right;

		}

		if (largest != index) {

			swap(data, index, largest);

			maxHeapify(data, heapSize, largest);

		}

	}

	public void add2Heap(T heap, E e) {

		int pos = 0;

		for (pos = 0; pos < heap.size(); pos++) {

			if (e.compareTo(heap.get(pos)) >= 0) {

				break;

			}

		}

		heap.add(pos, e);

	}

	private void recurHeapSort(T sort) {

		for (int i = sort.size() - 1; i > 0; i--) {

			swap(sort, 0, i);

			maxHeapify(sort, i, 0);

		}

	}

	private void swap(T data, int index1, int index2) {

		E temp = data.get(index1);

		data.set(index1, data.get(index2));

		data.set(index2, temp);

	}

	private static int getParentIndex(int current) {

		return (current - 1) >> 1;

	}

	private static int getChildLeftIndex(int current) {

		return (current << 1) + 1;

	}

	private static int getChildRightIndex(int current) {

		return (current << 1) + 2;

	}

	public void print(T data) {

		int pre = -2;

		for (int i = 0; i < data.size(); i++) {

			if (pre < (int) (Math.log(i + 1) / Math.log(2))) {

				pre = (int) (Math.log(i + 1) / Math.log(2));

				System.out.println();

			}

			System.out.print(data.get(i) + " |");

		}

		System.out.println();

	}

	public static void main(String[] args) {

		SortUtil<ComparableEntry<String, Double>, List<ComparableEntry<String, Double>>> su = new SortUtil<ComparableEntry<String, Double>, List<ComparableEntry<String, Double>>>();

		List<ComparableEntry<String, Double>> list1 = new ArrayList<ComparableEntry<String, Double>>();

		List<ComparableEntry<String, Double>> list2 = new ArrayList<ComparableEntry<String, Double>>();

		HashMap<String, Double> map = new HashMap<String, Double>();

		map.put("a", (double) 1);

		map.put("b", (double) 0);

		map.put("c", (double) 10);

		map.put("d", (double) 20);

		map.put("e", (double) 3);

		map.put("f", (double) 5);

		map.put("g", (double) 6);

		for (Entry<String, Double> en : map.entrySet()) {

			list1.add(new ComparableEntry<String, Double>(en.getKey(), en

			.getValue()));

		}

		map.clear();

		map.put("h", (double) 4);

		map.put("i", (double) 9);

		map.put("j", (double) 8);

		map.put("k", (double) 12);

		map.put("l", (double) 17);

		map.put("m", (double) 34);

		map.put("n", (double) 11);

		for (Entry<String, Double> en : map.entrySet()) {

			list2.add(new ComparableEntry<String, Double>(en.getKey(), en

			.getValue()));

		}

		su.heapSort(list1);

		su.heapSort(list2);

		List<ComparableEntry<String, Double>> list3 = su

		.mergeAndSort(list1, list2);

		System.out.println(list1);

		System.out.println(list2);

		System.out.println(list3);

	}

	public List<ComparableEntry<String, Double>> mergeAndSort(

	List<ComparableEntry<String, Double>> l1,

	List<ComparableEntry<String, Double>> l2) {

		List<ComparableEntry<String, Double>> merged = new ArrayList<ComparableEntry<String, Double>>();

		ComparableEntry<String, Double> f, t;
		
		int i, j;

		for (i = 0, j = 0; i < l1.size() && j < l2.size();) {

			f = l1.get(i);

			t = l2.get(j);

			if (f.compareTo(t) > 0) {

				merged.add(f);

				i++;

			} else if (f.compareTo(t) < 0) {

				merged.add(t);

				j++;

			} else {

				merged.add(f);

				merged.add(t);

				i++;

				j++;

			}

		}

		// add rest

		while (i < l1.size()) {

			f = l1.get(i);

			i++;

			merged.add(f);

		}

		while (j < l2.size()) {

			t = l2.get(j);

			j++;

			merged.add(t);

		}

		return merged;

	}

}
