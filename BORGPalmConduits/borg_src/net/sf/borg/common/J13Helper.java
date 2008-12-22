package net.sf.borg.common;

// functions to help out code running in the Palm conduits, which must be Java 1.3
public class J13Helper {
	public static String replace(String source, String pattern, String replace) {
		if (source != null) {
			final int len = pattern.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int start = 0;

			while ((found = source.indexOf(pattern, start)) != -1) {
				sb.append(source.substring(start, found));
				sb.append(replace);
				start = found + len;
			}

			sb.append(source.substring(start));

			return sb.toString();
		}

		return "";
	}

}
