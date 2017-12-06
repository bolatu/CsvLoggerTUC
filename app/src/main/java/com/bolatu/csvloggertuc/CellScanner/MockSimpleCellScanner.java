/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.bolatu.csvloggertuc.CellScanner;

//import org.mozilla.mozstumbler.service.core.logging.ClientLog;
import android.util.Log;

//import org.mozilla.mozstumbler.svclocator.services.log.LoggerUtil;

import java.util.LinkedList;
import java.util.List;


public class MockSimpleCellScanner implements ISimpleCellScanner {

    private static final String LOG_TAG = "MockSimpleCellScanner";

    private boolean started = false;

    public MockSimpleCellScanner() {
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public List<CellInfo> getCellInfo() {
        LinkedList<CellInfo> result = new LinkedList<CellInfo>();

        ISimulatorService simSvc = (ISimulatorService) ServiceLocator.getInstance()
                                        .getService(ISimulatorService.class);
        try {
            List<CellInfo> cellBlock = simSvc.getNextMockCellBlock();
            if (cellBlock != null) {
                result.addAll(cellBlock);
            }
        } catch (ClassCastException ex) {
            Log.e(LOG_TAG, "Error getting the proper context", ex);
        }
        return result;
    }

    public boolean isSupportedOnThisDevice() {
        return true;
    }
}
