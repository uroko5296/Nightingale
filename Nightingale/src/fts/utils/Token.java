package fts.utils;

public class Token implements Comparable<Token> {

	private final String token_;

	public Token(String t) {
		if (t == null || t.equals(""))
			throw new IllegalArgumentException("string t is empty!");
		token_ = t;
	}

	public String getToken() {
		return token_;
	}

	/*
	 * Mapのキーとして用いるためオーバライドする。
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		Token t = (Token) obj;
		return this.token_.equals(t.token_);
	}

	@Override
	public int hashCode() {
		return this.token_.hashCode();
	}

	@Override
	public String toString() {
		return "t{" + token_ + "}";
	}

	@Override
	public int compareTo(Token o) {
		if (o == null) {
			// 引数が null の場合、比較する必要なし。
			throw new NullPointerException();
		}
		return this.token_.compareTo(o.getToken());
	}
}
