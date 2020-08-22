package fts.searcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fts.utils.Record;

public class PhraseCounterImpl implements PhraseCounter {

	List<Record> sortedRecords_;
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
			List<Record> sortedRecords,
			List<Integer> candidateDocs) {
		sortedRecords_ = sortedRecords;
		candidateDocs_ = candidateDocs;
	}

	@Override
	public Map<Integer, Integer> phraseCheck() {
		if (tokens_ == null) {
			tokens_ = sortedRecords_.stream().map(r -> r.getTokenId()).mapToInt(t -> t).toArray();
		}
		if (bases_ == null) {
			bases_ = sortedRecords_.stream().map(r -> r.getPositionInQuery()).mapToInt(t -> t).toArray();
		}
		if (tokens_.length != bases_.length) {
			throw new IllegalArgumentException("Illegal array length!");
		}

		if (map_ == null) {
			map_ = docIdsToMap(candidateDocs_, sortedRecords_);
		}

		if (phraseCounts_ == null) {
			phraseCounts_ = new HashMap<Integer, Integer>();
			candidateDocs_.forEach(d -> {
				phraseCounts_.put(d, searchPhrase(d));
			});
		}
		return phraseCounts_;
	}

	private Map<Integer, Map<Integer, List<Integer>>> docIdsToMap(
			List<Integer> docIds,
			List<Record> sortedRecords) {

		Map<Integer, Map<Integer, List<Integer>>> map = new HashMap<Integer, Map<Integer, List<Integer>>>();
		if (docIds == null)
			return map;

		System.out.println("PhraseCounterImpl#docIdsToMap docIds:" + docIds);
		docIds.forEach(docIdToCheck -> {

			//フレーズサーチをするためのマップに追加する。
			Map<Integer, List<Integer>> tokenToPositions = new HashMap<Integer, List<Integer>>();
			//レコードすなわちトークンについてループを回す
			for (int i = 0; i < sortedRecords.size(); i++) {
				List<Integer> postingList = sortedRecords.get(i).getPostingList().get(docIdToCheck).stream()
						.collect(Collectors.toList());

				int tokenId = sortedRecords.get(i).getTokenId();
				tokenToPositions.put(tokenId, postingList);
			}
			map.put(docIdToCheck, tokenToPositions);

		});
		System.out.println("PhraseCounterImpl#docIdsToMap map:" + map);
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
