/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.bolatu.csvloggertuc.CellScanner;

import java.util.List;

public interface ISimpleCellScanner {
    void start();

    boolean isStarted();

    void stop();

    List<CellInfo> getCellInfo();

    public boolean isSupportedOnThisDevice();
}
