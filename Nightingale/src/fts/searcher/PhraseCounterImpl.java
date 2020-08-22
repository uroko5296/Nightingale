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

		if (map_ == null) {
			//map_ = docIdsToMap(candidateDocs_, tRecords_);
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

	private Map<Integer, Map<Integer, List<Integer>>> docIdsToMap(
			List<Integer> docIds,
			List<TRecord> tRecords) {

		Map<Integer, Map<Integer, List<Integer>>> map = new HashMap<Integer, Map<Integer, List<Integer>>>();
		if (docIds == null)
			return map;

		docIds.forEach(docIdToCheck -> {

			//フレーズサーチをするためのマップに追加する。
			Map<Integer, List<Integer>> tokenToPositions = new HashMap<Integer, List<Integer>>();
			//レコードすなわちトークンについてループを回す
			for (int i = 0; i < tRecords.size(); i++) {
				List<Integer> postingList = postingListList_.get(i).get(docIdToCheck).stream()
						.collect(Collectors.toList());

				int tokenId = tRecords.get(i).getTokenId();
				tokenToPositions.put(tokenId, postingList);
			}
			map.put(docIdToCheck, tokenToPositions);

		});
		return map;
	}

	/*
	 * ポスティングリストが文書IDの次に「出現位置で」ソートされている必要があるのはこの関数のためである。
	 */
	/*
	 * phraseCount
	 */
	private int searchPhrase(int docIdToCheck) {

		int phraseCount = 0;

		//トークンID⇒ポジションリスト
		Map<Integer, List<Integer>> positionLists = map_.get(docIdToCheck);

		//初期値はすべて０
		int[] cursors = new int[map_.get(docIdToCheck).keySet().size()];

		while (cursors[0] < positionLists.get(tokens_[0]).size()) {

			int relPosition = positionLists.get(tokens_[0]).get(cursors[0]) - bases_[0];
			int nextRelPosition = relPosition;
			for (int i = 1; i < cursors.length; i++) {
				List<Integer> positionList = positionLists.get(tokens_[i]);
				while (cursors[i] < positionList.size()
						&& positionList.get(cursors[i]) - bases_[i] < relPosition) {
					cursors[i] = cursors[i] + 1;
				}

				if (cursors[i] >= positionList.size()) {
					return phraseCount;//i番目のレコードにおいてポスティングリスト(文書IDがdocuIdToCheckのもの）を見終えた。
				}
				if (positionList.get(cursors[i]) - bases_[i] > relPosition) {
					nextRelPosition = positionList.get(cursors[i]) - bases_[i];
					break;//他の候補は見ずに抜ける。
				}
			}

			if (nextRelPosition > relPosition) {
				while (cursors[0] < positionLists.get(tokens_[0]).size()
						&& positionLists.get(tokens_[0]).get(cursors[0]) - bases_[0] < nextRelPosition) {
					cursors[0] = cursors[0] + 1;
				}
			} else {
				//フレーズが一致
				phraseCount++;
				cursors[0] = cursors[0] + 1;
			}
		}
		return phraseCount;
	}

}
