package com.newland.mesdk.util;

import com.newland.me.DeviceManager;
import com.newland.mtype.Module;
import com.newland.mtype.ModuleType;
import com.newland.mesdk.interfaceImpl.DeviceControllerInterfaceImpl;
/**
 * Created by YJF . 
 * 模块获取
 */
public class ModuleFactory {
	private DeviceManager deviceManager;

	public ModuleFactory() {
		deviceManager = DeviceControllerInterfaceImpl.getDeviceManager();
	}

	public Module getModule(ModuleType moduleType) {
		try {
			Module module = deviceManager.getDevice().getStandardModule(moduleType);
			return module;
		} catch (Exception e) {
			return null;
		}

	}

	public Module getExModule(String moduleType) {
		try {
			Module module = deviceManager.getDevice().getExModule(moduleType);
			return module;
		} catch (Exception e) {
			return null;
		}

	}
}
