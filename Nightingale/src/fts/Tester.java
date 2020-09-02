package fts;

import java.util.ArrayList;
import java.util.List;

public class Tester {

	private static List<String[]> ARGS;
	static {
		ARGS = new ArrayList<String[]>();
		ARGS.add(new String[] { "-s", "情報学" });
		ARGS.add(new String[] { "-s", "日本語" });
		ARGS.add(new String[] { "-s", "世界経済" });
		ARGS.add(new String[] { "-s", "情報学" });
		ARGS.add(new String[] { "-s", "日本語 プログラミング言語" });
	}

	public static void main(String[] args) throws Exception {

		for (int i = 0; i < ARGS.size(); i++) {
			System.out.println("--CASE[" + (i + 1) + "]--");
			Main.main(ARGS.get(i));
			System.out.println("-----------");
		}
	}

}
