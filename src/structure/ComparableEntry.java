package structure;

import java.util.Map.Entry;

public final class ComparableEntry<K, V extends Comparable<V>> implements
		Entry<K, V>, Comparable<ComparableEntry<K, V>> {

	private K key;
	private V value;

	public ComparableEntry(K key, V value) {
		super();
		this.key = key;
		this.value = value;
	}

	@Override
	public int compareTo(ComparableEntry<K, V> o) {
		return value.compareTo(o.value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		this.value = value;
		return value;
	}

	@Override
	public String toString() {
		String str = String.format("%s=%s", key.toString(), value.toString());
		return str;
	}

}