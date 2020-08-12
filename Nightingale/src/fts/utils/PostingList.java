package fts.utils;

import java.util.SortedMap;
import java.util.SortedSet;

/*
 * ポスティングリストは記述子の一種。（？）
 * 記述子とは特徴量であって、トークン以外の特徴量を採用した場合には、
 * より上位の概念のインターフェースを実装する必要が出てくる。
 */
public interface PostingList extends SortedMap<Integer, SortedSet<Integer>> {

	/*
	 * ポスティングリスト（文書IDと位置情報のリンクリスト構造）
	 * 実態は、HashSet<Posting>であって、Postingはintの組
	 */
	public boolean add(Posting p);

	public void addAllByDecoding(String encodings);

	public int getDocsCount();

	public String encode();

	public class Posting {
		int documentId_;
		int position_;

		public Posting(int documentId, int position) {
			assert (documentId > -1 && position > -1);
			documentId_ = documentId;
			position_ = position;
		}

		@Override
		public int hashCode() {
			return documentId_;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;
			if (this.getClass() != obj.getClass())
				return false;

			Posting p = (Posting) obj;
			return this.documentId_ == p.documentId_ && this.position_ == p.position_;
		}

		@Override
		public String toString() {
			return "p{" + documentId_ + "," + position_ + "}";
		}

		public int getDocumentId() {
			return documentId_;
		}

		public int getPosition() {
			return position_;
		}
	}
}
