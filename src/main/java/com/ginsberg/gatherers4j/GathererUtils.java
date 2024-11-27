/*
 * Copyright 2024 Todd Ginsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ginsberg.gatherers4j;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract class GathererUtils {

    final static long NANOS_PER_MILLISECOND = 1_000_000;


    static <INPUT> List<INPUT> listOfNullables(final @Nullable INPUT left, final @Nullable INPUT right) {
        final List<INPUT> list = new ArrayList<>();
        list.add(left);
        list.add(right);
        return list;
    }

    static void mustNotBeNull(final @Nullable Object subject, final String message) {
        if (subject == null) {
            throw new IllegalArgumentException(message);
        }
    }

    static boolean safeEquals(final @Nullable Object left, final @Nullable Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }
}
