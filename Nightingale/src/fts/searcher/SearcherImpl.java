package fts.searcher;

import java.util.List;
import java.util.Map;

import fts.index.tokenizer.Tokenizer;
import fts.searcher.Calculator.CalcResult;
import fts.utils.Record;

public class SearcherImpl implements Searcher {

	Tokenizer tokenizer_;//コンストラクタで受け取る。

	List<Record> sortedRecords_;
	int resultNum_ = -1;

	/*
	 * トークン全てが出現する文書IDの集合。
	 * （トークン全てが出現するだけではフレーズとして出現するとは限らないことに注意）
	 * Map<文書ID, Map<トークンID, List<p1,p2,...>>>
	 * List<p1,p2,...>はポジションのリスト。
	 */
	List<Integer> candidateDocs_;
	Map<Integer, Integer> bases_;

	/*
	 * candidateDocs_のkeySetで表わされる文書IDの集合について、
	 * フレーズとして出現する回数をカウントしたマップ。
	 */
	Map<Integer, Integer> phraseCounts_;

	/*
	 * candidateDocs_のkeySetで表わされる文書IDの集合について、
	 * tf-idf値を算出したマップ。
	 */
	CalcResult tfIdfs_;
	SearchResult searchResult_;

	public SearcherImpl(Tokenizer tokenizer) {
		if (tokenizer == null) {
			throw new IllegalArgumentException("null tokenizer!");
		}
		tokenizer_ = tokenizer;
	}

	@Override
	public SearchResult search(String query, int n) {
		if (sortedRecords_ == null) {
			QueryAnalyzer queryAnalyzer = new QueryAnalyzerImpl(query, tokenizer_);
			Retriever recordAcquirer = new RetrieverImpl(queryAnalyzer.sortedToekns());
			sortedRecords_ = recordAcquirer.getSortedRecords();
		}

		if (candidateDocs_ == null) {
			CandidateDocsPicker picker = new CandidateDocsPickerImpl(sortedRecords_);
			candidateDocs_ = picker.getCandidateDocs();
		}

		//System.out.println("SearchImpl#search sortedRecords_:");
		//sortedRecords_.forEach(r -> System.out.print(r.toString()));
		if (phraseCounts_ == null) {
			PhraseCounter checker = new PhraseCounterImpl(sortedRecords_, candidateDocs_);
			phraseCounts_ = checker.phraseCheck();
		}

		if (tfIdfs_ == null) {
			Calculator evaluator = new CalculatorForTfIdf(tokenizer_, phraseCounts_, sortedRecords_.size());
			tfIdfs_ = evaluator.calculate();
		}

		System.out.println("SearcherImpl#search	tfIdfs:" + tfIdfs_.toString());

		if (searchResult_ == null) {
			searchResult_ = generateSearchResult(tfIdfs_);
		}
		return searchResult_;
	}

	private SearchResult generateSearchResult(CalcResult e) {
		SearchResult s = new SearchResult();
		e.forEach((docId, eval) -> s.put(eval, docId));
		return s;
	}

}
