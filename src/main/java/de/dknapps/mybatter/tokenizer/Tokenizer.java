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

import static de.dknapps.mybatter.tokenizer.Token.PREFIX_CHARACTER_DATA;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_CLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_DOCUMENT_DECLARATION;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_DOUBLE_STRING;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_MYBATIS_VALUE_REFERENCE;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_MYBATIS_VARIABLE_REFERENCE;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_PROCESSING_INSTRUCTION;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_SINGLE_STRING;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_SQL_COMMENT;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_XML_COMMENT;
import static de.dknapps.mybatter.tokenizer.Token.PREFIX_XML_TAG;
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_CHARACTER_DATA;
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_DOUBLE_STRING;
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_MYBATIS_REFERENCE;
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_SINGLE_STRING;
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_XML_COMMENT;
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_XML_TAG;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_COMMA;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_DOT;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_SPACE;
import static de.dknapps.mybatter.tokenizer.TokenizerAction.CONSUME;
import static de.dknapps.mybatter.tokenizer.TokenizerAction.CONSUME_AND_RECURSE_AND_RETURN_TOKEN;
import static de.dknapps.mybatter.tokenizer.TokenizerAction.CONSUME_AND_RETURN_TOKEN;
import static de.dknapps.mybatter.tokenizer.TokenizerAction.RETURN_PREVIOUS_TOKEN;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Parses a MyBatis mapper input string into {@link Token} objects. The input string does not need to be
 * well-formed or valid. However, if not well-formed and valid results might be surprising.
 */
public class Tokenizer {

	/** Input string to be parsed into {@link Token} objects */
	private String input;

	/** Index of the character in input to be analysed next */
	private int head;

	/** String to be consumed from the input */
	private String consume;

	/** Top level token that will contain all found tokens as children */
	private Token rootToken;

	/** True if parsing process just analyses an xml tag */
	private boolean inXmlTag;

	/** True if parsing process just analyses an xml comment */
	private boolean inXmlComment;

	/** True if parsing process just analyses a string in double quotes */
	private boolean inDoubleString;

	/** True if parsing process just analyses a string in single quotes */
	private boolean inSingleString;

	/** True if parsing process just analyses a MyBatis value or variable reference */
	private boolean inMyBatisReference;

	/** True if parsing process just analyses xml character data */
	private boolean inCharacterData;

	/** True if parsing process just analyses an sql comment */
	private boolean inSqlComment;

	/**
	 * Constructs a Tokenizer with the given input string. Use {@link #tokenize()} afterwards to start
	 * tokenizing.
	 * 
	 * @param input
	 *            String containing MyBatis mapper xml file contents.
	 */
	public Tokenizer(String input) {
		this.head = 0;
		this.input = input;
		this.rootToken = new Token();
	}

	/**
	 * Parse input string into {@link Token} objects.
	 */
	public void tokenize() {
		addSubtokens(rootToken, false);
		TokenTypeDeterminer.determineTokenTypes(rootToken);
	}

	/**
	 * Returns the result of the tokenizing (the {@link Token} objects). Use {@link #tokenize()} before to
	 * start tokenizing.
	 * 
	 * @return The list of {@link Token} objects.
	 */
	public List<Token> getTokenList() {
		return rootToken.getTokenList();
	}

	/**
	 * Return a string representation of the tokenizing results (the {@link Token} objects). The output is
	 * primarily useful for JUnit tests.
	 * 
	 * @return The string representation.
	 */
	public String toString() {
		return rootToken.toString("");
	}

	/**
	 * Adds inner tokens to the given token.
	 * 
	 * @param token
	 *            Token to receive the subtokens.
	 * @param stopOnTerminatingToken
	 *            If true, addings subtokens stops when a subtokens returns true for isTerminating().
	 */
	private void addSubtokens(Token token, boolean stopOnTerminatingToken) {
		boolean done = false;
		while (!done) {
			Token subtoken = nextToken();
			if (subtoken.isEmpty()) {
				done = true;
			} else if (stopOnTerminatingToken && subtoken.isTerminating()) {
				done = true;
				if (!subtoken.endsWith(SUFFIX_XML_COMMENT)) {
					token.stripEnd(); // remove trailing whitespaces
				}
				token.append(subtoken.getValue()); // terminating token is not a subtoken
			} else if (subtoken.isWhitespace()) {
				if (!token.endsWith(VALUE_SPACE)) {
					token.append(VALUE_SPACE); // reduce multiple spaces to one
				}
			} else {
				token.add(subtoken);
			}
		}
	}

	/**
	 * Retrieve next token from the input string. Type of the token is basically derived from its value.
	 * 
	 * @return The next token on the input string.
	 */
	private Token nextToken() {
		Token token = new Token();
		while (head < input.length()) {
			consume = input.substring(head, head + 1);
			switch (deriveAction(token)) {
			case CONSUME:
				consume(token);
				break;
			case CONSUME_AND_RECURSE_AND_RETURN_TOKEN:
				consume(token);
				addSubtokens(token, true);
				return token;
			case CONSUME_AND_RETURN_TOKEN:
				consume(token);
				return token;
			case RETURN_PREVIOUS_TOKEN:
				return token;
			}
		}
		return token;
	}

	/**
	 * Append characters to be "consumed" to the token ({@link #consume}) and move character position
	 * ({@link #head} forward.
	 * 
	 * @param token
	 *            The token that receives the consumed characters.
	 */
	private void consume(Token token) {
		token.append(consume); // collect char or string in current token
		head += consume.length(); // move forward to char after consumed char or string
	}

