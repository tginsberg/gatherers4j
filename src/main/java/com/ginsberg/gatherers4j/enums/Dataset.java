/*
 * Copyright 2024-2026 Todd Ginsberg
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

/// Whether the elements seen by a statistical gatherer represent an entire population or a sample of a larger one.
public enum Dataset {
    /// The elements are the entire dataset. Variance is divided by *n*.
    Population,

    /// The elements are a sample of a larger population. Variance is divided by *n - 1*, so it is undefined for a
    /// single element. Gatherers using this mode emit nothing until at least two elements have been seen.
    Sample
}
