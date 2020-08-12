package fts.utils;

public class RichDocument implements Document {
	/*
	 * 検索の対象となる単位。文書。
	 * コンテナクラス。
	 * titleとbody以外に拡張することがないなら、クラスとして定義する必要はないかも。
	 */
	private String title_;
	private String body_;

	private String url_;
	private int id_;

	public RichDocument(String title, String body, String url, int id) {
		assert (title != null && body != null && url != null && id > 0);
		title_ = title;
		body_ = body;
		url_ = url;
		id_ = id;
	}

	@Override
	public String getTitle() {
		return title_;
	}

	@Override
	public String getBody() {
		return body_;
	}

	public String getUrl() {
		return url_;
	}

	public int getId() {
		return id_;
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

		RichDocument d = (RichDocument) obj;
		return this.title_.equals(d.title_)
				&& this.body_.equals(d.body_)
				&& this.url_.equals(d.url_)
				&& this.id_ == d.id_;
	}

}
