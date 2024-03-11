package org.example;

public class Utils {
  private Utils() {};

  public static final String noCRLF(String text) {
    if (text ==null)
      return null;
    return text.replaceAll("\\n", " ").replaceAll("\\r", " ").trim();
  }

//  public static final String singleLine(String text) {
//    if (text ==null)
//      return null;
//    return text.replaceAll("\\n", " ").replaceAll("\\r", " ").trim() + "\n";
//  }
//
//  public static final String noBlankLine(String ... texts) {
//    if (texts ==null)
//      return null;
//    StringBuilder sb = new StringBuilder();
//    for (String t:texts) {
//      if (t != null && t.trim().length() != 0) {
//        sb.append(t);
//        sb.append('\n');
//      }
//    }
//    return sb.toString();
//  }
}
