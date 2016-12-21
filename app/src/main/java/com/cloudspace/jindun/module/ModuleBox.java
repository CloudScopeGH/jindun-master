package com.cloudspace.jindun.module;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ModuleBox {

    private ClassLoader loader = null;

    private Map<Class<?>, Module> modules = new ConcurrentHashMap<Class<?>, Module>();

    public ModuleBox(ClassLoader loader) {
        this.loader = loader;
    }

    public boolean loadModule(String module) {
        boolean loaded = false;
        try {
            Class<?> modClass = loader.loadClass(module);
            Module mod = (Module) modClass.newInstance();
            this.modules.put(modClass, mod);
            loaded = true;
        } catch (Exception e) {
            Log.e(" module load error [" + module + "]", e.getMessage());
            e.printStackTrace();
            loaded = false;
        }
        return loaded;
    }

    public void destroyModules() {
        if (modules.isEmpty()) {
            return;
        }
        for (Module module : modules.values()) {
            module.destroy();
        }
    }

    public void initModules() {
        for (Module module : modules.values()) {
            boolean isInitialized = false;
            try {
                module.initialize(this);
                isInitialized = true;
            } catch (Exception e) {
                Log.e("module init error [" + module.getClass().getSimpleName() + "]", e.getMessage());
                this.modules.remove(module.getClass());
                if (isInitialized) {
                    module.destroy();
                }
            }
        }
    }

    /**
     * 可能返回null
     *
     * @param module 名称
     * @return
     */
    public Module getModuleByName(String module) {
        for (Class clazz : modules.keySet()) {
            if (clazz.getSimpleName().equals(module)) {
                return modules.get(clazz);
            }
        }
        return null;
    }

}
