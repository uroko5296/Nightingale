package fts.tokenizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fts.utils.Token;

public class NGramTokenizer extends TokenizerAbst {

	int n_;//n-gram

	public NGramTokenizer(int n, Collection<Character> ignoredChars) {
		super(ignoredChars);
		n_ = n;
	}

	@Override
	public List<Token> parse(String text) {
		/*
		 * 本書のtext_to_postings_listsに実質的に対応。
		 * テーブルに、文書IDとテキストであらわされる文書を、n-gramで分割しつつマージする。
		 * ラインのトークンに対してループを回して、テーブルに追加する。
		 * つまり、トークンとポスティングリストの組を追加する。
		 */
		List<Token> tokenList = new ArrayList<Token>();
		/*
		 * 対応するtextの位置から先頭方向に何文字だけ無視文字を踏まずに進めるか。
		 * 動的計画法のための配列。
		 * つまりnonIgnoredSeqence[0] = 0;
		 */
		int[] nonIgnoredSequence = new int[text.length() + 1];//先頭は番人（空文字扱い）
		int cursor = 0;
		nonIgnoredSequence[cursor] = 0;
		/*
		 * カーソルはテキストに対して常に＋１されていることに注意
		 */
		for (cursor = 1; cursor < text.length() + 1; cursor++) {
			/*
			 * 動的計画法で配列を更新する。
			 * 無視文字を踏んだら0、そうでなければ直前の位置＋１で計算できる。
			 */
			nonIgnoredSequence[cursor] = super.isIgnoredChar(text.charAt(cursor - 1))
					? 0
					: nonIgnoredSequence[cursor - 1] + 1;

			/*
				 * 無視文字を含まない文字列の長さがnに達しているとき、cursor位置を出力する。、
				 * 無視文字を踏んだときかつ直前にトークンを生成していないときは、cursor-1位置を出力する。
				 * 末尾に達したときかつ直前にトークン生成をしていない場合は、cursor位置を出力する。
				 * トークン生成をする。
				 */
			if (nonIgnoredSequence[cursor] >= n_) {//長さがnに達している場合。
				tokenList.add(new Token(text.substring(cursor - n_, cursor)));
			} else if (nonIgnoredSequence[cursor] == 0
					&& nonIgnoredSequence[cursor - 1] < n_
					&& nonIgnoredSequence[cursor - 1] > 0) {//2つめ場合。直前の長さが1以上の必要もあることに注意。
				tokenList.add(
						new Token(
								text.substring(
										(cursor - 1) - nonIgnoredSequence[cursor - 1],
										cursor - 1)));
				assert (!tokenList.get(tokenList.size() - 1).getToken().equals(""));
			} else if (cursor == text.length()
					&& nonIgnoredSequence[cursor - 1] < n_
					&& nonIgnoredSequence[cursor - 1] > 0) {//3つめ場合。直前の長さが1以上の必要もあることに注意。
				tokenList.add(
						new Token(
								text.substring(
										cursor - nonIgnoredSequence[cursor],
										cursor)));
				assert (!tokenList.get(tokenList.size() - 1).getToken().equals(""));
			}
		}
		return tokenList;
	}

	@Override
	public List<Token> parse(List<String> strList) {
		return strList.stream().flatMap(str -> this.parse(str).stream()).collect(Collectors.toList());
	}

}
