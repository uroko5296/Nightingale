package fts.searcher;

import java.util.ArrayList;
import java.util.List;

import fts.database.DBManager;
import fts.database.DBManagerForMySQL;
import fts.utils.PostingListImpl;
import fts.utils.Record;
import fts.utils.RecordImpl;
import fts.utils.Token;

public class RetrieverImpl implements Retriever {

	List<Token> rawTokens_;

	DBManager dbManager_;

	List<Record> sortedRecords_;

	public RetrieverImpl(List<Token> rawTokens) {

		rawTokens_ = rawTokens;
	}

	@Override
	public List<Record> getSortedRecords() {
		if (dbManager_ == null) {
			dbManager_ = new DBManagerForMySQL();
		}

		if (sortedRecords_ == null) {
			List<Integer> tokenIds1 = dbManager_.dbGetTokenIds(rawTokens_);

			long s = System.currentTimeMillis();
			List<Integer> tokenIds = dbManager_.dbGetTokenIds(rawTokens_);
			long s1 = System.currentTimeMillis();
			List<String> postingLists = dbManager_.dbGetPostingLists(rawTokens_);
			long s2 = System.currentTimeMillis();

			sortedRecords_ = new ArrayList<Record>();
			for (int i = 0; i < rawTokens_.size(); i++) {
				Record record = new RecordImpl(tokenIds.get(i), new PostingListImpl(postingLists.get(i)), i);
				sortedRecords_.add(record);
			}
			long s3 = System.currentTimeMillis();

			System.out.println("getSortedRecords etime:" + (s1 - s) + "[ms]");
			System.out.println("getSortedRecords etime:" + (s2 - s1) + "[ms]");
			System.out.println("getSortedRecords etime:" + (s3 - s2) + "[ms]");

			/*
			long s = System.currentTimeMillis();
			int[] i = { 0 };
			sortedRecords_ = rawTokens_.stream().map((Token token) -> {
				return new RecordImpl(dbManager_.dbGetTokenId(token),
						new PostingListImpl(dbManager_.dbGetPostingList(token)),
						i[0]++);
			}).sorted(new Comparator<Record>() {
				@Override
				public int compare(Record r1, Record r2) {
					return r1.getPostingList().getDocsCount() - r2.getPostingList().getDocsCount();
				}
			}).collect(Collectors.toList());
			long s1 = System.currentTimeMillis();
			System.out.println("getSortedRecords etime:" + (s1 - s) + "[ms]");
			*/
		}

		return sortedRecords_;
	}

}
