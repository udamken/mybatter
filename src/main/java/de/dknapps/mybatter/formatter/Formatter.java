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

import static de.dknapps.mybatter.formatter.Format.SPACE;
import static de.dknapps.mybatter.tokenizer.TokenType.CHARACTER_DATA;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_ENCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_PRIMARY_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.CLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.COMMA;
import static de.dknapps.mybatter.tokenizer.TokenType.DOCUMENT_DECLARATION;
import static de.dknapps.mybatter.tokenizer.TokenType.ENCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.MYBATIS_REFERENCE;
import static de.dknapps.mybatter.tokenizer.TokenType.OPENING_PARENTHESIS;
import static de.dknapps.mybatter.tokenizer.TokenType.PRIMARY_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.PROCESSING_INSTRUCTION;
import static de.dknapps.mybatter.tokenizer.TokenType.ROOT;
import static de.dknapps.mybatter.tokenizer.TokenType.SELFCLOSING_ENCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.SELFCLOSING_PRIMARY_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.SELFCLOSING_XML_TAG;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_COMBINING_STATEMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_STATEMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_STATEMENT_SUFFIX;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_SUB_STATEMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_SUB_STATEMENT_PREFIX;
import static de.dknapps.mybatter.tokenizer.TokenType.SQL_SUB_STATEMENT_SUFFIX;
import static de.dknapps.mybatter.tokenizer.TokenType.STRING;
import static de.dknapps.mybatter.tokenizer.TokenType.TERM;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_COMMENT;
import static de.dknapps.mybatter.tokenizer.TokenType.XML_TAG;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.dknapps.mybatter.tokenizer.Token;
import de.dknapps.mybatter.tokenizer.TokenType;
import de.dknapps.mybatter.tokenizer.Tokenizer;

/**
 * Format a MyBatis mapper input string into a well-formatted string.
 * 
 * <pre>
 *     TODO Use in-memory database to verify sql statements in mapper xml are valid
 *     TODO Use stack for indentions:
 *            with pop till select
 *            /sql pop till sql ... DONE
 *            and no pop
 *            ) pop till (
 *          What about always pushing and popping on some sort of type hierarchy
 *     TODO Allow to indent join by inserting a popping token before next substatement
 *     TODO Use context to refine token type: functions, AND in BETWEEN
 *     TODO Respect maxLineLength depending on token type, see {@link #write(Token)}
 *     TODO Tokenizer ... get rid of tokenize, do it when constructing ... but JUnit?
 *     TODO Formatter ... get rid of format, do it when construction ... but JUnit?
 * </pre>
 */
public class Formatter {

	/** Generic delimiter between several terms */
	static final String VALUE_SPACE = " ";

	/** Map of format information per token type to be written before writing a token */
	private static final Map<TokenType, Format> BEFORE_FORMAT_MAP = new HashMap<>();

	/** Map of format information per token type to be written after writing a token */
	private static final Map<TokenType, Format> AFTER_FORMAT_MAP = new HashMap<>();

	/** Maximum length of a line to be written to the output */
	private final int maxLineLength;

	/** Number of blanks to be used for a single indention */
	private final int indentionSize;

	/** Result of formatting the input string */
	private StringWriter output = new StringWriter();

	/** Length of line written so far */
	private int lineLength;

	/** Indention to be used with next newline */
	private int indention;

	/** Number of blanks currently withhold */
	private int blankCount;

	/** Stack of indententions formats with isStackIndention() true */
	private Deque<Integer> indentionStack = new ArrayDeque<>();

