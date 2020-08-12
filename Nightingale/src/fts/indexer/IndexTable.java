package fts.indexer;

import java.util.Map;

import fts.utils.PostingList;
import fts.utils.Token;

/*
 * 外部からテーブル（実体はDB）への許された操作を提供する。
 * DBとの接続を担うクラスとして設計する
 */
interface IndexTable {

	/*
	 * 転置インデックスの構築時
	 */
	//titleが任意の検索対象（文書）について定義できるかは不明。
	void addPosting(Token token, int documentId, int position);

	int getDocumentCountAdded();

	void incrementDocumentCountAdded();

	boolean isOverCapacity();

	Map<Token, PostingList> getMap();

	void flush();
}
