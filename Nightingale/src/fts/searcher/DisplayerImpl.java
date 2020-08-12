package fts.searcher;

import java.util.SortedMap;

import fts.database.DBManager;
import fts.database.DBManagerForMySQL;
import fts.searcher.Searcher.SearchResult;

public class DisplayerImpl implements Displayer {

	SortedMap<Double, Integer> searchResult_;
	DBManager dbManager_;

	public DisplayerImpl(SearchResult s) {
		searchResult_ = s;
	}

	@Override
	public void displayResult() {
		System.out.println("DisplayerImpl#displayResult");
		if (dbManager_ == null) {
			dbManager_ = new DBManagerForMySQL();
		}
		searchResult_.keySet().forEach(e -> {
			int d = searchResult_.get(e);
			String title = dbManager_.dbGetTitleOfDocument(d);
			System.out.println("DisplayerImpl#displayResult 文書ID=[" + d + "], title=" + title + "\t評価値=" + e);
		});
	}

}
