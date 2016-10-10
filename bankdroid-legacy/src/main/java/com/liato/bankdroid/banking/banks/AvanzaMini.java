/*
 * Copyright (C) 2014 Nullbyte <http://nullbyte.eu>
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

package com.liato.bankdroid.banking.banks;

import com.liato.bankdroid.banking.banks.avanza.Avanza;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.Context;

public class AvanzaMini extends Avanza {

    public AvanzaMini(Context context) {
        super(context, R.drawable.logo_avanzamini);
        TAG = "AvanzaMini";
        NAME = "Avanza Mini";
        NAME_SHORT = "avanzamini";
        URL = "https://www.avanza.se/mini/hem/";
        BANKTYPE_ID = IBankTypes.AVANZAMINI;
    }
}
