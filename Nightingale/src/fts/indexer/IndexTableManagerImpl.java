package fts.indexer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fts.database.DBManager;
import fts.database.DBManagerForMySQL;
import fts.tokenizer.Tokenizer;
import fts.utils.Document;
import fts.utils.PostingList;
import fts.utils.Token;

public class IndexTableManagerImpl implements IndexTableManager {

	private static final int DEFAULT_CAPACITY = 1000;

	private final IndexTable table_;
	private final DBManager dbManager_;

	public IndexTableManagerImpl() {
		table_ = new IndexTableImpl(DEFAULT_CAPACITY);
		dbManager_ = new DBManagerForMySQL();
	}

	@Override
	public void addDocumentIntoIndexTable(Document document, Tokenizer tokenizer) {

		/*
		 * 文書IDとトークンリストからポスティングリストを作る。
		 */

		List<Token> tokenList = tokenizer.parseAll(document.getBody());

		/*
		 * ストレージ上の文書用のテーブル（転置インデックスではない。）に文書を追加し、文書IDを取得する。
		 */
		int documentId = dbManager_.dbGetDocumentIdAndAddDocumentIfNotExists(document, tokenList.size());

		//まずはトークンリストの位置からミニ転置インデックスを更新する。
		for (int position = 0; position < tokenList.size(); position++) {
			table_.addPosting(tokenList.get(position), documentId, position);
		}

		//重複するトークンを除いてから、トークンをDBに追加する。
		Set<Token> tokenSet = new HashSet<Token>(tokenList);
		dbManager_.dbAddTokenSet(tokenSet);
		table_.incrementDocumentCountAdded();

		/*
		 * ミニ転置インデックスに追加されている文書数が閾値を上回ったら、
		 * ミニ転置インデックスにあるポスティングリストをストレージ上の
		 * テーブルに追加して、ミニ転置インデックスをフラッシュする。
		 */
		if (table_.isOverCapacity()) {
			this.flushTable();
		}
	}

	@Override
	public void flushTable() {
		Map<Token, PostingList> map = table_.getMap();
		dbManager_.dbUpdatePostingListMap(map);
		/*
		for (Token token : map.keySet()) {
			System.out.print(".");
			dbManager_.dbUpdatePostingList(token, map.get(token));
		}
		*/
		table_.flush();
	}

	@Override
	public void addPartsOfDocumentIndexTable(int documentId, String title, String partsOfBodyToAdd, int bodySize) {
		dbManager_.dbUpdateBodyOfDocument(documentId, title, partsOfBodyToAdd, bodySize);
	}
}
