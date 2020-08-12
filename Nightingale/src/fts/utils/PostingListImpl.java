package fts.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.base.Splitter;

public class PostingListImpl implements PostingList {

	protected SortedMap<Integer, SortedSet<Integer>> p_;

	public PostingListImpl(SortedMap<Integer, SortedSet<Integer>> postingList) {
		/*
		 * 構築時はTreeSet,検索時はHashSetでいいかもしれない。
		 * DBの更新の際にpostingsがソートされている必要があるのは構築時のみであるため。
		 * そうする場合は、抽象クラスで構築時と検索時の共通部分を実装するか。
		 */
		p_ = postingList;
	}

	/*
	 * encode関数に対応したコンストラクタ。
	 * 	 * 文字列としてのポスティングリストをデコード（パース）する。
	 * CSV形式であって、末尾にも「，」がある。
	 */
	public PostingListImpl(String s, SortedMap<Integer, SortedSet<Integer>> postingList) {
		this(postingList);
		addAllByDecoding(s);
		return;
	}

	public PostingListImpl(String s) {
		this(s, new TreeMap<Integer, SortedSet<Integer>>());
		return;
	}

	public PostingListImpl() {
		this("", new TreeMap<Integer, SortedSet<Integer>>());
		return;
	}

	@Override
	public boolean add(Posting p) {
		if (this.p_ == null) {
			throw new IllegalArgumentException("postingList is null !");
		}

		if (!this.p_.containsKey(p.documentId_)) {
			SortedSet<Integer> pList = new TreeSet<Integer>();
			pList.add(p.position_);
			this.p_.put(p.documentId_, pList);
			return true;
		} else {
			return this.p_.get(p.documentId_).add(p.position_);
		}
	}

	@Override
	public final void addAllByDecoding(String encodings) {
		if (encodings.length() < 2) {//','のみのとき
			return;
		}

		List<String> ss = Splitter.on(',').splitToList(encodings);
		assert (ss.size() % 2 == 0);
		for (int i = 0; i < ss.size() - 1; i += 2) {//末尾のカンマによる要素のため-1が要る
			int documentId = Integer.parseInt(ss.get(i));
			int position = Integer.parseInt(ss.get(i + 1));
			//System.out.println("PostingListAbst#addAllByDecoding	postingList:" + postingList_);
			this.add(new Posting(documentId, position));//ここでは定義していないため子クラスのaddが呼び出される。
		}
		return;
	}

	@Override
	public final String encode() {
		String postings = "";
		List<Posting> sortedList = new ArrayList<Posting>();
		p_.keySet().forEach(docId -> {
			SortedSet<Integer> positions = p_.get(docId);
			positions.forEach(p -> sortedList.add(new Posting(docId, p)));
		});
		for (Posting p : sortedList) {
			postings = postings + p.getDocumentId() + "," + p.getPosition() + ",";
		}
		return postings;
	}

	/*
	 * この関数の結果がDB（tokens.postings）に書き込まれることになる。
	 * encode関数でそのまま呼び出される。
	 * ソートするのにn log(n)時間かかっているためTreeSetの方がいいかも？
	 */
	@Override
	public String toString() {

		String postings = "";
		List<Posting> sortedList = new ArrayList<Posting>();
		p_.keySet().forEach(docId -> {
			SortedSet<Integer> positions = p_.get(docId);
			positions.forEach(p -> sortedList.add(new Posting(docId, p)));
		});
		for (Posting p : sortedList) {
			postings = postings + p.toString() + ",";
		}
		return postings;//末尾にカンマ(,)が残っているが面倒なのでそのまま。
	}

	@Override
	public final int hashCode() {
		return p_.hashCode();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass())
			return false;

		PostingListImpl p = (PostingListImpl) obj;
		return this.p_.equals(p.p_);
	}

	@Override
	public int getDocsCount() {
		return p_.keySet().size();
	}

	@Override
	public int size() {
		return p_.size();
	}

	@Override
	public boolean isEmpty() {
		return p_.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return p_.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return p_.containsValue(value);
	}

	@Override
	public SortedSet<Integer> get(Object key) {
		return p_.get(key);
	}

	@Override
	public SortedSet<Integer> put(Integer key, SortedSet<Integer> value) {
		return p_.put(key, value);
	}

	@Override
	public SortedSet<Integer> remove(Object key) {
		return p_.remove(key);
	}

	@Override
	public void clear() {
		p_.clear();
	}

	@Override
	public Comparator<? super Integer> comparator() {
		return p_.comparator();
	}

	@Override
	public SortedMap<Integer, SortedSet<Integer>> subMap(Integer fromKey, Integer toKey) {
		return p_.subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<Integer, SortedSet<Integer>> headMap(Integer toKey) {
		return p_.headMap(toKey);
	}

	@Override
	public SortedMap<Integer, SortedSet<Integer>> tailMap(Integer fromKey) {
		return p_.tailMap(fromKey);
	}

	@Override
	public Integer firstKey() {
		return p_.firstKey();
	}

	@Override
	public Integer lastKey() {
		return p_.lastKey();
	}

	@Override
	public Set<Integer> keySet() {
		return p_.keySet();
	}

	@Override
	public Collection<SortedSet<Integer>> values() {
		return p_.values();
	}

	@Override
	public Set<Entry<Integer, SortedSet<Integer>>> entrySet() {
		return p_.entrySet();
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends SortedSet<Integer>> m) {
		p_.putAll(m);
	}

}
