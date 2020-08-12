package fts.utils;

public interface Record {

	int getTokenId();

	//i番目の文書ID
	int getDocumentIdOf(int i);

	//文書IDの数
	int getDocumentNum();

	//i番目の文書IDに関連付けられている中のj番目の位置
	int getPositionOf(int i, int j);

	//i番目の文書IDに関連付けられている位置の数
	int getPositionNumOf(int i);

	int getPositionInQuery();

	PostingList getPostingList();
}
