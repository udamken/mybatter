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
package de.dknapps.mybatter.tokenizer;

import static de.dknapps.mybatter.tokenizer.TokenType.CHARACTER_DATA;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_ENCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_PRIMARY_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.COMMA;
import static de.dknapps.mybatter.tokenizer.TokenType.DOCUMENT_DECLARATION;
import static de.dknapps.mybatter.tokenizer.TokenType.DOT;
import static de.dknapps.mybatter.tokenizer.TokenType.ENCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.MYBATIS_REFERENCE;
import static de.dknapps.mybatter.tokenizer.TokenType.PROCESSING_INSTRUCTION;
import static de.dknapps.mybatter.tokenizer.TokenType.ROOT;
import static de.dknapps.mybatter.tokenizer.TokenType.SELFCLOSING_ENCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_DYADIC_OPERATOR;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_STATEMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_SUB_STATEMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_SUB_STATEMENT_SUFFIX;
import static de.dknapps.mybatter.tokenizer.TokenType.STRING;
import static de.dknapps.mybatter.tokenizer.TokenType.TERM;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_TAG;

import org.junit.Test;

import junit.framework.TestCase;

public class TokenizerTest extends TestCase {

	@Test
	public void test_fullStatement_01() {
		String input = "<mapper>select a, b from table1 join table2 on table1.a = table2.a and table1.b = table2.b where table1.a = 'X' and table1.b = 1 order by table1.a with ur</mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"  |select|" + SQL_STATEMENT, //
				"  |a|" + TERM, //
				"  |,|" + COMMA, //
				"  |b|" + TERM, //
				"  |from|" + SQL_SUB_STATEMENT, //
				"  |table1|" + TERM, //
				"  |join|" + SQL_SUB_STATEMENT, //
				"  |table2|" + TERM, //
				"  |on|" + SQL_SUB_STATEMENT, //
				"  |table1|" + TERM, //
				"  |.|" + DOT, //
				"  |a|" + TERM, //
				"  |=|" + TERM, //
				"  |table2|" + TERM, //
				"  |.|" + DOT, //
				"  |a|" + TERM, //
				"  |and|" + SQL_DYADIC_OPERATOR, //
				"  |table1|" + TERM, //
				"  |.|" + DOT, //
				"  |b|" + TERM, //
				"  |=|" + TERM, //
				"  |table2|" + TERM, //
				"  |.|" + DOT, //
				"  |b|" + TERM, //
				"  |where|" + SQL_SUB_STATEMENT, //
				"  |table1|" + TERM, //
				"  |.|" + DOT, //
				"  |a|" + TERM, //
				"  |=|" + TERM, //
				"  |'X'|" + STRING, //
				"  |and|" + SQL_DYADIC_OPERATOR, //
				"  |table1|" + TERM, //
				"  |.|" + DOT, //
				"  |b|" + TERM, //
				"  |=|" + TERM, //
				"  |1|" + TERM, //
				"  |order|" + SQL_SUB_STATEMENT, //
				"  |by|" + SQL_SUB_STATEMENT_SUFFIX, //
				"  |table1|" + TERM, //
				"  |.|" + DOT, //
				"  |a|" + TERM, //
				"  |with|" + SQL_SUB_STATEMENT, //
				"  |ur|" + SQL_SUB_STATEMENT_SUFFIX, //
				"  |</mapper>|" + CLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_processingInstruction() {
		String input = "<?xml version=\"1.0\"?>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<?xml version=\"1.0\"?>|" + PROCESSING_INSTRUCTION, //
				"    |xml|" + TERM, //
				"    |version=|" + TERM, //
				"    |\"1.0\"|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_documentDeclaration() {
		String input = "<!DOCTYPE greeting SYSTEM \"hello.dtd\">";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<!DOCTYPE greeting SYSTEM \"hello.dtd\">|" + DOCUMENT_DECLARATION, //
				"    |DOCTYPE|" + TERM, //
				"    |greeting|" + TERM, //
				"    |SYSTEM|" + TERM, //
				"    |\"hello.dtd\"|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_characterData_01() {
		String input = "<mapper><![CDATA[ < ]]></mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"  |<![CDATA[ < ]]>|" + CHARACTER_DATA, //
				"  |</mapper>|" + CLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_characterData_02() {
		String input = "<mapper><![CDATA[ > ]]></mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"  |<![CDATA[ > ]]>|" + CHARACTER_DATA, //
				"  |</mapper>|" + CLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_characterData_03() {
		String input = "<mapper><![CDATA[<>]]></mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"  |<![CDATA[<>]]>|" + CHARACTER_DATA, //
				"  |</mapper>|" + CLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_myBatisValueReference_01() {
		String input = "${a}";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |${a}|" + MYBATIS_REFERENCE //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_myBatisValueReference_02() {
		String input = "${a,jdbcType=type}";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |${a,jdbcType=type}|" + MYBATIS_REFERENCE //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_myBatisVariableReference_01() {
		String input = "#{a}";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |#{a}|" + MYBATIS_REFERENCE //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_myBatisVariableReference_02() {
		String input = "#{a,jdbcType=type}";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |#{a,jdbcType=type}|" + MYBATIS_REFERENCE //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_sql_01() {
		String input = "<mapper>select * from table1 <where> a &gt; 5</where></mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"  |select|" + SQL_STATEMENT, //
				"  |*|" + TERM, //
				"  |from|" + SQL_SUB_STATEMENT, //
				"  |table1|" + TERM, //
				"  |<where>|" + XML_TAG, //
				"    |where|" + SQL_SUB_STATEMENT, //
				"  |a|" + TERM, //
				"  |&gt;|" + TERM, //
				"  |5|" + TERM, //
				"  |</where>|" + CLOSING_XML_TAG, //
				"    |where|" + SQL_SUB_STATEMENT, //
				"  |</mapper>|" + CLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_openingXmlTagWithOneArgument_01() {
		String input = "<mapper arg1=\"'test  >  4711'>\">";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper arg1=\"'test  >  4711'>\">|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"    |arg1=|" + TERM, //
				"    |\"'test  >  4711'>\"|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_openingXmlTagWithOneArgument_02() {
		String input = "<mapper arg2='X'>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper arg2='X'>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"    |arg2=|" + TERM, //
				"    |'X'|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_openingXmlTagWithTwoArguments() {
		String input = "<mapper arg1=\"'test  >  4711'>\" arg2='X'>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper arg1=\"'test  >  4711'>\" arg2='X'>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"    |arg1=|" + TERM, //
				"    |\"'test  >  4711'>\"|" + STRING, //
				"    |arg2=|" + TERM, //
				"    |'X'|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_openingXmlTagWithoutArgument() {
		String input = "<mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_selfclosingXmlTagWithTwoArguments() {
		String input = "<mapper arg1=\"'test  >  4711'>\" arg2='X'/>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper arg1=\"'test  >  4711'>\" arg2='X'/>|" + SELFCLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"    |arg1=|" + TERM, //
				"    |\"'test  >  4711'>\"|" + STRING, //
				"    |arg2=|" + TERM, //
				"    |'X'|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_closingXmlTag() {
		String input = "</mapper>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |</mapper>|" + CLOSING_ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_xmlComment_01() {
		String input = "<!-- xx -->";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<!-- xx -->|" + XML_COMMENT, //
				"    |xx|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_xmlComment_02() {
		String input = "<!--xx-->";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<!--xx-->|" + XML_COMMENT, //
				"    |xx|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_xmlComment_03() {
		String input = "something<!--</mapper>-->otherthing";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |something|" + TERM, //
				"  |<!--</mapper>-->|" + XML_COMMENT, //
				"    |</mapper>|" + CLOSING_XML_TAG, //
				"  |otherthing|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_xmlComment_04() {
		String input = "<!-- \"ignore\" -->";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<!-- \"ignore\" -->|" + XML_COMMENT, //
				"    |\"ignore\"|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_sqlComment_01() {
		String input = "AND A = B -- OR C = D";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |AND|" + SQL_DYADIC_OPERATOR, //
				"  |A|" + TERM, //
				"  |=|" + TERM, //
				"  |B|" + TERM, //
				"  |-- OR C = D|" + SQL_COMMENT //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_sqlComment_02() {
		String input = "AND A = B -- OR C = D\nAND C = D";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |AND|" + SQL_DYADIC_OPERATOR, //
				"  |A|" + TERM, //
				"  |=|" + TERM, //
				"  |B|" + TERM, //
				"  |-- OR C = D|" + SQL_COMMENT, //
				"  |AND|" + SQL_DYADIC_OPERATOR, //
				"  |C|" + TERM, //
				"  |=|" + TERM, //
				"  |D|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_sqlComment_03() {
		String input = "AND A = B\n-- OR C = D\nAND C = D";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |AND|" + SQL_DYADIC_OPERATOR, //
				"  |A|" + TERM, //
				"  |=|" + TERM, //
				"  |B|" + TERM, //
				"  |-- OR C = D|" + SQL_COMMENT, //
				"  |AND|" + SQL_DYADIC_OPERATOR, //
				"  |C|" + TERM, //
				"  |=|" + TERM, //
				"  |D|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_whitespacesInSql() {
		String input1 = "select a, b";
		String input2 = "select a,b";
		String input3 = "select a ,b";
		String input4 = " select  a  ,  b";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |select|" + SQL_STATEMENT, //
				"  |a|" + TERM, //
				"  |,|" + COMMA, //
				"  |b|" + TERM, //
		};
		doAssertEquals(output, input1);
		doAssertEquals(output, input2);
		doAssertEquals(output, input3);
		doAssertEquals(output, input4);
	}

	@Test
	public void test_whitespacesInOpeningXmlTag() {
		String input1 = "<mapper>";
		String input2 = "<mapper >";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input1);
		doAssertEquals(output, input2);
	}

	@Test
	public void test_whitespacesInOpeningXmlTagInvalid() {
		String input1 = "< mapper>";
		String input2 = "< mapper >";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |< mapper>|" + ENCLOSING_XML_TAG, // leave invalid leading blank at it's place
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input1);
		doAssertEquals(output, input2);
	}

	@Test
	public void test_danglingDoubleString() {
		String input = "<mapper arg=\"string>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper arg=\"string>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"    |arg=|" + TERM, //
				"    |\"string>|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_danglingSingleString() {
		String input = "<mapper arg='string>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper arg='string>|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM, //
				"    |arg=|" + TERM, //
				"    |'string>|" + STRING //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_danglingXmlTag() {
		String input = "<mapper";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<mapper|" + ENCLOSING_XML_TAG, //
				"    |mapper|" + TERM //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_danglingCharacterData() {
		String input = "<![CDATA[</select>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |<![CDATA[</select>|" + CHARACTER_DATA //
		};
		doAssertEquals(output, input);
	}

	@Test
	public void test_danglingEscapedCharacter() {
		String input = "&lt 5</select>";
		String output[] = new String[] { //
				"|...|" + ROOT, //
				"  |&lt|" + TERM, //
				"  |5|" + TERM, //
				"  |</select>|" + CLOSING_PRIMARY_XML_TAG, //
				"    |select|" + SQL_STATEMENT //
		};
		doAssertEquals(output, input);
	}

	/**
	 * Helper method to check tokenizing results.
	 * 
	 * @param output
	 *            The expected output of the tokenizing process.
	 * @param input
	 *            The MyBatis mapper xml to be tokenized.
	 */
	private void doAssertEquals(String[] output, String input) {
		Tokenizer tokenizer = new Tokenizer(input);
		tokenizer.tokenize();
		assertEquals(String.join("\n", output), tokenizer.toString());
	}

	@Test
	public void test_constructor() {
		assertEquals("||" + null, new Tokenizer(null).toString());
	}

	@Test
	public void test_upcomingStartsWith_returnsTrue() {
		assertTrue(new Tokenizer("abc").upcomingStartsWith(""));
		assertTrue(new Tokenizer("abc").upcomingStartsWith("a"));
		assertTrue(new Tokenizer("abc").upcomingStartsWith("ab"));
		assertTrue(new Tokenizer("abc").upcomingStartsWith("abc"));
	}

	@Test
	public void test_upcomingStartsWith_returnsFalse() {
		assertFalse(new Tokenizer("abc").upcomingStartsWith("x"));
		assertFalse(new Tokenizer("abc").upcomingStartsWith("ax"));
		assertFalse(new Tokenizer("abc").upcomingStartsWith("abx"));
		assertFalse(new Tokenizer("abc").upcomingStartsWith("abcx"));
	}

}