	/**
	 * Decide what to do with the character(s) at the current character position.
	 * 
	 * @param token
	 *            The token that characters are currently collected to.
	 * @return The action to take after this decision.
	 */
	private TokenizerAction deriveAction(Token token) {
		if (inCharacterData) {
			if (upcomingStartsWith(SUFFIX_CHARACTER_DATA)) {
				inCharacterData = false;
				consume = SUFFIX_CHARACTER_DATA;
				return CONSUME_AND_RETURN_TOKEN;
			}
		} else if (inDoubleString) {
			if (upcomingStartsWith(SUFFIX_DOUBLE_STRING)) {
				inDoubleString = false;
				return CONSUME_AND_RETURN_TOKEN;
			}
		} else if (inSingleString) {
			if (upcomingStartsWith(SUFFIX_SINGLE_STRING)) {
				inSingleString = false;
				return CONSUME_AND_RETURN_TOKEN;
			}
		} else if (inMyBatisReference) {
			if (upcomingStartsWith(SUFFIX_MYBATIS_REFERENCE)) {
				inMyBatisReference = false;
				return CONSUME_AND_RETURN_TOKEN;
			}
		} else if (inSqlComment) {
			if (upcomingStartsWithLinebreak()) {
				inSqlComment = false;
				return RETURN_PREVIOUS_TOKEN;
			}
		} else if (upcomingStartsWith(PREFIX_DOUBLE_STRING)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			inDoubleString = true;
		} else if (upcomingStartsWith(PREFIX_SINGLE_STRING)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			inSingleString = true;
		} else if (upcomingStartsWith(PREFIX_MYBATIS_VALUE_REFERENCE)
				|| upcomingStartsWith(PREFIX_MYBATIS_VARIABLE_REFERENCE)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			inMyBatisReference = true;
		} else if (inXmlComment) {
			if (upcomingStartsWith(SUFFIX_XML_COMMENT)) {
				if (!token.isEmpty()) {
					return RETURN_PREVIOUS_TOKEN;
				}
				token.setTerminating(true);
				inXmlComment = false;
				consume = SUFFIX_XML_COMMENT;
				return CONSUME_AND_RETURN_TOKEN;
			} else if (StringUtils.isWhitespace(consume)) {
				if (!token.isEmpty()) {
					return RETURN_PREVIOUS_TOKEN;
				}
				return CONSUME_AND_RETURN_TOKEN;
			}
		} else if (inXmlTag) {
			if (upcomingStartsWith(SUFFIX_XML_TAG)) {
				if (!token.isEmpty() && !token.wereXmlTagSuffix(SUFFIX_XML_TAG)) {
					return RETURN_PREVIOUS_TOKEN;
				}
				token.setTerminating(true);
				inXmlTag = false;
				return CONSUME_AND_RETURN_TOKEN;
			} else if (StringUtils.isWhitespace(consume)) {
				if (!token.isEmpty()) {
					return RETURN_PREVIOUS_TOKEN;
				}
				return CONSUME_AND_RETURN_TOKEN;
			}
		} else if (upcomingStartsWith(PREFIX_XML_TAG)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			if (upcomingStartsWith(PREFIX_XML_COMMENT)) { // watch processing order!
				inXmlComment = true;
				consume = PREFIX_XML_COMMENT;
			} else if (upcomingStartsWith(PREFIX_CLOSING_XML_TAG)) {
				inXmlTag = true;
				consume = PREFIX_CLOSING_XML_TAG;
			} else if (upcomingStartsWith(PREFIX_PROCESSING_INSTRUCTION)) {
				inXmlTag = true;
				consume = PREFIX_PROCESSING_INSTRUCTION;
			} else if (upcomingStartsWith(PREFIX_CHARACTER_DATA)) {
				inCharacterData = true;
				consume = PREFIX_CHARACTER_DATA;
				return CONSUME;
			} else if (upcomingStartsWith(PREFIX_DOCUMENT_DECLARATION)) { // watch processing order!
				inXmlTag = true;
				consume = PREFIX_DOCUMENT_DECLARATION;
			} else {
				inXmlTag = true;
			}
			return CONSUME_AND_RECURSE_AND_RETURN_TOKEN;
		} else if (upcomingStartsWith(VALUE_COMMA)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			return CONSUME_AND_RETURN_TOKEN;
		} else if (upcomingStartsWith(VALUE_DOT)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			return CONSUME_AND_RETURN_TOKEN;
		} else if (upcomingStartsWith(PREFIX_SQL_COMMENT)) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			inSqlComment = true;
		} else if (upcomingStartsWithWhitespace()) {
			if (!token.isEmpty()) {
				return RETURN_PREVIOUS_TOKEN;
			}
			return CONSUME_AND_RETURN_TOKEN;
		}
		return CONSUME;
	}

	/**
	 * Check if the character(s) at the current character position are that of prefix.
	 * 
	 * @param prefix
	 *            The character to check the input for.
	 * @return True if the input at the character position begins with prefix.
	 */
	boolean upcomingStartsWith(String prefix) {
		for (int i = 0; i < prefix.length(); i++) {
			int peek = head + i;
			if (peek >= input.length()) {
				return false;
			}
			if (input.charAt(peek) != prefix.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the character at the current character position is a linebreak character. Neither StringUtils
	 * nor Character contains an isLinebreak() method. The solution used is that described herein:
	 * https://stackoverflow.com/a/25915115
	 * 
	 * @return True if the input at the character position is a linebreak character.
	 */
	boolean upcomingStartsWithLinebreak() {
		char characterAtHead = input.charAt(head);
		return (!String.valueOf(characterAtHead).matches("."));
	}

	/**
	 * Check if the character at the current character position is a whitespace character.
	 * 
	 * @return True if the input at the character position is a whitespace character.
	 */
	boolean upcomingStartsWithWhitespace() {
		char characterAtHead = input.charAt(head);
		return Character.isWhitespace(characterAtHead);
	}

}