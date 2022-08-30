/*
 * Copyright 2016-2022 Stefan Kalscheuer
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

package de.stklcode.pubtrans.ura.model;

import java.io.IOException;
import java.io.Serializable;

/**
 * Interface for model classes to bundle common methods.
 *
 * @author Stefan Kalscheuer
 * @since 1.1.1
 */
interface Model extends Serializable {
    /**
     * Generate exception for unmatched type when String is expected.
     *
     * @param field  Field number.
     * @param actual Actual class.
     * @return The Exception.
     */
    static IOException typeErrorString(int field, Class actual) {
        return typeError(field, actual, "String");
    }

    /**
     * Generate exception for unmatched type.
     *
     * @param field    Field number.
     * @param actual   Actual class.
     * @param expected Expected type.
     * @return The Exception.
     */
    static IOException typeError(int field, Class actual, String expected) {
        return new IOException(String.format("Field %d not of expected type %s, found %s",
                field, expected, actual.getSimpleName()));
    }
}
