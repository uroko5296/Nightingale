package fts.tokenizer;

import java.util.List;

import fts.utils.Token;

public interface Tokenizer {

	/*
		public void goNext();//パースを一つ進める。
		public boolean hasNext();//次にパースを進められるか。
		public int getPosition();//現在のカーソル位置。（0スタート。）
		public Token getToken();//最後にパースしたトークン。
	*/
	public List<Token> parse(String str);

	public List<Token> parse(List<String> strList);
}
