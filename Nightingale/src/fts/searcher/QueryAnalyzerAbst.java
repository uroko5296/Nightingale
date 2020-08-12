package fts.searcher;

import fts.index.tokenizer.Tokenizer;

public abstract class QueryAnalyzerAbst implements QueryAnalyzer {

	protected Tokenizer tokenizer_;

	public QueryAnalyzerAbst(String query, Tokenizer tokenizer) {
		tokenizer_ = tokenizer;
	}

}
