package com.newland.mesdk.interfaceImpl;

import java.util.concurrent.TimeUnit;

import com.newland.mesdk.moduleinterface.PinInputInterface;
import com.newland.mesdk.util.ModuleBase;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.AccountInputType;
import com.newland.mtype.module.common.pin.EncryptAlgorithm;
import com.newland.mtype.module.common.pin.KSNKeyType;
import com.newland.mtype.module.common.pin.KSNLoadResult;
import com.newland.mtype.module.common.pin.KekUsingType;
import com.newland.mtype.module.common.pin.KeyManageType;
import com.newland.mtype.module.common.pin.LoadPKResultCode;
import com.newland.mtype.module.common.pin.LoadPKType;
import com.newland.mtype.module.common.pin.MacAlgorithm;
import com.newland.mtype.module.common.pin.MacResult;
import com.newland.mtype.module.common.pin.PinInput;
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.pin.PinInputResult;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.pin.WorkingKeyType;

/**
 * Created by YJF 密码输入接口实现
 */
public class PinInputInterfaceImpl extends ModuleBase implements PinInputInterface {

	private PinInput pinInput;

	public PinInputInterfaceImpl() {
		pinInput = (PinInput) factory.getModule(ModuleType.COMMON_PININPUT);
	}

	// 0.大数据mac计算
	@Override
	public MacResult calcMac(MacAlgorithm macAlgorithm, KeyManageType pinManageType, WorkingKey wk, byte[] input) {
		MacResult macResult = pinInput.calcMac(macAlgorithm, pinManageType, wk, input);
		return macResult;
	}

	// 1.撤消上一次的密码输入
	@Override
	public void cancelPinInput() {
		pinInput.cancelPinInput();
	}

	// 2.解密一串数据
	@Override
	public byte[] decrypt(EncryptAlgorithm encryptAlgorithm, WorkingKey wk, byte[] input, byte[] cbcInit) {
		byte[] result = pinInput.decrypt(encryptAlgorithm,wk, input, cbcInit);
		return result;
	}

	// 3.加密一串数据
	@Override
	public byte[] encrypt(EncryptAlgorithm encryptAlgorithm,WorkingKey wk,  byte[] input, byte[] cbcInit) {
		byte[] result = pinInput.encrypt(encryptAlgorithm, wk, input, cbcInit);
		return result;
	}

	// 4.无键盘输入密码
	@Override
	public PinInputResult encryptPIN(WorkingKey wk, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol, byte[] pin) {
		return pinInput.encryptPIN(wk, pinManageType, acctInputType, acctSymbol, pin);
	}

	// 5.loadIPEK
	@Override
	public KSNLoadResult loadIPEK(KSNKeyType keytype, int KSNIndex, byte[] ksn, byte[] defaultKeyData, int mainKeyIndex, byte[] checkValue) {
		KSNLoadResult ksnLoadResult = pinInput.loadIPEK(keytype, KSNIndex, ksn, defaultKeyData, mainKeyIndex, checkValue);
		return ksnLoadResult;
	}

	// 6.装载主密钥
	@Override
	public byte[] loadMainKey(KekUsingType kekUsingType, int mainIndex, byte[] data, byte[] checkValue, int kekIndex) {
		byte[] mainKey = pinInput.loadMainKey(kekUsingType, mainIndex, data, checkValue, kekIndex);
		return mainKey;
	}

	// 7.load公钥
	@Override
	public LoadPKResultCode loadPublicKey(LoadPKType keytype, int pkIndex, String pkLength, byte[] pkModule, byte[] pkExponent, byte[] index,
			byte[] mac) {
		LoadPKResultCode loadPKResultCode = pinInput.loadPublicKey(keytype, pkIndex, pkLength, pkModule, pkExponent, index, mac);
		return loadPKResultCode;
	}

	// 8.装载工作密钥
	@Override
	public byte[] loadWorkingKey(WorkingKeyType type, int mainKeyIndex, int workingKeyIndex, byte[] data, byte[] checkValue) {
		byte[] wk = pinInput.loadWorkingKey(type, mainKeyIndex, workingKeyIndex, data, checkValue);
		return wk;
	}

	// 9。调用一个pin输入过程
	@Override
	public PinInputEvent startStandardPinInput(WorkingKey workingKey, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol,
			int inputMaxLen, byte[] pinPadding, boolean isEnterEnabled, String displayContent, long timeout, TimeUnit timeunit) {
		PinInputEvent event = pinInput.startStandardPinInput(workingKey, pinManageType, acctInputType, acctSymbol, inputMaxLen, pinPadding,
				isEnterEnabled, displayContent, timeout, timeunit);
		return event;
	}

	// 10开启一个密码输入过程
	@Override
	public void startStandardPinInput(WorkingKey workingKey, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol,
			int inputMaxLen, byte[] pinPadding, boolean isEnterEnabled, String displayContent, long timeout, TimeUnit timeunit,
			DeviceEventListener<PinInputEvent> inputListener) {
		pinInput.startStandardPinInput(workingKey, pinManageType, acctInputType, acctSymbol, inputMaxLen, pinPadding, isEnterEnabled, displayContent,
				timeout, timeunit);
	}
}
