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
package com.liato.bankdroid.provider;

/**
 * Defines what types of accounts are supported.
 *
 * @since 8 jan 2011
 */
public interface IAccountTypes {

    int REGULAR = 1;
    int FUNDS = 2;
    int LOANS = 3;
    int CCARD = 4;
    int OTHER = 5;
}
