package com.withertech.tim_wim_holes.render.context_management;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

//sometimes minecraft stores some dimension-specific things into static fields
//for example BackgroundRenderer
//we have to render multiple dimensions at the same time
//so we have to store multiple sets of these static fields
public class StaticFieldsSwappingManager<Context> {
    private final Consumer<Context> copyFromObject;
    private final Consumer<Context> copyToObject;
    private final boolean strictCheck;
    @Nullable
    private final Supplier<Context> contextConstructor;
    
    public static class ContextRecord<Ctx> {
        public RegistryKey<World> dimension;
        public Ctx context;
        
        //sometimes the static fields contain the latest context
        //and our context object contains out-dated info
        public boolean isHoldingLatestContext = false;
        
        public ContextRecord(RegistryKey<World> dimension, Ctx context, boolean isHoldingLatestContext) {
            this.dimension = dimension;
            this.context = context;
            this.isHoldingLatestContext = isHoldingLatestContext;
        }
    }
    
    private RegistryKey<World> outerDimension;
    private Stack<ContextRecord<Context>> swappedContext = new Stack<>();
    
    //this will be managed by other classes
    public final Map<RegistryKey<World>, ContextRecord<Context>> contextMap = new HashMap<>();
    
    public StaticFieldsSwappingManager(
        Consumer<Context> copyFromObject,
        Consumer<Context> copyToObject,
        boolean doStrictCheck,
        @Nullable Supplier<Context> contextConstructor
    ) {
        
        this.copyFromObject = copyFromObject;
        this.copyToObject = copyToObject;
        this.strictCheck = doStrictCheck;
        this.contextConstructor = contextConstructor;
    }
    
    public boolean isSwapped() {
        return !swappedContext.empty();
    }
    
    public void setOuterDimension(RegistryKey<World> dim) {
        Validate.isTrue(!isSwapped());
        
        outerDimension = dim;
    }
    
    public void resetChecks() {
        Validate.isTrue(!isSwapped());
        
        contextMap.values().forEach(record -> {
            record.isHoldingLatestContext = record.dimension != outerDimension;
        });
    }
    
    public RegistryKey<World> getCurrentDimension() {
        if (swappedContext.empty()) {
            Validate.notNull(outerDimension);
            return outerDimension;
        }
        else {
            return swappedContext.peek().dimension;
        }
    }
    
    public void pushSwapping(RegistryKey<World> newDimension) {
        RegistryKey<World> currentDimension = getCurrentDimension();
        
        ContextRecord<Context> oldContext = contextMap.get(currentDimension);
        ContextRecord<Context> newContext = contextMap.computeIfAbsent(newDimension, k -> {
            return new ContextRecord<>(newDimension, contextConstructor.get(), true);
        });
        Validate.notNull(oldContext);
        Validate.notNull(newContext);
        
        swappedContext.push(newContext);
        
        transferDataFromStaticFieldsToObject(oldContext);
        
        transferDataFromObjectToStaticFields(newContext);
    }
    
    public void popSwapping() {
        ContextRecord<Context> outerContext = swappedContext.pop();
        ContextRecord<Context> innerContext = contextMap.get(getCurrentDimension());
        
        transferDataFromStaticFieldsToObject(outerContext);
        
        transferDataFromObjectToStaticFields(innerContext);
    }
    
    public void swapAndInvoke(RegistryKey<World> newDimension, Runnable func) {
        pushSwapping(newDimension);
        func.run();
        popSwapping();
    }
    
    private void transferDataFromObjectToStaticFields(ContextRecord<Context> newContext) {
        if (!strictCheck) {
            if (newContext == null) {
                return;
            }
        }
        
        if (strictCheck) {
            Validate.isTrue(newContext.isHoldingLatestContext);
        }
        newContext.isHoldingLatestContext = false;
        copyFromObject.accept(newContext.context);
    }
    
    private void transferDataFromStaticFieldsToObject(ContextRecord<Context> oldContext) {
        if (!strictCheck) {
            if (oldContext == null) {
                return;
            }
        }
        
        if (strictCheck) {
            Validate.isTrue(!oldContext.isHoldingLatestContext);
        }
        oldContext.isHoldingLatestContext = true;
        copyToObject.accept(oldContext.context);
    }
    
    //called when player teleports
    public void updateOuterDimensionAndChangeContext(RegistryKey<World> newDimension) {
        Validate.isTrue(!isSwapped());
        Validate.notNull(outerDimension);
        
        RegistryKey<World> oldDimension = this.outerDimension;
        
        transferDataFromStaticFieldsToObject(contextMap.get(oldDimension));
        
        transferDataFromObjectToStaticFields(contextMap.get(newDimension));
        
        outerDimension = newDimension;
    }
    
    
}
