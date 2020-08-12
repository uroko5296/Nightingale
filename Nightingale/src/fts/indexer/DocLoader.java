package fts.indexer;

import fts.tokenizer.Tokenizer;

public interface DocLoader {

	void setTokenizer(Tokenizer tokenzer);

	void setMaxDocumentCount(int n);

	void setFilePath(String path);

	//tokenizerとtableをsetしてからloadする。
	void load() throws Exception;//xmlやファイルの次の単位を読み込む

}
