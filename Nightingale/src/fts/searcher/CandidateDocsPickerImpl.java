package fts.searcher;

import java.util.ArrayList;
import java.util.List;

import fts.utils.Record;

public class CandidateDocsPickerImpl implements CandidateDocsPicker {

	List<Record> sortedRecords_;
	CandidateRecords candidateRecords_;//カーソル位置が更新されるため、入力変数として保持すべきでない。カーソル位置は別の変数とするなど考える。その場合はsortedRecordsで足りることも考えられるのでcandidateRecordsクラス自体の必要性も検討する。
	List<Integer> candidateDocs_;

	public CandidateDocsPickerImpl(List<Record> sortedRecords) {
		sortedRecords_ = sortedRecords;
	}

	@Override
	public List<Integer> getCandidateDocs() {
		if (candidateDocs_ == null) {
			candidateDocs_ = searchDocs(sortedRecords_);
		}
		return candidateDocs_;
	}

	/*
	 * ①まず0番目のレコード以外をnextDocIdにそろえようと試みる。
	 * ②次に0番目のレコードをnextDocIdにそろえようと試みる。
	 * ③すべてのレコードがnextDocIdにそろったかを確かめる。
	 * ④すべてのレコードがnextDocIdにそろった場合は検索結果候補とする。
	 * ⑤検索結果候補についてフレーズとしてクエリに一致するかを確かめる。
	 * ⑥フレーズに一致する場合は類似度を算出して検索結果とする。
	 */
	/*
	 * フィルタとしてストリームで実装できる？？？？？？？
	 */
	private List<Integer> searchDocs(List<Record> sortedRecords) {
		List<Integer> candidateDocIds = new ArrayList<Integer>();
		if (sortedRecords == null)
			return candidateDocIds;

		CandidateRecords candidateRecords = new CandidateRecords(sortedRecords);

		while (candidateRecords.hasDocumentIdOf(0)) {
			int docId = candidateRecords.getDocumentIdOf(0);
			int nextDocId = 0;

			/*
			 * 0番目以外のレコードについて、docId以上になるように進める。
			 */
			for (int i = 1; i < candidateRecords.getRecordNum(); i++) {
				while (candidateRecords.hasDocumentIdOf(i)
						&& candidateRecords.getDocumentIdOf(i) < docId) {
					candidateRecords.incrementCursor(i);
				}
				if (!candidateRecords.hasDocumentIdOf(i)) {
					return candidateDocIds;//i番目のレコードにおいてポスティングリストを見終えた。
				}
				/*
				 * 0番目以外のレコードでdocIdを超えてかつdocIdと異なることになったら、
				 * nextDocIdを設定する。これが起こるとnextDocIdが0より大きくなる。
				 */
				if (candidateRecords.getDocumentIdOf(i) > docId) {
					nextDocId = candidateRecords.getDocumentIdOf(i);
					break;//他の候補は見ずに抜ける。
				}
			}

			/*
			 * nextDocIdが設定されているかにより、分岐を判断する。
			 * 設定されているなら（nextDocId > 0なら）0番目のレコードを進める
			 * 設定されていないなら（すべて合致したので）検索結果とする
			 */
			if (nextDocId > 0) {
				while (candidateRecords.getDocumentIdOf(0) < nextDocId) {
					candidateRecords.incrementCursor(0);
				}
			} else {
				int documentIdToCheck = candidateRecords.getDocumentIdOf(0);
				candidateDocIds.add(documentIdToCheck);
				candidateRecords.incrementCursor(0);
			}
		}

		return candidateDocIds;

	}

	public class CandidateRecords {

		List<Record> sortedRecords_;
		int[] cursors_;//各レコードのポスティングリストにおける未チェックの最小カーソル位置

		public CandidateRecords(List<Record> sortedRecords) {
			assert (sortedRecords_.size() > 0);
			sortedRecords_ = sortedRecords;
			cursors_ = new int[sortedRecords_.size()];//初期値はすべて0
		}

		public int getRecordNum() {
			return cursors_.length;
		}

		public int tokenIdOf(int rId) {
			return sortedRecords_.get(rId).getTokenId();
		}

		//rId番目のレコードから、現在のカーソルが指す位置の文書IDを取得する
		public int getDocumentIdOf(int rId) {
			return getDocumentIdOfCursor(rId, cursors_[rId]);
		}

		//rId番目のレコードから、cur番目の位置の文書IDを取得する
		public int getDocumentIdOfCursor(int rId, int cur) {
			Record r = sortedRecords_.get(rId);
			return r.getDocumentIdOf(cur);
		}

		public void incrementCursor(int i) {
			cursors_[i] = cursors_[i] + 1;
		}

		public boolean hasRecord(int rId) {
			return rId < sortedRecords_.size();
		}

		public boolean hasDocumentIdOf(int rId) {
			Record r = sortedRecords_.get(rId);
			return cursors_[rId] < r.getDocumentNum();
		}

	}

}
