package com.notzyvex.fastprint;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(FastPrintPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
