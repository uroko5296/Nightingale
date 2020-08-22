package fts.searcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import fts.utils.PostingList;
import fts.utils.Token;

public class PhraseCounterImpl implements PhraseCounter {

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

	/*
	 * phraseCount_;
	 * キーは文書ID
	 * 値は、フレーズの出現回数
	 */
	Map<Integer, Integer> phraseCounts_;

	//.reduce((acc, t) -> acc.addAll(t.keySet())
	public PhraseCounterImpl(
			List<Token> tokenList,
			List<PostingList> postingListList,
			List<Integer> candidateDocs) {
		tokenList_ = tokenList;
		postingListList_ = postingListList;
		candidateDocs_ = candidateDocs;
	}

	@Override
	public Map<Integer, Integer> phraseCheck() {

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
			Set<Integer> iSet = new HashSet<Integer>();
			for (int i = 0; i < positionSetList.size(); i++) {
				HashSet<Integer> newSet = new HashSet<Integer>();
				SortedSet<Integer> oldSet = positionSetList.get(i);
				int base = i;
				oldSet.forEach(pos -> newSet.add(pos - base));
				iSet = i == 0 ? newSet : Sets.intersection(iSet, newSet);
			}

			phraseCounts.put(docIdToCheck, iSet.size());
		});
		return phraseCounts;

	}

}
