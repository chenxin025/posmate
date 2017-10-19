package com.newland.mesdk.moduleinterface;

import java.util.concurrent.TimeUnit;

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
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.pin.PinInputResult;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.pin.WorkingKeyType;

/**
 * Created by HJP
 *  on 2015/8/12.
 */
public interface PinInputInterface {
	public MacResult calcMac(MacAlgorithm macAlgorithm, KeyManageType pinManageType, WorkingKey wk, byte[] input);

	public void cancelPinInput();

	public byte[] decrypt(EncryptAlgorithm encryptAlgorithm,WorkingKey wk, byte[] input, byte[] cbcInit);

	public byte[] encrypt(EncryptAlgorithm encryptAlgorithm,WorkingKey wk, byte[] input, byte[] cbcInit);

	public PinInputResult encryptPIN(WorkingKey wk, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol, byte[] pin);

	public KSNLoadResult loadIPEK(KSNKeyType keytype, int KSNIndex, byte[] ksn, byte[] defaultKeyData, int mainKeyIndex, byte[] checkValue);

	public byte[] loadMainKey(KekUsingType kekUsingType, int mainIndex, byte[] data, byte[] checkValue, int kekIndex);

	public LoadPKResultCode loadPublicKey(LoadPKType keytype, int pkIndex, String pkLength, byte[] pkModule, byte[] pkExponent, byte[] index,
			byte[] mac);

	public byte[] loadWorkingKey(WorkingKeyType type, int mainKeyIndex, int workingKeyIndex, byte[] data, byte[] checkValue);

	public PinInputEvent startStandardPinInput(WorkingKey workingKey, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol,
			int inputMaxLen, byte[] pinPadding, boolean isEnterEnabled, String displayContent, long timeout, TimeUnit timeunit);

	public void startStandardPinInput(WorkingKey workingKey, KeyManageType pinManageType, AccountInputType acctInputType, String acctSymbol,
			int inputMaxLen, byte[] pinPadding, boolean isEnterEnabled, String displayContent, long timeout, TimeUnit timeunit,
			DeviceEventListener<PinInputEvent> inputListener);
}
