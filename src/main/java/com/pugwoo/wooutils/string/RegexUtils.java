package com.pugwoo.wooutils.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * 正则表达式相关工具<br>
 *
 * 如果需要区分大小写，则正则表达式前面加上 (?i)
 *
 * @author nick
 */
public class RegexUtils {


	/**
	 * 检查给定的字符串是否匹配上了正则
	 * @param str
	 * @param regex 正则表达式
	 * @return
	 */
	public static boolean isMatch(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return isMatch(str, pattern);
	}

	/**
	 * 检查给定的字符串是否匹配上了正则
	 * @param str
	 * @param pattern 正则表达式
	 * @return
	 */
	public static boolean isMatch(String str, Pattern pattern) {
		Matcher m = pattern.matcher(str);
		return m.find();
	}

	/**
	 * 获得第一个匹配到的字符串，推荐使用group regex，
	 * 将返回所有group的值的拼凑，推荐只用一个group，没有匹配到返回null。
	 * @param str
	 * @param regex
	 * @return
	 */
	public static String getFirstMatchStr(String str, String regex) {
		Pattern p = Pattern.compile(regex);
	    return getFirstMatchStr(str, p);
	}

	/**
	 * 获得第一个匹配到的字符串，推荐使用group regex，
	 * 将返回所有group的值的拼凑，推荐只用一个group，没有匹配到返回null。
	 * @param str
	 * @param pattern 正则表达式
	 * @return
	 */
	public static String getFirstMatchStr(String str, Pattern pattern) {
		Matcher m = pattern.matcher(str);
		while (m.find() == true) {
			int groupCount = m.groupCount();
			if(groupCount == 0) {
				return m.group();
			} else if (groupCount == 1) {
				return m.group(1);
			} else {
				StringBuilder sb = new StringBuilder();
				for(int i = 1; i <= groupCount; i++) {
					sb.append(m.group(i));
				}
				return sb.toString();
			}
		}
		return null;
	}
	
	/**
	 * 获得所有匹配到的字符串，推荐使用group regex，
	 * 将返回所有group的值的拼凑，推荐只用一个group，没有匹配到返回empty list。
	 * @param str
	 * @param regex
	 * @return
	 */
	public static List<String> getAllMatchStr(String str, String regex) {
		Pattern p = Pattern.compile(regex);
	    return getAllMatchStr(str, p);
	}

	/**
	 * 获得所有匹配到的字符串，推荐使用group regex，
	 * 将返回所有group的值的拼凑，推荐只用一个group，没有匹配到返回empty list。
	 * @param str
	 * @param pattern 正则表达式
	 * @return
	 */
	public static List<String> getAllMatchStr(String str, Pattern pattern) {
		Matcher m = pattern.matcher(str);
		List<String> result = new ArrayList<String>();
		while (m.find() == true) {
			int groupCount = m.groupCount();
			if(groupCount == 0) {
				result.add(m.group());
			} else if (groupCount == 1) {
				result.add(m.group(1));
			} else {
				StringBuilder sb = new StringBuilder();
				for(int i = 1; i <= groupCount; i++) {
					sb.append(m.group(i));
				}
				result.add(sb.toString());
			}
		}
		return result;
	}
	
	/**
	 * 替换所有匹配的字符串组
	 * 请求参数详见 {@link #replaceGroup(boolean, String, String, String...)}
	 */
	public static String replaceAllGroup(String str, String regex, String... replacements) {
		return replaceGroup(false, str, regex, replacements);
	}
	
	/**
	 * 替换所有匹配的字符串组
	 * 请求参数详见 {@link #replaceGroup(boolean, String, String, StringReplacementFunction...)}
	 */
	public static String replaceAllGroup(String str, String regex, StringReplacementFunction... replacementFunctions) {
		return replaceGroup(false, str, regex, replacementFunctions);
	}
	
	/**
	 * 替换第一个匹配的字符串组
	 * 请求参数详见 {@link #replaceGroup(boolean, String, String, String...)}
	 */
	public static String replaceFirstGroup(String str, String regex, String... replacements) {
		return replaceGroup(true, str, regex, replacements);
	}
	
