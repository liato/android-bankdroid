/*
 * Copyright (C) 2014 Nullbyte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liato.bankdroid.banking.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ErrorMessage implements Serializable {

    private static final long serialVersionUID = 7228754028321179052L;

    @JsonProperty
    String title;

    @JsonProperty
    String code;

    @JsonProperty
    String message;

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
