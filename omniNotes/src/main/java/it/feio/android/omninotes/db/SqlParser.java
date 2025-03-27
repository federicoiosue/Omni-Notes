/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.feio.android.omninotes.db;

import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


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
      try (BufferedReader buffReader = new BufferedReader(new InputStreamReader(is))) {
          String line;
          String multiLineComment = null;
          
          while ((line = buffReader.readLine()) != null) {
              line = line.trim();
              
              if (multiLineComment != null) {
                  multiLineComment = checkMultiLineCommentEnd(multiLineComment, line);
                  continue;
              }
              
              if (isStartOfMultiLineComment(line)) {
                  multiLineComment = getMultiLineCommentStart(line);
                  continue;
              }
              
              if (!isSingleLineComment(line) && !line.isEmpty()) {
                  sql.append(" ").append(line);
              }
          }
      }
      return sql.toString();
  }

  private static boolean isSingleLineComment(String line) {
      return line.startsWith("--");
  }

  private static boolean isStartOfMultiLineComment(String line) {
      return line.startsWith("/*") || line.startsWith("{");
  }

  private static String getMultiLineCommentStart(String line) {
      return (!line.endsWith("}")) ? (line.startsWith("/*") ? "/*" : "{") : null;
  }

  private static String checkMultiLineCommentEnd(String multiLineComment, String line) {
      return (multiLineComment.equals("/*") && line.endsWith("*/")) || (multiLineComment.equals("{") && line.endsWith("}")) 
              ? null 
              : multiLineComment;
  }

  private static List<String> splitSqlScript(String script, char delim) {
    List<String> statements = new ArrayList<>();
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
