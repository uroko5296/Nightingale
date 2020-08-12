package fts.index.tokenizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fts.utils.Token;

public class NGramTokenizer extends TokenizerAbst {

	int n_;//n-gram

	public NGramTokenizer(int n, Collection<Character> ignoredChars) {
		super(ignoredChars);
		n_ = n;
	}

	@Override
	public List<Token> parseAll(String text) {
		System.out.println("NGramTokneizer.parseAll:	text:[" + text + "]");
		/*
		 * 本書のtext_to_postings_listsに実質的に対応。
		 * テーブルに、文書IDとテキストであらわされる文書を、n-gramで分割しつつマージする。
		 * ラインのトークンに対してループを回して、テーブルに追加する。
		 * つまり、トークンとポスティングリストの組を追加する。
		 */
		List<Token> tokenList = new ArrayList<Token>();

		//最初に、tの冒頭の空白などを読み飛ばす。
		for (int i = 0; super.isIgnoredChar(text.charAt(i)); i++)
			text = text.substring(1, text.length());

		//Map<String, ArrayList<Integer>> line = new HashMap<String,ArrayList<Integer>>();
		for (int i = 0; i < text.length(); i++) {//トークンの開始位置についてのループ
			int m = 0;
			boolean useBreak = false;
			for (m = 0; m < n_; m++) {//トークンの長さを一文字ずつ増加させるループ
				/*
				 * トークン長さがnになる前に空白文字などに当たった場合は、使う。
				 */
				System.out.println("i:" + i + "	m:" + m);
				if (super.isIgnoredChar(text.charAt(i + m))) {
					useBreak = true;
					break;//空白文字に当たれば、とにかく内側のループを抜ける。
				}
			}
			/*
			 * 検索時は、n文字未満ものはマップに追加しない。
			 * mが0の時は、空白が連続しているときである
			 */
			if (m > 0 && m >= n_)
				tokenList.add(new Token(text.substring(i, i + m)));
			//見たところまでiを進めておく。フラグを使っているのがなんかダサい。要改善。
			i = useBreak ? i + m : i + m - 1;//breakした場合はm++がされないので帳尻合わせ。
		}

		return tokenList;
	}

}
