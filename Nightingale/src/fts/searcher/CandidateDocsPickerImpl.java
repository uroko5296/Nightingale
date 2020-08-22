package fts.searcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import fts.utils.PostingList;

public class CandidateDocsPickerImpl implements CandidateDocsPicker {

	List<PostingList> postingListList_;

	List<Integer> candidateDocs_;

	public CandidateDocsPickerImpl(List<PostingList> postingListList) {
		postingListList_ = postingListList;
	}

	@Override
	public List<Integer> getCandidateDocs() {
		if (candidateDocs_ == null) {
			candidateDocs_ = searchDocs(postingListList_);
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
	private List<Integer> searchDocs(List<PostingList> postingListList) {
		if (postingListList == null || postingListList.size() < 1)
			return new ArrayList<Integer>();

		List<Set<Integer>> docIdsList = postingListList.stream().map(p -> p.keySet())
				.collect(Collectors.toList());

		Set<Integer> docIds = new HashSet<Integer>();
		docIds.addAll(docIdsList.get(0));
		for (int i = 1; i < docIdsList.size(); i++) {
			docIds = Sets.intersection(docIds, docIdsList.get(i));
		}
		System.out.println("searchDocs2 docIds:" + docIds);

		return docIds.stream().collect(Collectors.toList());

	}

}
