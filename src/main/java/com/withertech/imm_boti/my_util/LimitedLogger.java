package com.withertech.imm_boti.my_util;

import com.withertech.imm_boti.Helper;

import java.util.function.Supplier;

// Log error and avoid spam
// TODO use this to replace these
public class LimitedLogger {
    private int remain;
    
    public LimitedLogger(int maxCount) {
        remain = maxCount;
    }
    
    public void log(String s) {
        invoke(() -> Helper.log(s));
    }
    
    public void err(String s) {
        invoke(() -> Helper.err(s));
    }
    
    public void invoke(Runnable r) {
        if (remain > 0) {
            remain--;
            r.run();
        }
    }
    
    public void throwException(Supplier<RuntimeException> s) {
        invoke(() -> {
            throw s.get();
        });
    }
}
