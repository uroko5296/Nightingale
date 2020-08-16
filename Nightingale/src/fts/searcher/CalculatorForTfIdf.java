package fts.searcher;

import java.util.Map;

import fts.database.DBManager;
import fts.database.DBManagerForMySQL;
import fts.tokenizer.Tokenizer;

public class CalculatorForTfIdf implements Calculator {

	/*
	 * Tf-Idf値の算出器。
	 * 単語(N-gramの場合はフレーズともいう)ごとに算出する。
	 * 当面はN-gram（つまり単語はフレーズ）で実装する。後でN-gram以外にも拡張する。
	 */

	//DBアクセス用。
	DBManager dbManager_;

	/*
	 * トークナイザ
	 * TF値を求める際の分母の計算に必要。
	 * DBから文書のBodyを読みだして、Tokeinzerに掛けた際のトークン数を用いる。
	 */
	Tokenizer tokenizer_;

	//文書IDから単語（N-gramの場合はフレーズ）の出現回数へのマップ
	Map<Integer, Integer> phraseCounts_;

	//フレーズ当たりのトークン数
	int tokensPerPhrases_;

	//データベースの総文書数
	int N_ = -1;

	//文書IDから文書サイズへのマップ
	Map<Integer, Integer> mapFromDocIdToBodySize_;

	//List<Token> tokenizedQuery_;
	//フレーズはクエリに対応して一つ存在するとする。
	//クエリが分割された場合は今は考えない。

	/*
	 * 出力値。文書IDからTf-Idf値へのマップ。
	 */
	CalcResult tfIdfs_;

	public CalculatorForTfIdf(
			Tokenizer tokenizer,
			Map<Integer, Integer> phraseCounts,
			int tokensPerPhrases) {
		dbManager_ = new DBManagerForMySQL();
		tokenizer_ = tokenizer;
		phraseCounts_ = phraseCounts;
		tokensPerPhrases_ = tokensPerPhrases;
	}

	@Override
	public CalcResult calculate() {
		if (tfIdfs_ != null)
			return tfIdfs_;

		if (N_ < 0) {
			N_ = dbManager_.getTotalDocumentNum();
		}

		if (mapFromDocIdToBodySize_ == null) {
			mapFromDocIdToBodySize_ = dbManager_.dbGetMapOfBodySizeOfDocument(phraseCounts_.keySet());
		}

		int df = phraseCounts_.keySet().size();

		tfIdfs_ = new CalcResult();
		phraseCounts_.keySet().forEach((Integer d) -> {
			int phraseCount = phraseCounts_.get(d);
			int totalCount = getTotalCountOf(d);
			double tfIdf = calcTfIdf(phraseCount, totalCount, df, N_);

			tfIdfs_.put(d, tfIdf);
		});

		return tfIdfs_;
	}

	private int getTotalCountOf(Integer d) {
		int totalTokenNum = mapFromDocIdToBodySize_.get(d);
		int totalPhraseNum = totalTokenNum / tokensPerPhrases_;
		return totalPhraseNum;
	}

	private double calcTfIdf(int phraseCount, int totalCount, int df, int N) {
		double tf = (double) phraseCount / (double) totalCount;
		double idf = Math.log(N / df) + 1;
		return tf / idf;
	}

}
