package io.jwt4j.lite.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * 문자열 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class StringUtil {
	/**
	 * 에러 메세지 형식에 맞게 변환
	 *
	 * @param code    에러 코드
	 * @param message 에러 메세지
	 * @return 에러 메세지 예) [code]message
	 */
	public static String toCodeString(Object code, Object message) {
		return String.format("[%s]%s", code, message);
	}

	/**
	 * 문자열을 스페이스로 결합
	 *
	 * @param values 문자열들
	 * @return 결합된 문자열
	 */
	public static String joiningBySpace(String... values) {
		return joining(" ", values);
	}

	/**
	 * 문자열을 구분자로 결합
	 *
	 * @param delimiter 구분자
	 * @param values    문자열들
	 * @return 결합된 문자열
	 */
	public static String joining(String delimiter, String... values) {
		return Arrays.stream(values)
				.filter(StringUtils::isNotEmpty)
				.collect(Collectors.joining(delimiter));
	}
}
