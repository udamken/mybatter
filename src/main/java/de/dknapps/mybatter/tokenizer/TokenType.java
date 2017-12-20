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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Getter;

public enum TokenType {

	// Root token type

	ROOT,

	// Parent token types

	CHARACTER_DATA,
	DOCUMENT_DECLARATION,
	PROCESSING_INSTRUCTION,
	XML_COMMENT,
	STRING,
	SQL_COMMENT,
	COMMA,
	OPENING_PARENTHESIS,
	CLOSING_PARENTHESIS,
	TERM,

	XML_TAG,
	CLOSING_XML_TAG,
	SELFCLOSING_XML_TAG,

	// Subtoken types (XML)

	ENCLOSING_XML_TAG(XML_TAG, "mapper"),
	CLOSING_ENCLOSING_XML_TAG(CLOSING_XML_TAG, ENCLOSING_XML_TAG.getTokenNameList()),
	SELFCLOSING_ENCLOSING_XML_TAG(SELFCLOSING_XML_TAG, ENCLOSING_XML_TAG.getTokenNameList()),

	PRIMARY_XML_TAG(XML_TAG, "cache", "resultmap", "select", "insert", "update", "delete", "sql"),
	CLOSING_PRIMARY_XML_TAG(CLOSING_XML_TAG, PRIMARY_XML_TAG.getTokenNameList()),
	SELFCLOSING_PRIMARY_XML_TAG(SELFCLOSING_XML_TAG, PRIMARY_XML_TAG.getTokenNameList()),

	// Subtoken types (TERM)

	MYBATIS_REFERENCE(TERM),
	SQL_STATEMENT(TERM, "select", "update", "insert", "delete"),
	SQL_SUB_STATEMENT(TERM, "from", "group", "having", "join", "on", "order", "set", "values", "where",
			"with"),
	SQL_COMBINING_STATEMENT(TERM, "union"),

	// Context dependend types

	SQL_STATEMENT_SUFFIX(TERM, "all", "distinct", "from", "into"),
	SQL_SUB_STATEMENT_PREFIX(TERM, "left", "right", "outer", "inner"),
	SQL_SUB_STATEMENT_SUFFIX(TERM, "by", "cs", "rr", "rs", "ur");

	/** Token type that this token type is a subtype of */
	@Getter
	final private TokenType parentTokenType;

	/** Names of tokens that should belong to this token type */
	@Getter
	final private List<String> tokenNameList;

	/**
	 * Constructs a parent token type.
	 */
	private TokenType() {
		this(null);
	}

	/**
	 * Constructs a subtoken type.
	 * 
	 * @param parentTokenType
	 *            Parent token type that this token type is a subtype of.
	 * @param tokenNameArray
	 *            Names of tokens that should belong to this token type.
	 */
	private TokenType(TokenType parentTokenType, String... tokenNameArray) {
		this(parentTokenType, Arrays.asList(tokenNameArray));
	}

	/**
	 * Constructs a subtoken type.
	 * 
	 * @param parentTokenType
	 *            Parent token type that this token type is a subtype of.
	 * @param tokenNameList
	 *            Names of tokens that should belong to this token type.
	 */
	private TokenType(TokenType parentTokenType, List<String> tokenNameList) {
		this.parentTokenType = parentTokenType;
		this.tokenNameList = tokenNameList;
	}

	/**
	 * Returns true for a parent token type.
	 */
	public boolean isParentTokenType() {
		return parentTokenType == null;
	}

	/**
	 * Returns true if this and another instance with the same parent token type both mention the same token
	 * name.
	 * 
	 * @param tokenName
	 *            The name of the token.
	 * @return True if token type is ambiguous.
	 */
	public boolean isAmbiguousTokenType(String tokenName) {
		// TODO It might be useful to cache these boolean values ...
		if (CollectionUtils.isNotEmpty(tokenNameList) && tokenNameList.contains(tokenName)) {
			for (TokenType other : TokenType.values()) {
				if (this != other && parentTokenType == other.parentTokenType
						&& CollectionUtils.isNotEmpty(other.tokenNameList)
						&& other.tokenNameList.contains(tokenName)) {
					return true;
				}
			}
		}
		return false;
	}

}