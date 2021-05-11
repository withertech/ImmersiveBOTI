package com.withertech.imm_boti.render;

import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;

public class GlQueryObject {
    private int idQueryObject = -1;
    private boolean isQuerying = false;
    private boolean hasResult = false;
    
    public GlQueryObject(int handle) {
        this.idQueryObject = handle;
    }
    
    public void performQueryAnySamplePassed(Runnable renderingFunc) {
        // mac does not support any samples passed query
        if (Minecraft.IS_RUNNING_ON_MAC) {
            performQuerySampleNumPassed(renderingFunc);
            return;
        }
        
        performQuery(renderingFunc, GL33.GL_ANY_SAMPLES_PASSED);
    }
    
    public void performQuerySampleNumPassed(Runnable renderingFunc) {
        performQuery(renderingFunc, GL15.GL_SAMPLES_PASSED);
    }
    
    private void performQuery(Runnable renderingFunc, int glQueryType) {
        Validate.isTrue(isValid());
        
        Validate.isTrue(!isQuerying);
        
        GL15.glBeginQuery(glQueryType, idQueryObject);
        
        isQuerying = true;
        
        renderingFunc.run();
        
        GL15.glEndQuery(glQueryType);
        
        isQuerying = false;
        
        hasResult = true;
    }
    
    public boolean fetchQueryResult() {
        Validate.isTrue(isValid());
        Validate.isTrue(hasResult);
        
        int result = GL15.glGetQueryObjecti(idQueryObject, GL15.GL_QUERY_RESULT);
        
        return result != 0;
    }
    
    private void dispose() {
        if (idQueryObject != -1) {
            GL15.glDeleteQueries(idQueryObject);
            idQueryObject = -1;
        }
    }
    
    public boolean isValid() {
        return idQueryObject != -1;
    }
    
    private void reset() {
        hasResult = false;
    }
    
    private static final ArrayList<GlQueryObject> queryObjects = new ArrayList<>();
    
    private static void prepareQueryObjects() {
        int[] buf = new int[500];
        GL15.glGenQueries(buf);
        for (int id : buf) {
            queryObjects.add(new GlQueryObject(id));
        }
    }
    
    public static GlQueryObject acquireQueryObject() {
        if (queryObjects.isEmpty()) {
            prepareQueryObjects();
        }
        
        return queryObjects.remove(queryObjects.size() - 1);
    }
    
    public static void returnQueryObject(GlQueryObject obj) {
        obj.reset();
        if (queryObjects.size() > 1500) {
            obj.dispose();
        }
        else {
            queryObjects.add(obj);
        }
    }
}
