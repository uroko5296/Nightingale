package fts.searcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fts.database.DBManager;
import fts.database.DBManagerForMySQL;
import fts.utils.PostingList;
import fts.utils.PostingListImpl;
import fts.utils.Record;
import fts.utils.RecordImpl;
import fts.utils.Token;

public class RetrieverImpl implements Retriever {

	List<Token> tokenList_;

	DBManager dbManager_;

	List<Record> sortedRecords_;

	List<PostingList> postingListList_;

	public RetrieverImpl(List<Token> tokenList) {

		tokenList_ = tokenList;
	}

	@Override
	public List<Record> getSortedRecords() {
		if (dbManager_ == null) {
			dbManager_ = new DBManagerForMySQL();
		}

		if (sortedRecords_ == null) {
			long s = System.currentTimeMillis();
			List<Integer> tokenIds = dbManager_.dbGetTokenIds(tokenList_).stream().sorted()
					.collect(Collectors.toList());
			long s1 = System.currentTimeMillis();
			List<String> postingLists = dbManager_
					.dbGetPostingLists(tokenList_);
			long s2 = System.currentTimeMillis();

			sortedRecords_ = new ArrayList<Record>();
			postingListList_ = new ArrayList<PostingList>();
			for (int i = 0; i < tokenList_.size(); i++) {
				PostingList p = new PostingListImpl(postingLists.get(i));
				Record record = new RecordImpl(tokenIds.get(i), p, i);
				sortedRecords_.add(record);
				postingListList_.add(p);
			}
			long s3 = System.currentTimeMillis();

			System.out.println("getSortedRecords etime:" + (s1 - s) + "[ms]");
			System.out.println("getSortedRecords etime:" + (s2 - s1) + "[ms]");
			System.out.println("getSortedRecords etime:" + (s3 - s2) + "[ms]");

		}

		return sortedRecords_;
	}

	@Override
	public List<PostingList> getPostingListList() {
		if (dbManager_ == null) {
			dbManager_ = new DBManagerForMySQL();
		}

		if (postingListList_ == null) {
			long s = System.currentTimeMillis();
			List<Integer> tokenIds = dbManager_.dbGetTokenIds(tokenList_).stream().sorted()
					.collect(Collectors.toList());
			long s1 = System.currentTimeMillis();
			List<String> postingLists = dbManager_
					.dbGetPostingLists(tokenList_);
			long s2 = System.currentTimeMillis();

			sortedRecords_ = new ArrayList<Record>();
			postingListList_ = new ArrayList<PostingList>();
			for (int i = 0; i < tokenList_.size(); i++) {
				PostingList p = new PostingListImpl(postingLists.get(i));
				Record record = new RecordImpl(tokenIds.get(i), p, i);
				sortedRecords_.add(record);
				postingListList_.add(p);
			}
			long s3 = System.currentTimeMillis();

			System.out.println("getSortedRecords etime:" + (s1 - s) + "[ms]");
			System.out.println("getSortedRecords etime:" + (s2 - s1) + "[ms]");
			System.out.println("getSortedRecords etime:" + (s3 - s2) + "[ms]");

		}

		return postingListList_;
	}

}
