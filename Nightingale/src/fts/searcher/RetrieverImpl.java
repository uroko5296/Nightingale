package fts.searcher;

import java.util.ArrayList;
import java.util.List;

import fts.database.DBManager;
import fts.database.DBManager.TRecord;
import fts.database.DBManagerForMySQL;
import fts.utils.PostingList;
import fts.utils.PostingListImpl;
import fts.utils.Token;

public class RetrieverImpl implements Retriever {

	List<Token> tokenList_;

	DBManager dbManager_;

	List<TRecord> recordList_;

	List<PostingList> postingListList_;

	public RetrieverImpl(List<Token> tokenList) {

		tokenList_ = tokenList;
	}

	@Override
	public List<PostingList> getPostingListList() {
		if (dbManager_ == null) {
			dbManager_ = new DBManagerForMySQL();
		}

		if (postingListList_ == null) {
			long s = System.currentTimeMillis();
			long s1 = System.currentTimeMillis();
			List<String> postingLists = dbManager_
					.dbGetPostingLists(tokenList_);
			long s2 = System.currentTimeMillis();

			postingListList_ = new ArrayList<PostingList>();
			for (int i = 0; i < tokenList_.size(); i++) {
				PostingList p = new PostingListImpl(postingLists.get(i));
				postingListList_.add(p);
			}
			long s3 = System.currentTimeMillis();

			System.out.println("getSortedRecords etime1:" + (s1 - s) + "[ms]");
			System.out.println("getSortedRecords etime2:" + (s2 - s1) + "[ms]");
			System.out.println("getSortedRecords etime3:" + (s3 - s2) + "[ms]");

		}

		return postingListList_;
	}

}
