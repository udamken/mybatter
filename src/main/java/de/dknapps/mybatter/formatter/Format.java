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

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Format {

	/** Format for just a space without newlines or blanks */
	public static final Format SPACE = new Format(0, 0, 1);

	/** Number of line breaks to be written when writing this format */
	@Getter
	private final int newlineCount;

	/** Number of indentions to add (+) or subtract (-) when writing this format */
	@Getter
	private final int indentionDelta;

	/** Number of blanks to add (+) or substract (-) when writing this format */
	@Getter
	private final int blankCount;

	/** Push indention after increasing or pop before decreasing it */
	@Getter
	private final boolean stackIndention;

	public Format(int newlineCount, int indentionDelta, int blankCount) {
		this(newlineCount, indentionDelta, blankCount, false);
	}

}
