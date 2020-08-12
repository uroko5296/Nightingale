package fts.searcher;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public interface Searcher {

	public SearchResult search(String query, int n);

	/*
	 * SearchResultは単なるSortedMap<Double, Integer>の委譲クラス。
	 * 文書IDごとの評価値から、文書IDへのマップ。
	 */
	class SearchResult implements SortedMap<Double, Integer> {

		SortedMap<Double, Integer> r_;

		SearchResult() {
			r_ = new TreeMap<Double, Integer>(new Comparator<Double>() {
				@Override
				public int compare(Double d1, Double d2) {
					return d2.compareTo(d1);//スコアの降順にする。
				}
			});
		}

		@Override
		public int size() {
			return r_.size();
		}

		@Override
		public boolean isEmpty() {
			return r_.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return r_.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return r_.containsValue(value);
		}

		@Override
		public Integer get(Object key) {
			return r_.get(key);
		}

		@Override
		public Integer put(Double key, Integer value) {
			return r_.put(key, value);
		}

		@Override
		public Integer remove(Object key) {
			return r_.remove(key);
		}

		@Override
		public void putAll(Map<? extends Double, ? extends Integer> m) {
			r_.putAll(m);
		}

		@Override
		public void clear() {
			r_.clear();
		}

		@Override
		public Comparator<? super Double> comparator() {
			return r_.comparator();
		}

		@Override
		public SortedMap<Double, Integer> subMap(Double fromKey, Double toKey) {
			return r_.subMap(fromKey, toKey);
		}

		@Override
		public SortedMap<Double, Integer> headMap(Double toKey) {
			return r_.headMap(toKey);
		}

		@Override
		public SortedMap<Double, Integer> tailMap(Double fromKey) {
			return r_.tailMap(fromKey);
		}

		@Override
		public Double firstKey() {
			return r_.firstKey();
		}

		@Override
		public Double lastKey() {
			return r_.lastKey();
		}

		@Override
		public Set<Double> keySet() {
			return r_.keySet();
		}

		@Override
		public Collection<Integer> values() {
			return r_.values();
		}

		@Override
		public Set<Entry<Double, Integer>> entrySet() {
			return r_.entrySet();
		}

		@Override
		public int hashCode() {
			return r_.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return r_.equals(obj);
		}

		@Override
		public String toString() {
			return r_.toString();
		}

	}
}
