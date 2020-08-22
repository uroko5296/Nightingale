package fts.searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import fts.database.DBManager.TRecord;
import fts.utils.PostingList;
import fts.utils.Token;

public class PhraseCounterImpl implements PhraseCounter {

	List<TRecord> tRecords_;

	List<Token> tokenList_;

	List<PostingList> postingListList_;
	/*
	 * candidateDocs_：
	 * 候補となる文献IDのリスト。
	 */
	List<Integer> candidateDocs_;

	/*
	 * candidateDocs_：
	 * キーは候補となる文書ID
	 * 値は、トークンID⇒ポジションリスト
	 */
	Map<Integer, Map<Integer, List<Integer>>> map_;

	/*
	 * tokens_: トークンIDの配列
	 * bases_：ベース位置（そのもの）の配列
	 * tokens_とbases_は要素位置で対応している。
	 */
	int[] tokens_;
	int[] bases_;

	/*
	 * phraseCount_;
	 * キーは文書ID
	 * 値は、フレーズの出現回数
	 */
	Map<Integer, Integer> phraseCounts_;

	//.reduce((acc, t) -> acc.addAll(t.keySet())
	public PhraseCounterImpl(
			List<Token> tokenList,
			List<TRecord> tRecords,
			List<PostingList> postingListList,
			List<Integer> candidateDocs) {
		tokenList_ = tokenList;
		tRecords_ = tRecords;
		postingListList_ = postingListList;
		candidateDocs_ = candidateDocs;
	}

	@Override
	public Map<Integer, Integer> phraseCheck() {
		if (tokens_ == null) {
			tokens_ = tRecords_.stream().map(r -> r.getTokenId()).mapToInt(t -> t).toArray();
		}
		if (bases_ == null) {
			bases_ = new int[tRecords_.size()];
			for (int i = 0; i < bases_.length; i++) {
				bases_[i] = i;
			}
		}

		if (phraseCounts_ == null) {
			phraseCounts_ = makePhraseCounts(candidateDocs_, postingListList_);
		}

		return phraseCounts_;
	}

	/*
	 * postingListListの並び順はクエリにあるままのトークン順である前提である
	 */
	private Map<Integer, Integer> makePhraseCounts(List<Integer> docIds, List<PostingList> postingListList) {
		Map<Integer, Integer> phraseCounts = new HashMap<Integer, Integer>();

		docIds.forEach(docIdToCheck -> {
			List<SortedSet<Integer>> positionSetList = postingListList.stream().map(pl -> pl.get(docIdToCheck))
					.collect(Collectors.toList());
			List<HashSet<Integer>> rebasedPositionSetList = new ArrayList<HashSet<Integer>>();//ここでListを生成する必要はない１
			for (int i = 0; i < positionSetList.size(); i++) {
				HashSet<Integer> newSet = new HashSet<Integer>();
				SortedSet<Integer> oldSet = positionSetList.get(i);
				int base = i;
				oldSet.forEach(pos -> newSet.add(pos - base));
				rebasedPositionSetList.add(newSet);//ここでListを生成する必要はない２
			}

			if (rebasedPositionSetList.isEmpty()) {
				phraseCounts.put(docIdToCheck, 0);
			} else {

				Set<Integer> iSet = rebasedPositionSetList.get(0);
				if (rebasedPositionSetList.size() > 1) {
					for (int j = 1; j < rebasedPositionSetList.size(); j++) {
						iSet = Sets.intersection(iSet, rebasedPositionSetList.get(j));
					}
				}
				phraseCounts.put(docIdToCheck, iSet.size());
			}
		});
		return phraseCounts;

	}

}
