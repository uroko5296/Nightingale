package fts.searcher;

import java.util.List;

public interface RecordAcquirer {

	/*
	 * indexerでいうIndexTableのような存在。
	 * クエリに基づき候補となるレコード(Record)の取得およびプールをするためのクラス。
	 *
	 * Searcherがサーチをするためのデータ構造を構築するFactory的なクラスでもある。
	 */

	List<Record> getSortedRecords();

}
