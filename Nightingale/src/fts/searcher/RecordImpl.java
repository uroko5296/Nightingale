package fts.searcher;

import fts.utils.PostingList;

class RecordImpl implements Record {

	private final int tokenId_;//idで持つこととする。
	//private final Token token_;

	/*
	 * 文書ID順→位置順にソート済みのリスト。
	 * SortedMap<Integer, SortedSet<Integer>>
	 */
	private final PostingList postingList_;
	private int[] documentIds_;
	private int[][] positions_;

	private final int positionInQuery_;

	RecordImpl(int tokenId, PostingList postingList, int positionInQuery) {
		tokenId_ = tokenId;
		postingList_ = postingList;
		documentIds_ = postingList.keySet().stream().mapToInt(d -> d).toArray();
		positions_ = new int[documentIds_.length][];
		for (int i = 0; i < documentIds_.length; i++) {
			positions_[i] = postingList.get(documentIds_[i]).stream().mapToInt(p -> p).toArray();
		}
		positionInQuery_ = positionInQuery;
	}

	@Override
	public int getTokenId() {
		return tokenId_;
	}

	@Override
	public PostingList getPostingList() {
		return postingList_;
	}

	@Override
	public int getPositionInQuery() {
		return positionInQuery_;
	}

	@Override
	public String toString() {
		return "$R{" + "(t:" + tokenId_ + "," + "b:" + positionInQuery_ + ")->" + postingList_.toString() + "}";
	}

	@Override
	public int getDocumentIdOf(int i) {
		return documentIds_[i];
	}

	@Override
	public int getPositionOf(int i, int j) {
		return positions_[i][j];
	}

	@Override
	public int getDocumentNum() {
		return documentIds_.length;
	}

	@Override
	public int getPositionNumOf(int i) {
		return positions_[i].length;
	}

}
