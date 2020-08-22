package fts.searcher;

import java.util.List;
import java.util.Map;

import fts.searcher.Calculator.CalcResult;
import fts.tokenizer.Tokenizer;
import fts.utils.PostingList;
import fts.utils.Record;
import fts.utils.Token;

public class SearcherImpl implements Searcher {

	Tokenizer tokenizer_;//コンストラクタで受け取る。

	List<Token> tokenList_;
	List<PostingList> postingListList;
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
		if (tokenList_ == null) {
			tokenList_ = tokenizer_.parseAll(query);
		}

		if (postingListList == null) {
			Retriever retriever = new RetrieverImpl(tokenList_);
			postingListList = retriever.getPostingListList();
			sortedRecords_ = retriever.getSortedRecords();
		}

		if (candidateDocs_ == null) {
			CandidateDocsPicker picker = new CandidateDocsPickerImpl(postingListList);
			candidateDocs_ = picker.getCandidateDocs();
		}

		if (phraseCounts_ == null) {
			PhraseCounter checker = new PhraseCounterImpl(sortedRecords_, candidateDocs_);
			phraseCounts_ = checker.phraseCheck();
		}

		if (tfIdfs_ == null) {
			Calculator evaluator = new CalculatorForTfIdf(tokenizer_, phraseCounts_, tokenList_.size());
			tfIdfs_ = evaluator.calculate();
		}

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
