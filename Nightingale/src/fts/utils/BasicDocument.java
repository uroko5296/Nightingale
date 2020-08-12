package fts.utils;

public class BasicDocument implements Document {
	/*
	 * 検索の対象となる単位。文書。
	 * コンテナクラス。
	 * titleとbody以外に拡張することがないなら、クラスとして定義する必要はないかも。
	 */
	private String title_;
	private String body_;

	public BasicDocument(String title, String body) {
		assert (title != null && body != null);
		title_ = title;
		body_ = body;
	}

	@Override
	public String getTitle() {
		return title_;
	}

	@Override
	public String getBody() {
		return body_;
	}

	@Override
	public int hashCode() {
		return title_.hashCode();
	}

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

		BasicDocument d = (BasicDocument) obj;
		return this.title_.equals(d.title_)
				&& this.body_.equals(d.body_);
	}

}
