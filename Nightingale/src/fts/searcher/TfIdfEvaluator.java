package fts.searcher;

import java.util.Map;

import fts.database.DBManager;
import fts.database.DBManagerForMySQL;
import fts.index.tokenizer.Tokenizer;

public class TfIdfEvaluator implements Evaluator {

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

	//List<Token> tokenizedQuery_;
	//フレーズはクエリに対応して一つ存在するとする。
	//クエリが分割された場合は今は考えない。

	/*
	 * 出力値。文書IDからTf-Idf値へのマップ。
	 */
	EvalResult tfIdfs_;

	public TfIdfEvaluator(
			Tokenizer tokenizer,
			Map<Integer, Integer> phraseCounts,
			int tokensPerPhrases) {
		dbManager_ = new DBManagerForMySQL();
		tokenizer_ = tokenizer;
		phraseCounts_ = phraseCounts;
		tokensPerPhrases_ = tokensPerPhrases;
	}

	@Override
	public EvalResult evaluate() {
		if (tfIdfs_ != null)
			return tfIdfs_;

		if (N_ < 0) {
			N_ = dbManager_.getTotalDocumentNum();
		}

		int df = phraseCounts_.keySet().size();

		tfIdfs_ = new EvalResult();
		phraseCounts_.keySet().forEach((Integer d) -> {
			int phraseCount = phraseCounts_.get(d);
			int totalCount = getTotalCountOf(d);
			double tfIdf = calcTfIdf(phraseCount, totalCount, df, N_);

			tfIdfs_.put(d, tfIdf);
		});

		return tfIdfs_;
	}

	private int getTotalCountOf(Integer d) {
		String body = dbManager_.dbGetBodyOfDocument(d);
		int totalTokenNum = tokenizer_.parseAll(body).size();
		int totalPhraseNum = totalTokenNum / tokensPerPhrases_;
		return totalPhraseNum;
	}

	private double calcTfIdf(int phraseCount, int totalCount, int df, int N) {
		double tf = (double) phraseCount / (double) totalCount;
		double idf = Math.log(N / df) + 1;
		return tf / idf;
	}

}
