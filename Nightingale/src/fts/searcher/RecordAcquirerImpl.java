package fts.searcher;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import fts.database.DBManager;
import fts.database.MySQLManager;
import fts.utils.PostingListImpl;
import fts.utils.Token;

public class RecordAcquirerImpl implements RecordAcquirer {

	List<Token> rawTokens_;

	DBManager dbManager_;

	List<Record> sortedRecords_;

	public RecordAcquirerImpl(List<Token> rawTokens) {

		rawTokens_ = rawTokens;
	}

	@Override
	public List<Record> getSortedRecords() {
		if (dbManager_ == null) {
			dbManager_ = new MySQLManager();
		}

		if (sortedRecords_ == null) {
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
		}
		return sortedRecords_;
	}

}
