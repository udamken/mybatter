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

import static de.dknapps.mybatter.tokenizer.TokenTypeDeterminer.RESOLVER_MAP;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

public class TokenTypeTest {

	@Test
	public void test_static_ensureAmbiguousSubtokenTypesAreHandled() {
		Set<String> ambiguousTokenNameList = new HashSet<>();
		for (TokenType tokenType : TokenType.values()) {
			if (CollectionUtils.isNotEmpty(tokenType.getTokenNameList())) {
				for (String tokenName : tokenType.getTokenNameList()) {
					if (tokenType.isAmbiguousTokenType(tokenName) && RESOLVER_MAP.get(tokenName) == null) {
						ambiguousTokenNameList.add(tokenName);
					}
				}
			}
		}
		assertEquals(new HashSet<String>().toString(), ambiguousTokenNameList.toString());
	}

}
