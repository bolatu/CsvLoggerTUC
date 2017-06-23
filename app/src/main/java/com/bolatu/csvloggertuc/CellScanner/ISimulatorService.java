/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.bolatu.csvloggertuc.CellScanner;


import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;

import java.util.List;

public interface ISimulatorService {

    public void startSimulation(Context ctx);
    public void stopSimulation();

    public Location getNextGPSLocation();
    public List<ScanResult> getNextMockWifiBlock();
    public List<CellInfo> getNextMockCellBlock();

}
