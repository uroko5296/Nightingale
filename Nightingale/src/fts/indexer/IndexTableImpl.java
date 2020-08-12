package fts.indexer;

import java.util.HashMap;
import java.util.Map;

import fts.utils.PostingList;
import fts.utils.PostingList.Posting;
import fts.utils.PostingListImpl;
import fts.utils.Token;

class IndexTableImpl implements IndexTable {
	/*
	 * ミニ転置インデックス（トークンIDをキーとし、ポスティングリストを値とする連想配列）
	 * すなわち、ストレージではなくメモリに読みだされている転置インデックス。
	 * 実態は、
	 * Map<String, PostingList>
	 */
	/*
	 * 転置インデックス作成時：
	 * ミニ転置インデックスとしてしかインスタンス化しない。
	 * すなわち、実質的なインスタンスは一つのみである。（現時点の実装はリンク構造のため厳密には複数インスタンス化しているが。）
	 */

	private Map<Token, PostingListImpl> table_;

	int documentCountAdded_;//追加されてきた文書（Documentクラスに対応。）の数。

	final int capacity_;

	IndexTableImpl(int capacity) {
		table_ = new HashMap<Token, PostingListImpl>();
		documentCountAdded_ = 0;
		capacity_ = capacity;
	};

	@Override
	public void addPosting(Token token, int documentId, int position) {
		/*
		 * keyとvalからなるpostingを追加
		 * リストが存在しなければ新規追加
		 * MiniTableをnullにするだけでGCに確実に拾ってもらえるようにtokenはコピーして保持しておく。
		 */
		//
		Token newToken = new Token(new String(token.getToken()));
		assert (table_ != null);
		if (!table_.containsKey(newToken)) {//初めて見るトークンかどうか
			//PostingListOnIndexing val = new PostingListOnIndexing(documentId, position);//新しいPostingList
			PostingListImpl val = new PostingListImpl();//新しいPostingList
			val.add(new Posting(documentId, position));
			table_.put(newToken, val);
		} else {
			table_.get(newToken).add(new Posting(documentId, position));//リストに出現位置を追加
		}
	}

	@Override
	public int getDocumentCountAdded() {
		return documentCountAdded_;
	}

	@Override
	public void incrementDocumentCountAdded() {
		documentCountAdded_++;
	}

	@Override
	public boolean isOverCapacity() {
		return documentCountAdded_ > capacity_;
	}

	@Override
	public Map<Token, PostingList> getMap() {
		return new HashMap<Token, PostingList>(table_);
	}

	@Override
	public void flush() {
		table_ = null;
		table_ = new HashMap<Token, PostingListImpl>();
		documentCountAdded_ = 0;
	}
}
