package fts.searcher;

import java.util.List;

import fts.utils.Token;

public interface QueryAnalyzer {

	public List<Token> sortedToekns();
}
