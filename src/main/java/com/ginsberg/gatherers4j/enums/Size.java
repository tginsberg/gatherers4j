/*
 * Copyright 2025 Todd Ginsberg
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

package com.ginsberg.gatherers4j.enums;

public enum Size {
    Equals {
        @Override
        public boolean tryAccept(long length, long target) {
            return length <= target;
        }

        @Override
        public boolean accept(long length, long target) {
            return length == target;
        }
    },
    GreaterThan {
        @Override
        public boolean accept(long length, long target) {
            return length > target;
        }
    },
    GreaterThanOrEqualTo {
        @Override
        public boolean accept(long length, long target) {
            return length >= target;
        }
    },
    LessThan {
        @Override
        public boolean tryAccept(long length, long target) {
            return length < target;
        }
    },
    LessThanOrEqualTo {
        @Override
        public boolean tryAccept(long length, long target) {
            return length <= target;
        }
    };

    public boolean accept(long length, long target) {
        return true;
    }

    public boolean tryAccept(long length, long target) {
        return true;
    }
}
