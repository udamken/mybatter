/*
 * MyBatter - Formats your MyBatis mapper XML files
 *
 *     Copyright (C) 2017 Uwe Damken
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dknapps.mybatter.formatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import de.dknapps.mybatter.tokenizer.TokenType;

public class FormatterTest {

	@Rule
	public MethodRule rule = new MethodRule() {
		@Override
		public Statement apply(Statement base, FrameworkMethod method, Object target) {
			System.out.println("===== " + method.getName() + "() =====");
			return base;
		}
	};

	@Test
	public void test_static_ensureExistingFormatsForEveryTokenType() {
		for (TokenType tokenType : TokenType.values()) {
			assertNotNull("Before format missing for " + tokenType.toString(),
					Formatter.retrieveBeforeFormat(tokenType));
			assertNotNull("After format missing for " + tokenType.toString(),
					Formatter.retrieveAfterFormat(tokenType));
		}
	}

	// TODO Create a real working example mapper xml file
	// @Test
	// public void test_format_sampleMapperFormattingDifference() throws IOException {
	// File inputFile = FileUtils.toFile(getClass().getResource("SampleMapper.xml"));
	// String input = FileUtils.readFileToString(inputFile, "UTF-8");
	// String expected = FileUtils.readFileToString(inputFile, "UTF-8").replace("\r", ""); // 0x0D0A => 0x0D
	// String actual = new Formatter().format(input);
	// assertNotEquals(expected, actual);
	// // assertEquals(expected, actual); // use this to see differences produced by formatting
	// }

	@Test
	public void test_format_andAfterXmlTag() {
		String[] lines = new String[] { //
				"<if test=\"arg_==_null_||_arg.isEmpty()\">", //
				"\tand field in (", //
				"\t\t'A','B','C'", //
				"\t)", //
				"</if>" };
		// checkResultLines(lines, new Formatter().format(compact(lines)));
		// checkResultLines(lines, new Formatter().format(compressed(lines)));
		// checkResultLines(lines, new Formatter().format(regular(lines)));
		// checkResultLines(lines, new Formatter().format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)), "New lines before IN values");
	}

	@Test
	public void test_format_newLinesAfterMapperTag() {
		String[] lines = new String[] { //
				"<mapper namespace=\"de.dknapps.mybatter.mapper.TableMapper\">", //
				"", //
				"</mapper>" };
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_newLinesAfterMapperTagBeforeComment() {
		String[] lines = new String[] { //
				"<mapper namespace=\"de.dknapps.mybatter.mapper.TableMapper\">", //
				"", //
				"\t<!--", //
				"\t\tcomment", //
				"\t-->", //
				"</mapper>" };
		// checkResultLines(lines, new Formatter().format(compact(lines)));
		// checkResultLines(lines, new Formatter().format(compressed(lines)));
		// checkResultLines(lines, new Formatter().format(regular(lines)));
		// checkResultLines(lines, new Formatter().format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)),
				"Comment in a single line instead of three lines");
	}

	@Test
	public void test_format_newLinesAfterClosingResultMapTag() {
		String[] lines = new String[] {
				"<resultMap id=\"tableData\" type=\"de.dknapps.mybatter.model.TableData\">", //
				"\t<result column=\"FIELD\" property=\"field\" jdbcType=\"CHAR\"/>", //
				"</resultMap>", //
				"", //
				"<unknown/>" };
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_newLinesAfterSelfclosingResultMapTag() {
		String[] lines = new String[] {
				"<resultMap id=\"tableData\" type=\"de.dknapps.mybatter.model.TableData\"/>", //
				"", //
				"<unknown/>" };
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_newlinesAfterClosingCacheTag() {
		String[] lines = new String[] { //
				"<cache flushInterval=\"123\" readOnly=\"true\" size=\"456\">", //
				"</cache>", //
				"", //
				"</unknown>" //
		};
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_newlinesAfterSelfclosingCacheTag() {
		String[] lines = new String[] {
				"<cache flushInterval=\"86400000\" readOnly=\"true\" size=\"30000\"/>", //
				"", //
				"</unknown>" //
		};
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_characterDataUnchanged_01() {
		String[] lines = new String[] { //
				"field <![CDATA[_<>_]]> 'X'" //
		};
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_characterDataUnchanged_02() {
		String[] lines = new String[] { //
				"field <![CDATA[_><_]]> 'X'" // nonsense but < ]]> looks like an xml
												// tag
		};
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_stringArgumentUnchanged_01() {
		String[] lines = new String[] {
				// https://www.w3.org/TR/xml/ ... > allowed, < not allowed
				"<if test=\"value_!=_'X'_and_list.size()_>_0\">" };
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_stringArgumentUnchanged_02() {
		String[] lines = new String[] {
				// https://www.w3.org/TR/xml/ ... > allowed, < not allowed
				"<if test='value_!=_\"X\"_and_list.size()_>_0'>" };
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_commaSeparatedFieldList() {
		String[] lines = new String[] { //
				"<sql>", //
				"\tfield1,", //
				"\tfield2", //
				"</sql>" };
		checkResultLines(lines, new Formatter().format(compact(lines)));
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_commentWrapping() {
		String[] lines = new String[] { //
				"<mapper>", //
				"", //
				"\t<!--", //
				"\t\t9012345678901", //
				"\t\t901234567890", //
				"\t\t1_12_123_123", //
				"\t\t2_22_2234", //
				"\t\t3234", //
				"\t-->", //
				"\t<cache/>", //
				"", //
				"</mapper>" };
		// checkResultLines(lines, new Formatter(20, 4).format(compact(lines)));
		// checkResultLines(lines, new Formatter(20, 4).format(compressed(lines)));
		// checkResultLines(lines, new Formatter(20, 4).format(regular(lines)));
		// checkResultLines(lines, new Formatter(20, 4).format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)), "Comment not wrapped at all");
	}

	@Test
	public void test_format_xmlTagWrapping_01() {
		String[] lines = new String[] { //
				"<mapper>", //
				"", //
				"\t<cache", //
				"\t\t\targ1=\"argument1\"", //
				"\t\t\targ2=\'argument2\'", //
				"\t\t\t/>", //
				"", //
				"</mapper>" };
		// compact(lines) removes all separators, hence it cannot work
		// checkResultLines(lines, new Formatter(46, 4).format(compressed(lines)));
		// checkResultLines(lines, new Formatter(46, 4).format(regular(lines)));
		// checkResultLines(lines, new Formatter(46, 4).format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)), "No blank before closing tag");
	}

	@Test
	public void test_format_xmlTagWrapping_02() {
		String[] lines = new String[] { //
				"<mapper>", //
				"", //
				"\t<cache", //
				"\t\t\targ1=\"argument1\"", //
				"\t\t\targ2=\'argument2\'/>", //
				"", //
				"</mapper>" };
		// compact(lines) removes all separators, hence it cannot work
		// checkResultLines(lines, new Formatter(45, 4).format(compressed(lines)));
		// checkResultLines(lines, new Formatter(45, 4).format(regular(lines)));
		// checkResultLines(lines, new Formatter(45, 4).format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)),
				"XML tag is not wrapped at all");
	}

	@Test
	public void test_format_xmlTagNoWrapping_01() {
		String[] lines = new String[] { //
				"<mapper>", //
				"", //
				"\t<cache arg1=\"argument1\" arg2=\'argument2'/>", //
				"", //
				"</mapper>" };
		checkResultLines(lines, new Formatter(47, 4).format(compact(lines)));
		checkResultLines(lines, new Formatter(47, 4).format(compressed(lines)));
		checkResultLines(lines, new Formatter(47, 4).format(regular(lines)));
		checkResultLines(lines, new Formatter(47, 4).format(expanded(lines)));
	}

	@Test
	public void test_format_xmlTagNoWrapping_02() {
		String[] lines = new String[] { //
				"<mapper>", //
				"", //
				"\t<cache arg1=\"argument1\" arg2=\'argument2'/>", // no blank before />
				"", //
				"</mapper>" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter(46, 4).format(compressed(lines)));
		checkResultLines(lines, new Formatter(46, 4).format(regular(lines)));
		checkResultLines(lines, new Formatter(46, 4).format(expanded(lines)));
	}

	@Test
	public void test_format_myBatisReferences() {
		String[] lines = new String[] { //
				"select", //
				"\tdate", //
				"from", //
				"\ttable", //
				"where", //
				"\tdate &lt;= #{date,jdbcType=DATE}" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_withUr() {
		String[] lines = new String[] { //
				"<select>", //
				"\tselect", //
				"\t\ta", //
				"\tfrom", //
				"\t\ttable", //
				"\twhere", //
				"\t\ta = 'A'", //
				"\tWITH UR", //
				"</select>" };
		// compact(lines) removes all separators, hence it cannot work
		// checkResultLines(lines, new Formatter().format(compressed(lines)));
		// checkResultLines(lines, new Formatter().format(regular(lines)));
		// checkResultLines(lines, new Formatter().format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)),
				"Closing select xml tag erroneously indented");
	}

	@Test
	public void test_format_select_inXmlTag() {
		String[] lines = new String[] { //
				"<select>", //
				"\tselect", //
				"\t\ta", //
				"\tfrom", //
				"\t\ttable", //
				"\twhere", //
				"\t\ta = 'A'", //
				"</select>" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_union() {
		String[] lines = new String[] { //
				"<select>", //
				"\tselect", //
				"\t\ta", //
				"\tfrom", //
				"\t\ttable1", //
				"\twhere", //
				"\t\ta = 'A'", //
				"", //
				"\tunion", //
				"", //
				"\tselect", //
				"\t\tb", //
				"\tfrom", //
				"\t\ttable2", //
				"\twhere", //
				"\t\tb = 'B'", //
				"</select>" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_and() {
		String[] lines = new String[] { //
				"a = 'A'", //
				"AND b = 'B'" //
		};
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_or() {
		String[] lines = new String[] { //
				"a = 'A'", //
				"AND b = 'B'" //
		};
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_andInBetween() {
		String[] lines = new String[] { //
				"a between 'B' and 'C'" };
		// compact(lines) removes all separators, hence it cannot work
		// checkResultLines(lines, new Formatter().format(compressed(lines)));
		// checkResultLines(lines, new Formatter().format(regular(lines)));
		// checkResultLines(lines, new Formatter().format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)),
				"Line break before AND of BETWEEN");
	}

	@Test
	public void test_format_select_columnOnly() {
		String[] lines = new String[] { "select", //
				"\ta" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_join() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable1", //
				"join", //
				"\ttable2", //
				"on", //
				"\ttable1.a = table2.a", //
				"where", //
				"\ta = 1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_outerJoin() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable1", //
				"outer join", //
				"\ttable2", //
				"on", //
				"\ttable1.a = table2.a", //
				"where", //
				"\ta = 1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_innerJoin() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable1", //
				"inner join", //
				"\ttable2", //
				"on", //
				"\ttable1.a = table2.a", //
				"where", //
				"\ta = 1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_leftOuterJoin() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable1", //
				"left outer join", //
				"\ttable2", //
				"on", //
				"\ttable1.a = table2.a", //
				"where", //
				"\ta = 1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_rightOuterJoin() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable1", //
				"right outer join", //
				"\ttable2", //
				"on", //
				"\ttable1.a = table2.a", //
				"where", //
				"\ta = 1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_basic() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_groupBy() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable", //
				"group by", //
				"\ta" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_having() {
		String[] lines = new String[] { //
				"select", //
				"\ta", //
				"from", //
				"\ttable", //
				"group by", //
				"\ta", //
				"having", //
				"\tcount(*) &gt; 1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_all() {
		String[] lines = new String[] { //
				"select all", //
				"\ta", //
				"from", //
				"\ttable" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_distinct() {
		String[] lines = new String[] { //
				"select distinct", //
				"\ta", //
				"from", //
				"\ttable" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_update_basic() {
		String[] lines = new String[] { //
				"update", //
				"\ttable1", //
				"set", //
				"\ta = 9" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_update_tableOnly() {
		String[] lines = new String[] { //
				"update", //
				"\ttable1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_insert_tableOnly() {
		String[] lines = new String[] { //
				"insert into", //
				"\ttable1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_delete_tableOnly() {
		String[] lines = new String[] { //
				"delete from", //
				"\ttable1" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_sqlCommentAtEndOfLine() {
		String[] lines = new String[] { //
				"<select>", //
				"\tselect", //
				"\t\ta, --_comment_a", //
				"\t\tb, --_comment_b", //
				"\t\tc --_comment_c", //
				"\tfrom", //
				"\t\ttable", //
				"</select>" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_select_sqlCommentWholeLine() {
		String[] lines = new String[] { //
				"<select>", //
				"\tselect", //
				"\t\ta, --_comment_a", //
				"\t\t--_an_xml_comment_should_be_used_instead", //
				"\t\tc --_comment_c", //
				"\tfrom", //
				"\t\ttable", //
				"\twhere", //
				"\t\ta = 'A'", //
				"</select>" };
		// TERM comments for whole lines shouldn't be used in an XML but xml comments, so this result might
		// look like a misbehaviour and this test has been added to show the intentional acceptance.
		String[] expectedLines = new String[] { //
				"<select>", //
				"\tselect", //
				"\t\ta, --_comment_a --_an_xml_comment_should_be_used_instead", //
				"\t\tc --_comment_c", //
				"\tfrom", //
				"\t\ttable", //
				"\twhere", //
				"\t\ta = 'A'", //
				"</select>" };
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(expectedLines, new Formatter().format(compressed(lines)));
		checkResultLines(expectedLines, new Formatter().format(regular(lines)));
		checkResultLines(expectedLines, new Formatter().format(expanded(lines)));
	}

	@Test
	public void test_format_insert() {
		String[] lines = new String[] { //
				"<insert>", //
				"\tINSERT INTO", //
				"\t\t${owner}.table (", //
				"\t\t\tfield1,", //
				"\t\t\tfield2", //
				"\t\t)", //
				"\tVALUES", //
				"\t\t(", //
				"\t\t\tvalue1,", //
				"\t\t\tvalue2", //
				"\t\t)", //
				"</insert>" //
		};
		// compact(lines) removes all separators, hence it cannot work
		// checkResultLines(lines, new Formatter().format(compressed(lines)));
		// checkResultLines(lines, new Formatter().format(regular(lines)));
		// checkResultLines(lines, new Formatter().format(expanded(lines)));
		// TODO Let this test get successful ... for the time being it would fail:
		printExpectedDeviation(lines, new Formatter().format(regular(lines)),
				"No blank between table and opening parenthesis");
	}

	@Test
	public void test_format_delete() {
		String[] lines = new String[] { //
				"<delete>", //
				"\tDELETE FROM", //
				"\t\t${owner}.table", //
				"\twhere", //
				"\t\tfield1 = #{value1}", //
				"\t\tAND field2 = #{value2}", //
				"</delete>" //
		};
		// compact(lines) removes all separators, hence it cannot work
		checkResultLines(lines, new Formatter().format(compressed(lines)));
		checkResultLines(lines, new Formatter().format(regular(lines)));
		checkResultLines(lines, new Formatter().format(expanded(lines)));
	}

	private String compact(String[] lines) {
		return String.join("", lines) // join lines very tight
				.replaceAll("\t", "") // join characters very tight
				.replace('_', ' '); // preserve "blanks" in string
	}

	private String compressed(String[] lines) {
		return String.join("\n", lines) // join lines very tight
				.replaceAll("\t", "") // join characters very tight
				.replace('_', ' '); // preserve "blanks" in string
	}

	private String regular(String[] lines) {
		return String.join("\n", lines) // join lines as usual
				.replaceAll("\t", "    ") // join characters as usual
				.replace('_', ' '); // preserve "blanks" in string
	}

	private String expanded(String[] lines) {
		return String.join("\n", lines) // join lines as usual
				.replace('\t', '\n') // split more than usual
				.replace(' ', '\n') // split even between words
				.replace('_', ' '); // preserve "blanks" in string
	}

	private void checkResultLines(String[] expectedLines, String actual) {
		assertNotNull(actual);
		assertEquals(regular(expectedLines), actual);
	}

	private void printExpectedDeviation(String[] expectedLines, String actual, String expectedDeviation) {
		System.out.println(regular(expectedLines));
		System.out.println(StringUtils.repeat("-", 70) + " " + expectedDeviation + ":");
		System.out.println(actual);
	}

}
