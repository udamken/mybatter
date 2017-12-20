/*
 * MyBatter - Formats your MyBatis mapper xml files
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
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.COMMA;
import static de.dknapps.mybatter.tokenizer.TokenType.DOCUMENT_DECLARATION;
import static de.dknapps.mybatter.tokenizer.TokenType.MYBATIS_REFERENCE;
import static de.dknapps.mybatter.tokenizer.TokenType.OPENING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.PROCESSING_INSTRUCTION;
import static de.dknapps.mybatter.tokenizer.TokenType.SELFCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.STRING;
import static de.dknapps.mybatter.tokenizer.TokenType.TERM;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_TAG;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TokenTypeDeterminerTest {

	@Test
	public void test_deriveTokenTypeFromValue_characterData() {
		assertTokenType("<![CDATA[character_data]]>", CHARACTER_DATA);
		assertTokenType("<![CDATA[character_data", CHARACTER_DATA);
		assertTokenType("<![CDATA[", CHARACTER_DATA);
	}

	@Test
	public void test_deriveTokenTypeFromValue_documentDeclaration() {
		assertTokenType("<!document_declaration>", DOCUMENT_DECLARATION);
		assertTokenType("<!document_declaration", DOCUMENT_DECLARATION);
		assertTokenType("<![CDATA", DOCUMENT_DECLARATION); // not CHARACTER_DATA
		assertTokenType("<![CDATA/>", DOCUMENT_DECLARATION); // not SELFCLOSING_XML_TAG
		assertTokenType("<!-", DOCUMENT_DECLARATION);
		assertTokenType("<!-/>", DOCUMENT_DECLARATION); // not SELFCLOSING_XML_TAG
		assertTokenType("<!", DOCUMENT_DECLARATION);
	}

	@Test
	public void test_deriveTokenTypeFromValue_processingInstruction() {
		assertTokenType("<?processing_instruction?>", PROCESSING_INSTRUCTION);
		assertTokenType("<?processing_instruction", PROCESSING_INSTRUCTION);
		assertTokenType("<?", PROCESSING_INSTRUCTION);
	}

	@Test
	public void test_deriveTokenTypeFromValue_xmlTag() {
		assertTokenType("<xml_tag>", XML_TAG);
		assertTokenType("<xml_tag", XML_TAG);
		assertTokenType("<", XML_TAG);
	}

	@Test
	public void test_deriveTokenTypeFromValue_closingXmlTag() {
		assertTokenType("</closing_xml_tag>", CLOSING_XML_TAG);
		assertTokenType("</closing_xml_tag", CLOSING_XML_TAG);
		assertTokenType("</>", CLOSING_XML_TAG); // not SELFCLOSING_XML_TAG
		assertTokenType("</", CLOSING_XML_TAG);
	}

	@Test
	public void test_deriveTokenTypeFromValue_xmlComment() {
		assertTokenType("<!--xml_comment-->", XML_COMMENT);
		assertTokenType("<!--xml_comment", XML_COMMENT);
		assertTokenType("<!--", XML_COMMENT);
	}

	@Test
	public void test_deriveTokenTypeFromValue_string() {
		assertTokenType("\"double_string\"", STRING);
		assertTokenType("'single_string'", STRING);
	}

	@Test
	public void test_deriveTokenTypeFromValue_selfclosingXmlTag() {
		assertTokenType("<selfclosing_xml_tag/>", SELFCLOSING_XML_TAG);
	}

	@Test
	public void test_deriveTokenTypeFromValue_sqlComment() {
		assertTokenType("--sql_comment", SQL_COMMENT);
		assertTokenType("--", SQL_COMMENT);
	}

	@Test
	public void test_deriveTokenTypeFromValue_sqlValueDelimiter() {
		assertTokenType(",", COMMA);
	}

	@Test
	public void test_deriveTokenTypeFromValue_openingParenthesis() {
		assertTokenType("(", OPENING_PARENTHESIS);
	}

	@Test
	public void test_deriveTokenTypeFromValue_closingParenthesis() {
		assertTokenType(")", CLOSING_PARENTHESIS);
	}

	@Test
	public void test_deriveTokenTypeFromValue_word() {
		assertTokenType("x)", TERM);
		assertTokenType(")x", TERM);
		assertTokenType("x(", TERM);
		assertTokenType("(x", TERM);
		assertTokenType("x,", TERM);
		assertTokenType(",x", TERM);
		assertTokenType("select", TokenType.SQL_STATEMENT);
		assertTokenType("and", TERM);
		assertTokenType("gobbledegook", TERM);
		assertTokenType("${variable}", MYBATIS_REFERENCE);
		assertTokenType("#{variable}", MYBATIS_REFERENCE);
		assertTokenType("@class_reference@", TERM);
	}

	private void assertTokenType(String value, TokenType tokenType) {
		Token token = new Token();
		token.append(value);
		new TokenTypeDeterminer().determineTokenTypeFromValue(token);
		assertEquals(tokenType, token.getTokenType());
	}

}