	/**
	 * Sets up static final maps.
	 */
	static {
		// Root token type

		addFormats(ROOT, SPACE, SPACE); // useless, just to be complete

		// Parent token types

		addFormats(CHARACTER_DATA, SPACE, SPACE);
		addFormats(DOCUMENT_DECLARATION, new Format(1, 0, 0), new Format(1, 0, 0));
		addFormats(PROCESSING_INSTRUCTION, new Format(1, 0, 0), new Format(1, 0, 0));
		addFormats(XML_COMMENT, new Format(2, 0, 0), new Format(1, 0, 0));
		addFormats(STRING, SPACE, SPACE);
		addFormats(SQL_COMMENT, new Format(0, 0, 1), new Format(1, 0, 0));
		addFormats(COMMA, new Format(0, 0, 0), new Format(1, 0, 0));
		addFormats(OPENING_PARENTHESIS, new Format(0, 0, 0), new Format(1, 1, 0));
		addFormats(CLOSING_PARENTHESIS, new Format(1, -1, 0), new Format(1, 0, 0));
		addFormats(TERM, SPACE, SPACE);

		addFormats(XML_TAG, new Format(1, 0, 0), new Format(1, 1, 0));
		addFormats(CLOSING_XML_TAG, new Format(1, -1, 0), new Format(1, 0, 0));
		addFormats(SELFCLOSING_XML_TAG, new Format(1, 0, 0), new Format(1, 0, 0));

		// Subtoken types (XML)

		addFormats(PRIMARY_XML_TAG, new Format(1, 0, 0), new Format(1, 1, 0, true));
		addFormats(CLOSING_PRIMARY_XML_TAG, new Format(1, -1, 0, true), new Format(2, 0, 0));
		addFormats(SELFCLOSING_PRIMARY_XML_TAG, new Format(1, 0, 0), new Format(2, 0, 0));

		addFormats(ENCLOSING_XML_TAG, new Format(1, 0, 0), new Format(2, 1, 0));
		addFormats(CLOSING_ENCLOSING_XML_TAG, new Format(1, -1, 0), new Format(2, 0, 0));
		addFormats(SELFCLOSING_ENCLOSING_XML_TAG, new Format(1, 0, 0), new Format(2, 0, 0));

		// Subtoken types (TERM)

		addFormats(MYBATIS_REFERENCE, retrieveBeforeFormat(TERM), retrieveAfterFormat(TERM));
		addFormats(SQL_STATEMENT, new Format(1, 0, 0), new Format(1, 1, 0));
		addFormats(SQL_SUB_STATEMENT, new Format(1, -1, 0), new Format(1, 1, 0));
		addFormats(SQL_COMBINING_STATEMENT, new Format(2, -1, 0), new Format(2, 0, 0));

		addFormats(SQL_STATEMENT_SUFFIX, SPACE, retrieveAfterFormat(SQL_STATEMENT));
		addFormats(SQL_SUB_STATEMENT_PREFIX, retrieveBeforeFormat(SQL_SUB_STATEMENT), SPACE);
		addFormats(SQL_SUB_STATEMENT_SUFFIX, SPACE, retrieveAfterFormat(SQL_SUB_STATEMENT));
	}

	/**
	 * Add before and after format for a token type to the format maps.
	 */
	private static void addFormats(TokenType tokenType, Format beforeFormat, Format afterFormat) {
		BEFORE_FORMAT_MAP.put(tokenType, beforeFormat);
		AFTER_FORMAT_MAP.put(tokenType, afterFormat);
	}

	public Formatter() {
		this(80, 4);
	}

	public Formatter(int maxLineLength, int indentionSize) {
		this.maxLineLength = maxLineLength;
		this.indentionSize = indentionSize;
	}

	public String format(String input) {

		// Analyse input and split into a list of top level tokens
		Tokenizer tokenizer = new Tokenizer(input);
		tokenizer.tokenize();
		List<Token> tokenList = tokenizer.getTokenList();

		// TODO Determine the type of every token by looking at its neighbors

		// Write every token in a formatted way to the output
		Token previousToken = null;
		for (Token token : tokenList) {
			write(deriveEffectiveBeforeFormat(token, previousToken));
			write(token);
			previousToken = token;
		}

		// Return the contents of the output stream as a string
		return output.toString();
	}

	/**
	 * Join formats of one token or two consecutive tokens into one by aggregation of their linefeeds and
	 * indentions or by just using one of them without aggregation (in case of prefixes or suffixes). The
	 * result is the effective format *before* the current token.
	 * 
	 * @param token
	 *            The current token.
	 * @param previousToken
	 *            The previous token.
	 * @return The format to be written *before* writing the current token.
	 */
	private Format deriveEffectiveBeforeFormat(Token token, Token previousToken) {
		Format beforeFormat;
		TokenType tokenType = token.getTokenType();
		beforeFormat = retrieveBeforeFormat(tokenType);
		if (previousToken != null) {
			TokenType previousTokenType = previousToken.getTokenType();
			Format previousAfterFormat = retrieveAfterFormat(previousTokenType);
			if (tokenType == SQL_COMMENT && allowSqlCommentOnSameLine(previousTokenType)) {
				beforeFormat = SPACE;
			} else if (tokenType == SQL_STATEMENT_SUFFIX || tokenType == SQL_SUB_STATEMENT_SUFFIX) {
				beforeFormat = SPACE;
			} else if (previousTokenType == SQL_SUB_STATEMENT_PREFIX) {
				beforeFormat = SPACE;
			} else {
				beforeFormat = joinFormats(previousAfterFormat, beforeFormat);
			}
		}
		return beforeFormat;
	}

