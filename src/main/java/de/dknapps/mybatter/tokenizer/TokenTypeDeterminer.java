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
import static de.dknapps.mybatter.tokenizer.Token.SUFFIX_SELFCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_CLOSING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_COMMA;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_DOT;
import static de.dknapps.mybatter.tokenizer.Token.VALUE_OPENING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.CHARACTER_DATA;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.COMMA;
import static de.dknapps.mybatter.tokenizer.TokenType.DOCUMENT_DECLARATION;
import static de.dknapps.mybatter.tokenizer.TokenType.DOT;
import static de.dknapps.mybatter.tokenizer.TokenType.MYBATIS_REFERENCE;
import static de.dknapps.mybatter.tokenizer.TokenType.OPENING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.PROCESSING_INSTRUCTION;
import static de.dknapps.mybatter.tokenizer.TokenType.ROOT;
import static de.dknapps.mybatter.tokenizer.TokenType.SELFCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_AND_IN_BETWEEN;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_STATEMENT_SUFFIX;
import static de.dknapps.mybatter.tokenizer.TokenType.STRING;
import static de.dknapps.mybatter.tokenizer.TokenType.TERM;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_TAG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Determines the type of a token.
 */
public class TokenTypeDeterminer {

	/**
	 * Resolves the ambiguity of a token type depending on the context of the token.
	 */
	@FunctionalInterface
	interface AmbiguousTokenTypeResolver {

		/**
		 * Sets the type of the token depending on its context.
		 * 
		 * @param token
		 *            The token.
		 * @param beforeTokenList
		 *            Left context of the token.
		 * @param afterTokenList
		 *            Right context of the token.
		 */
		void resolve(Token token, List<Token> beforeTokenList, List<Token> afterTokenList);

	}

	/** Map of instances of {@link AmbiguousTokenTypeResolver} per token name */
	static final Map<String, AmbiguousTokenTypeResolver> RESOLVER_MAP = new HashMap<>();

	/**
	 * Sets up static final maps.
	 */
	static {
		RESOLVER_MAP.put("and", new AmbiguousTokenTypeResolver() {

			@Override
			public void resolve(Token token, List<Token> beforeTokenList, List<Token> afterTokenList) {
				for (int i = beforeTokenList.size() - 1; i >= 0; i--) {
					Token previousToken = beforeTokenList.get(i);
					switch (previousToken.getTokenType()) {
					case TERM:
						if (previousToken.tokenName().equals("between")) {
							token.setTokenType(SQL_AND_IN_BETWEEN);
							return; // between operator found
						}
						// intentionally falling through
					case SQL_SUB_STATEMENT:
					case SQL_STATEMENT:
					case PRIMARY_XML_TAG:
						return; // stop looking for between operator
					default:
						// go backwards to next token
					}
				}
			}

		});
		RESOLVER_MAP.put("from", new AmbiguousTokenTypeResolver() {

			@Override
			public void resolve(Token token, List<Token> beforeTokenList, List<Token> afterTokenList) {
				for (int i = beforeTokenList.size() - 1; i >= 0; i--) {
					Token previousToken = beforeTokenList.get(i);
					switch (previousToken.getTokenType()) {
					case SQL_STATEMENT:
						if (previousToken.tokenName().equals("delete")) {
							token.setTokenType(SQL_STATEMENT_SUFFIX);
							return; // delete statement found
						}
						// intentionally falling through
					case PRIMARY_XML_TAG:
						return; // stop looking for delete statement
					default:
						// go backwards to next token
					}
				}
			}

		});
	}

	/**
	 * Determines the type of the root token and its subtokens.
	 * 
	 * @param rootToken
	 *            The root token.
	 */
	public static void determineTokenTypes(Token rootToken) {
		TokenTypeDeterminer tokenTypeDeterminer = new TokenTypeDeterminer();

		// At first determine token type from value only
		tokenTypeDeterminer.determineTokenTypeFromValueRecursively(rootToken.getTokenList());

		// Then resolve ambiguous token types from context
		tokenTypeDeterminer.determineTokenTypeFromContext(rootToken.getTokenList());

		// The root token is always of type ROOT
		rootToken.setTokenType(ROOT);
	}

