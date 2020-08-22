package fts.searcher;

import java.util.List;

import fts.database.DBManager.TRecord;
import fts.utils.PostingList;

public interface Retriever {

	/*
	 * indexerでいうIndexTableのような存在。
	 * クエリに基づき候補となるレコード(Record)の取得およびプールをするためのクラス。
	 *
	 * Searcherがサーチをするためのデータ構造を構築するFactory的なクラスでもある。
	 */

	List<TRecord> getRecordList();

	List<PostingList> getPostingListList();

}
