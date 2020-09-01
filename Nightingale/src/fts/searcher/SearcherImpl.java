package fts.searcher;

import java.util.List;
import java.util.Map;

import fts.searcher.Calculator.CalcResult;
import fts.tokenizer.Tokenizer;
import fts.utils.PostingList;
import fts.utils.Token;

public class SearcherImpl implements Searcher {

	Tokenizer tokenizer_;//コンストラクタで受け取る。

	List<Token> tokenList_;
	List<PostingList> postingListList_;

	int resultNum_ = -1;

	/*
	 * トークン全てが出現する文書IDの集合。
	 * （トークン全てが出現するだけではフレーズとして出現するとは限らないことに注意）
	 * Map<文書ID, Map<トークンID, List<p1,p2,...>>>
	 * List<p1,p2,...>はポジションのリスト。
	 */
	List<Integer> candidateDocs_;

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
		long t0 = System.currentTimeMillis();
		if (tokenList_ == null) {
			tokenList_ = tokenizer_.parseAll(query);
		}

		long t1 = System.currentTimeMillis();
		if (postingListList_ == null) {
			Retriever retriever = new RetrieverImpl(tokenList_);
			postingListList_ = retriever.getPostingListList();
		}
		long t2 = System.currentTimeMillis();

		if (candidateDocs_ == null) {
			CandidateDocsPicker picker = new CandidateDocsPickerImpl(postingListList_);
			candidateDocs_ = picker.getCandidateDocs();
		}
		long t3 = System.currentTimeMillis();
		if (phraseCounts_ == null) {
			PhraseCounter checker = new PhraseCounterImpl(tokenList_, postingListList_, candidateDocs_);
			phraseCounts_ = checker.phraseCheck();
		}
		long t4 = System.currentTimeMillis();
		if (tfIdfs_ == null) {
			Calculator calculator = new CalculatorForTfIdf(tokenizer_, phraseCounts_, tokenList_.size());
			tfIdfs_ = calculator.calculate();
		}
		long t5 = System.currentTimeMillis();
		if (searchResult_ == null) {
			searchResult_ = generateSearchResult(tfIdfs_);
		}

		System.out.println("SearcherImpl#search T0:" + (t1 - t0) + "[ms]");
		System.out.println("SearcherImpl#search T1:" + (t2 - t1) + "[ms]");
		System.out.println("SearcherImpl#search T2:" + (t3 - t2) + "[ms]");
		System.out.println("SearcherImpl#search T3:" + (t4 - t3) + "[ms]");
		System.out.println("SearcherImpl#search T4:" + (t5 - t4) + "[ms]");

		return searchResult_;
	}

	private SearchResult generateSearchResult(CalcResult e) {
		SearchResult s = new SearchResult();
		e.forEach((docId, eval) -> s.put(eval, docId));
		return s;
	}

}
