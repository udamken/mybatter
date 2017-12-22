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

import static de.dknapps.mybatter.tokenizer.TokenType.ROOT;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a tag, word or argument in a MyBatis mapper xml.
 */
public class Token {

	static final String PREFIX_CHARACTER_DATA = "<![CDATA[";

	static final String SUFFIX_CHARACTER_DATA = "]]>";

	static final String PREFIX_DOCUMENT_DECLARATION = "<!"; // watch processing order!

	static final String PREFIX_PROCESSING_INSTRUCTION = "<?";

	static final String SUFFIX_PROCESSING_INSTRUCTION = "?>";

	static final String PREFIX_XML_TAG = "<"; // watch processing order!

	static final String SUFFIX_XML_TAG = ">";

	static final String PREFIX_XML_COMMENT = PREFIX_DOCUMENT_DECLARATION + "--"; // watch processing order!

	static final String SUFFIX_XML_COMMENT = "-->";

	static final String PREFIX_SINGLE_STRING = "\'";

	static final String SUFFIX_SINGLE_STRING = "\'";

	static final String PREFIX_DOUBLE_STRING = "\"";

	static final String SUFFIX_DOUBLE_STRING = "\"";

	static final String PREFIX_MYBATIS_VALUE_REFERENCE = "${";

	static final String PREFIX_MYBATIS_VARIABLE_REFERENCE = "#{";

	static final String SUFFIX_MYBATIS_REFERENCE = "}";

	static final String PREFIX_CLOSING_XML_TAG = "</"; // watch processing order!

	static final String SUFFIX_SELFCLOSING_XML_TAG = "/>";

	static final String PREFIX_SQL_COMMENT = "--";

	static final String VALUE_COMMA = ",";

	static final String VALUE_SPACE = " ";

	static final String VALUE_OPENING_PARENTHESIS = "(";

	static final String VALUE_CLOSING_PARENTHESIS = ")";

	/** List of children */
	@Getter
	private List<Token> tokenList = new ArrayList<>();

	/** String content parsed into this token */
	@Getter
	private String value;

	/** True if this tag shall end the collection of subtokens */
	@Getter
	@Setter
	private boolean terminating;

	/** Type of token based on its value (the parsed string) or {@value TokenType#ROOT} */
	@Getter
	@Setter
	private TokenType tokenType;

	/**
	 * Constructs a token.
	 */
	public Token() {
		value = "";
	}

	/**
	 * Append another character to the content.
	 * 
	 * @param c
	 *            The character to be appended.
	 */
	public void append(char c) {
		value += c;
	}

	/**
	 * Append several characters to the content.
	 * 
	 * @param s
	 *            The characters to be appended.
	 */
	public void append(String s) {
		value += s;
	}

	/**
	 * Add a token as subtoken and append its content to the content of this token.
	 * 
	 * @param t
	 *            The subtoken to be added.
	 */
	public void add(Token t) {
		tokenList.add(t);
		value += t.value;
	}

	/**
	 * Returns a string representation of this token and its children.
	 * 
	 * @return The string representation.
	 */
	public String toString() {
		return toString("");
	}

	/**
	 * Returns a string representation of this token and its children.
	 *
	 * @param indent
	 *            The string to be prepended to every line of output.
	 * @return The string representation.
	 */

	String toString(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("|").append((tokenType == ROOT) ? "..." : value).append("|")
				.append(tokenType);
		if (CollectionUtils.isNotEmpty(tokenList)) {
			for (Token subtoken : tokenList) {
				sb.append("\n").append(subtoken.toString(indent + "  "));
			}
		}
		return sb.toString();
	}

	/**
	 * Returns true if the content of this token is empty.
	 * 
	 * @return True if content is empty.
	 */
	public boolean isEmpty() {
		return value.isEmpty();
	}

	/**
	 * Returns true if the content is made of whitespaces.
	 * 
	 * @return True if content contains only whitespaces.
	 */
	public boolean isWhitespace() {
		return StringUtils.isWhitespace(value);
	}

	/**
	 * Returns true if content ends with the suffix.
	 * 
	 * @param suffix
	 *            The suffix.
	 * @return True if content ends with suffix.
	 */
	public boolean endsWith(String suffix) {
		return value.endsWith(suffix);
	}

	/**
	 * Returns true if content would be an xml tag suffix if ending were appended to the content.
	 * 
	 * @param ending
	 *            The ending assumed to be appended to the content.
	 * @return True if content plus ending ends with SUFFIX_PROCESSING_INSTRUCTION or
	 *         SUFFIX_SELFCLOSING_XML_TAG.
	 */
	public boolean wereXmlTagSuffix(String ending) {
		String potential = value + ending;
		return potential.endsWith(SUFFIX_PROCESSING_INSTRUCTION)
				|| potential.endsWith(SUFFIX_SELFCLOSING_XML_TAG);
	}

	/**
	 * Remove all whitespaces from the end of the content.
	 */
	public void stripEnd() {
		value = StringUtils.stripEnd(value, null);
	}

	/**
	 * Return the value of the first subtoken or the value of token itself as the name of the token.
	 * 
	 * @return The name of the token, e.g. mapper (XML) or select (SQL)
	 */
	public String tokenName() {
		String tokenName;
		if (CollectionUtils.isEmpty(tokenList)) {
			tokenName = value;
		} else {
			tokenName = tokenList.get(0).getValue();
		}
		return tokenName.toLowerCase();
	}

}