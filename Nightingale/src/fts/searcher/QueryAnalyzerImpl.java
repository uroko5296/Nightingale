package fts.searcher;

import java.util.List;

import fts.index.tokenizer.Tokenizer;
import fts.utils.Token;

public class QueryAnalyzerImpl extends QueryAnalyzerAbst {

	String query_;

	public QueryAnalyzerImpl(String query, Tokenizer tokenizer) {
		super(query, tokenizer);
		query_ = query;
	}

	@Override
	public List<Token> sortedToekns() {
		// TODO 自動生成されたメソッド・スタブ
		return tokenizer_.parseAll(query_);
	}

}
