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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/*
{
    "errorMessages": {
        "general": [{
            "title": "Mobilbanken ej tillg&auml;nglig",
            "code": "SERVICE_UNAVAILABLE",
            "message": "F&ouml;r n&auml;rvarande &auml;r Mobil- och iPad-apparna otillg&auml;ngliga. V&auml;nligen f&ouml;rs&ouml;k igen senare."
        }]
    }
}
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = 1971937841087070779L;

    @JsonProperty
    Map<String, ArrayList<ErrorMessage>> errorMessages = Collections.emptyMap();

    public Map<String, ArrayList<ErrorMessage>> getErrorMessages() {
        return errorMessages;
    }

}



