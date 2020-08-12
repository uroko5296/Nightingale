package fts.index.tokenizer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import fts.utils.Token;

public abstract class TokenizerAbst implements Tokenizer {

	protected HashSet<Character> ignoredCharSet_;

	public TokenizerAbst(Collection<Character> ignoredChars) {
		assert (ignoredChars != null);
		ignoredCharSet_ = new HashSet<Character>(ignoredChars);
	}

	protected boolean isIgnoredChar(char c) {
		assert (ignoredCharSet_ != null);
		return ignoredCharSet_.contains(c);
	}

	@Override
	public abstract List<Token> parseAll(String text);
}
