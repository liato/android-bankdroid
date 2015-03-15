/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
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

package com.liato.bankdroid.banking.exceptions;

import com.liato.bankdroid.banking.BankChoice;

import java.util.ArrayList;

public class BankChoiceException extends Exception {

    private static final long serialVersionUID = 1L;

    private ArrayList<BankChoice> banks;

    public BankChoiceException(String message) {
        super(message);
    }

    public BankChoiceException(String message, ArrayList<BankChoice> banks) {
        super(message);
        this.banks = banks;
    }

    /**
     * @return the banks
     */
    public ArrayList<BankChoice> getBanks() {
        return banks;
    }
}
