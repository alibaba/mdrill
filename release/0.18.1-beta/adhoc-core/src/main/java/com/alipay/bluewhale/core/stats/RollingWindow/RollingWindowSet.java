package com.alipay.bluewhale.core.stats.RollingWindow;

import com.alipay.bluewhale.core.callback.RunnableCallback;

public class RollingWindowSet {
    public RunnableCallback updater;
    public RunnableCallback extractor;
    public RollingWindow[] windows;
    public Object all_time;
}
