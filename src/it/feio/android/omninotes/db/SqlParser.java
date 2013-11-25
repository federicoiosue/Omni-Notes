package it.feio.android.omninotes.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;

public class SqlParser {

	public static List<String> parseSqlFile(String sqlFile,
			AssetManager assetManager) throws IOException {
		List<String> sqlIns = null;
		InputStream is = assetManager.open(sqlFile);
		try {
			sqlIns = parseSqlFile(is);
		} finally {
			is.close();
		}
		return sqlIns;
	}

	public static List<String> parseSqlFile(InputStream is) throws IOException {
		String script = removeComments(is);
		return splitSqlScript(script, ';');
	}

	private static String removeComments(InputStream is) throws IOException {

		StringBuilder sql = new StringBuilder();

		InputStreamReader isReader = new InputStreamReader(is);
		try {
			BufferedReader buffReader = new BufferedReader(isReader);
			try {
				String line;
				String multiLineComment = null;
				while ((line = buffReader.readLine()) != null) {
					line = line.trim();

					if (multiLineComment == null) {
						if (line.startsWith("/*")) {
							if (!line.endsWith("}")) {
								multiLineComment = "/*";
							}
						} else if (line.startsWith("{")) {
							if (!line.endsWith("}")) {
								multiLineComment = "{";
							}
						} else if (!line.startsWith("--") && !line.equals("")) {
							sql.append(" " + line);
						}
					} else if (multiLineComment.equals("/*")) {
						if (line.endsWith("*/")) {
							multiLineComment = null;
						}
					} else if (multiLineComment.equals("{")) {
						if (line.endsWith("}")) {
							multiLineComment = null;
						}
					}

				}
			} finally {
				buffReader.close();
			}

		} finally {
			isReader.close();
		}

		return sql.toString();
	}

	private static List<String> splitSqlScript(String script, char delim) {
		List<String> statements = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		boolean inLiteral = false;
		char[] content = script.toCharArray();
		for (int i = 0; i < script.length(); i++) {
			if (content[i] == '\'') {
				inLiteral = !inLiteral;
			}
			if (content[i] == delim && !inLiteral) {
				if (sb.length() > 0) {
					statements.add(sb.toString().trim());
					sb = new StringBuilder();
				}
			} else {
				sb.append(content[i]);
			}
		}
		if (sb.length() > 0) {
			statements.add(sb.toString().trim());
		}
		return statements;
	}

}