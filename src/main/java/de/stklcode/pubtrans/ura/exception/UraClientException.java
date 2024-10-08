/*
 * Copyright 2016-2024 Stefan Kalscheuer
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

package de.stklcode.pubtrans.ura.exception;

import java.io.IOException;

/**
 * Custom exception class indicating an error with the URA API communication.
 *
 * @author Stefan Kalscheuer
 * @since 2.0
 */
public class UraClientException extends IOException {
    private static final long serialVersionUID = 4585240685746203433L;

    /**
     * Default constructor.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method).
     */
    public UraClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