	/**
	 * Returns true if the token type is one of those that allows sql comments on the same line for tokens of
	 * this type.
	 * 
	 * @param tokenType
	 *            The token type.
	 * @return True for token types that allow comments on same line.
	 */
	private static boolean allowSqlCommentOnSameLine(TokenType tokenType) {
		if (!tokenType.isParentTokenType()) {
			return allowSqlCommentOnSameLine(tokenType.getParentTokenType());
		}
		return tokenType == STRING //
				|| tokenType == SQL_COMMENT //
				|| tokenType == COMMA //
				|| tokenType == OPENING_PARENTHESIS //
				|| tokenType == CLOSING_PARENTHESIS //
				|| tokenType == TERM;
	}

	/**
	 * Retrieve formatting information to be applied before writing this token.
	 * 
	 * @param token
	 *            The token type to get formatting information for.
	 */
	static Format retrieveBeforeFormat(TokenType tokenType) {
		return BEFORE_FORMAT_MAP.get(tokenType);
	}

	/**
	 * Retrieve formatting information to be applied after writing this token.
	 * 
	 * @param token
	 *            The token type to get formatting information for.
	 */
	static Format retrieveAfterFormat(TokenType tokenType) {
		return AFTER_FORMAT_MAP.get(tokenType);
	}

	/**
	 * Join two consecutive formats into one by of aggregation their linefeeds and indentions.
	 * 
	 * @param first
	 *            The first format.
	 * @param second
	 *            The first format.
	 * @return A joined format.
	 */
	private Format joinFormats(Format first, Format second) {
		int linefeedCount = Math.max(first.getNewlineCount(), second.getNewlineCount());
		int indentionDelta = first.getIndentionDelta() + second.getIndentionDelta();
		int blankCount = Math.min(first.getBlankCount(), second.getBlankCount());
		boolean stackIndention = first.isStackIndention() || second.isStackIndention();
		return new Format(linefeedCount, indentionDelta, blankCount, stackIndention);
	}

	/**
	 * Writes linefeeds and indentions to the output.
	 * 
	 * @param token
	 *            The format to be written.
	 * @return
	 */
	private void write(Format format) {
		for (int i = 0; i < format.getNewlineCount(); i++) {
			newline();
		}
		if (format.isStackIndention() && format.getIndentionDelta() < 0) {
			indention = (indentionStack.isEmpty()) ? 0 : indentionStack.pop();
		}
		indention += format.getIndentionDelta();
		if (format.isStackIndention() && format.getIndentionDelta() > 0) {
			indentionStack.push(indention);
		}
		blankCount += format.getBlankCount();
	}

	/**
	 * Writes a token in a formatted way to the output.
	 * 
	 * @param token
	 *            The token to be written.
	 * @return
	 */
	private void write(Token token) {
		// TODO Respect maxLineLength depending on token type
		if (lineLength == 0) {
			write(StringUtils.repeat(VALUE_SPACE, indention * indentionSize));
		} else {
			write(StringUtils.repeat(VALUE_SPACE, blankCount));
		}
		blankCount = 0;
		write(token.getValue());
	}

	/**
	 * Writes a string to the output and increase lineLine accordingly.
	 * 
	 * @param string
	 *            String to be written
	 */
	public void write(String string) {
		output.write(string);
		lineLength += string.length();
	}

	/**
	 * Write a line break to the output if not at the very beginning.
	 */
	public void newline() {
		if (output.getBuffer().length() == 0) {
			return;
		}
		blankCount = 0; // no need to write blanks to the end of the line
		output.write('\n');
		lineLength = 0;
	}

}
