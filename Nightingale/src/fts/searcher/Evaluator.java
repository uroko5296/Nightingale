package fts.searcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface Evaluator {

	EvalResult evaluate();

	/*
	 * EvalResultはMap<Integer, Double>の委譲クラス。
	 * 文書IDごとの評価値を表す。
	 */
	class EvalResult implements Map<Integer, Double> {

		Map<Integer, Double> r_;

		EvalResult() {
			r_ = new HashMap<Integer, Double>();
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
		public Double get(Object key) {
			return r_.get(key);
		}

		@Override
		public Double put(Integer key, Double value) {
			return r_.put(key, value);
		}

		@Override
		public Double remove(Object key) {
			return r_.remove(key);
		}

		@Override
		public void putAll(Map<? extends Integer, ? extends Double> m) {
			r_.putAll(m);
		}

		@Override
		public void clear() {
			r_.clear();
		}

		@Override
		public Set<Integer> keySet() {
			return r_.keySet();
		}

		@Override
		public Collection<Double> values() {
			return r_.values();
		}

		@Override
		public Set<Entry<Integer, Double>> entrySet() {
			return r_.entrySet();
		}

	}

}
