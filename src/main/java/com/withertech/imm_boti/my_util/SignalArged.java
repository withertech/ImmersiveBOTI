package com.withertech.imm_boti.my_util;

import com.withertech.imm_boti.Helper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SignalArged<Arg> {
    private List<Consumer<Arg>> funcList = new ArrayList<>();
    private boolean isEmitting = false;
    
    public void emit(Arg arg) {
        isEmitting = true;
        try {
            funcList.forEach(runnable -> runnable.accept(arg));
        }
        finally {
            isEmitting = false;
        }
    }
    
    //NOTE the func should not capture owner
    public <T> void connectWithWeakRef(T owner, BiConsumer<T, Arg> func) {
        //NOTE using weak hash map was a mistake
        //https://stackoverflow.com/questions/8051912/will-a-weakhashmaps-entry-be-collected-if-the-value-contains-the-only-strong-re
        
        WeakReference<T> weakRef = new WeakReference<>(owner);
        Helper.SimpleBox<Consumer<Arg>> boxOfRunnable = new Helper.SimpleBox<>(null);
        boxOfRunnable.obj = (arg) -> {
            T currentTarget = weakRef.get();
            if (currentTarget != null) {
                func.accept(currentTarget, arg);
            }
            else {
                disconnect(boxOfRunnable.obj);
            }
        };
        connect(boxOfRunnable.obj);
    }
    
    public void connect(Consumer<Arg> func) {
        copyDataWhenEmitting();
        funcList.add(func);
    }
    
    public void disconnect(Consumer<Arg> func) {
        copyDataWhenEmitting();
        boolean removed = funcList.remove(func);
        assert removed;
    }
    
    private void copyDataWhenEmitting() {
        if (isEmitting) {
            funcList = new ArrayList<>(funcList);
        }
    }
}