	/**
	 * Constructs a token type determiner.
	 */
	TokenTypeDeterminer() {
	}

	/**
	 * Determines the type of the tokens in the list and of their subtokens based on their parsed strings.
	 * 
	 * @param tokenList
	 *            The list of tokens.
	 */
	private void determineTokenTypeFromValueRecursively(List<Token> tokenList) {
		if (CollectionUtils.isNotEmpty(tokenList)) {
			for (Token token : tokenList) {
				determineTokenTypeFromValue(token);
				determineTokenTypeFromValueRecursively(token.getTokenList());
			}
		}
	}

	/**
	 * Determines the type of the token based on the parsed string.
	 */
	void determineTokenTypeFromValue(Token token) {

		// Determine token type without checking the value of the token
		determineTokenTypeFromValueBasically(token);

		// Refine token type by checking the value of the token
		String tokenName = token.tokenName();
		for (TokenType potentialTokenType : TokenType.values()) {
			if (potentialTokenType.getParentTokenType() == token.getTokenType()
					&& potentialTokenType.getTokenNameList().contains(tokenName)) {
				token.setTokenType(potentialTokenType);
			}
		}
	}

	/**
	 * Determines the token type of the token based on the parsed string without checking the value of the
	 * token.
	 */
	private void determineTokenTypeFromValueBasically(Token token) {
		String value = token.getValue();
		TokenType tokenType;
		if (value.startsWith(PREFIX_CHARACTER_DATA)) {
			tokenType = CHARACTER_DATA;
		} else if (value.startsWith(PREFIX_XML_COMMENT)) {
			tokenType = XML_COMMENT;
		} else if (value.startsWith(PREFIX_DOCUMENT_DECLARATION)) {
			tokenType = DOCUMENT_DECLARATION;
		} else if (value.startsWith(PREFIX_PROCESSING_INSTRUCTION)) {
			tokenType = PROCESSING_INSTRUCTION;
		} else if (value.startsWith(PREFIX_CLOSING_XML_TAG)) {
			tokenType = CLOSING_XML_TAG;
		} else if (value.startsWith(PREFIX_XML_TAG)) {
			if (value.endsWith(SUFFIX_SELFCLOSING_XML_TAG)) {
				tokenType = SELFCLOSING_XML_TAG;
			} else {
				tokenType = XML_TAG;
			}
		} else if (value.startsWith(PREFIX_SINGLE_STRING)) {
			tokenType = STRING;
		} else if (value.startsWith(PREFIX_DOUBLE_STRING)) {
			tokenType = STRING;
		} else if (value.startsWith(PREFIX_MYBATIS_VALUE_REFERENCE)
				|| value.startsWith(PREFIX_MYBATIS_VARIABLE_REFERENCE)) {
			tokenType = MYBATIS_REFERENCE;
		} else if (value.startsWith(PREFIX_SQL_COMMENT)) {
			tokenType = SQL_COMMENT;
		} else if (value.equals(VALUE_COMMA)) {
			tokenType = COMMA;
		} else if (value.equals(VALUE_DOT)) {
			tokenType = DOT;
		} else if (value.equals(VALUE_OPENING_PARENTHESIS)) {
			tokenType = OPENING_PARENTHESIS;
		} else if (value.equals(VALUE_CLOSING_PARENTHESIS)) {
			tokenType = CLOSING_PARENTHESIS;
		} else {
			tokenType = TERM;
		}
		token.setTokenType(tokenType);
	}

	/**
	 * Determines the type of the tokens in the list and of their subtokens based on their context.
	 * 
	 * @param tokenList
	 *            The list of tokens.
	 */
	private void determineTokenTypeFromContext(List<Token> tokenList) {
		if (CollectionUtils.isNotEmpty(tokenList)) {
			for (int i = 0; i < tokenList.size(); i++) {
				Token token = tokenList.get(i);
				String tokenName = token.tokenName();
				if (token.getTokenType().isAmbiguousTokenType(tokenName)) {
					List<Token> beforeTokenList = tokenList.subList(0, i);
					List<Token> afterTokenList = tokenList.subList(i, tokenList.size());
					RESOLVER_MAP.get(tokenName).resolve(token, beforeTokenList, afterTokenList);
				}
			}
		}
	}

}