	/**
	 * 替换第一个匹配的字符串组
	 * 请求参数详见 {@link #replaceGroup(boolean, String, String, StringReplacementFunction...)}
	 */
	public static String replaceFirstGroup(String str, String regex, StringReplacementFunction... replacementFunctions) {
		return replaceGroup(true, str, regex, replacementFunctions);
	}
	
	/**
	 * 字符串替换 <br/>
	 *  1. 普通替换，与 String.replaceAll String.replaceFirst功能一致 <br/>
	 *  2. 替换正则表达式捕获组(captureGroup)匹配到的字符串(单个/多个) <br/>
	 * @param first        是否仅替换第一个符合的匹配项
	 * @param str          待替换的字符串
	 * @param regex        正则表达式
	 * @param replacements 需要替换的字符，如果不需要替换，传入null
	 * @return 替换后的字符串
	 */
	private static String replaceGroup(boolean first, String str, String regex, String... replacements) {
		// 参数校验 不符合的直接返回str
		if (replacements == null || str == null || regex == null) { return str; }
		int replacementLength = replacements.length;
		if (replacementLength == 0) { return str; }
		Set<String> replacementSet = Stream.of(replacements).collect(toSet());
		if (replacementSet.size() == 1 && replacementSet.contains(null)) { return str; }
		
		StringReplacementFunction[] replacementFunctions = Stream.of(replacements)
				.map(replacement ->
						Optional.ofNullable(replacement)
								.map(item -> (StringReplacementFunction) ignore -> replacement)
								.orElse(null)
				)
				.toArray(StringReplacementFunction[]::new);
		return replaceGroup(first, str, regex, replacementFunctions);
	}
	
	/**
	 * 字符串替换 <br/>
	 *  1. 普通替换，与 String.replaceAll String.replaceFirst功能一致 <br/>
	 *  2. 替换正则表达式捕获组(captureGroup)匹配到的字符串(单个/多个) <br/>
	 * @param first        是否仅替换第一个符合的匹配项
	 * @param str          待替换的字符串
	 * @param regex        正则表达式
	 * @param replacementFunctions 需要替换的逻辑
	 * @return 替换后的字符串
	 */
	private static String replaceGroup(boolean first, String str, String regex,
									   StringReplacementFunction... replacementFunctions) {
		// 参数校验 不符合的直接返回str
		if (replacementFunctions == null || str == null || regex == null) { return str; }
		int replacementLength = replacementFunctions.length;
		if (replacementLength == 0) { return str; }
		Set<StringReplacementFunction> replacementSet = Stream.of(replacementFunctions).collect(toSet());
		if (replacementSet.size() == 1 && replacementSet.contains(null)) { return str; }
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		boolean match = m.find();
		if (!match) { return str; }
		
		// 游标 用于拼接使用
		int cursor = 0;
		StringBuilder sb = new StringBuilder();
		do {
			int groupCount = m.groupCount();
			// 如果groupCount==0 则使用m.group(0), replacements[0]
			// 如果groupCount>0  则使用m.group(1...groupCount), replacements[0....groupCount-1]
			int groupStart = groupCount == 0 ? 0 : 1;
			for (int i = groupStart; i <= groupCount; i++) {
				sb.append(str, cursor, m.start(i));
				cursor = m.end(i);
				int replacementsIndex = Math.max(i - 1, 0);
				String group = m.group(i);
				// 如果替换的内容为null 则保留原来的内容
				if (replacementLength >= i && replacementFunctions[replacementsIndex] != null) {
					StringReplacementFunction replacementFunction = replacementFunctions[replacementsIndex];
					String replacement = replacementFunction.apply(group);
					if (replacement == null) {
						sb.append(group);
					} else {
						sb.append(replacement);
					}
				} else {
					sb.append(group);
				}
			}
			if (first) { break; }
		} while (m.find());
		sb.append(str, cursor, str.length());
		
		return sb.toString();
	}
}
