package com.newland.mesdk.moduleinterface;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.newland.mtype.module.common.emv.AIDConfig;
import com.newland.mtype.module.common.emv.CAPublicKey;
import com.newland.mtype.module.common.emv.EmvCardInfo;
import com.newland.mtype.module.common.emv.EmvControllerListener;
import com.newland.mtype.module.common.emv.EmvDataType;
import com.newland.mtype.module.common.emv.EmvTagRef;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvWorkingMode;
import com.newland.mtype.module.common.emv.OnlinePinConfig;
import com.newland.mtype.module.common.emv.PbocTransLog;
import com.newland.mtype.module.common.emv.TerminalConfig;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.MSDAlgorithm;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;

/**
 * Created by HJP on 2015/8/12.
 */
public interface EmvInterface {
	public boolean addAID(AIDConfig aidConfig);

	public boolean addAIDWithDataSource(byte[] aidDatasource);

	public boolean addCAPublicKey(byte[] rid, CAPublicKey capk);

	public boolean addCAPublicKeyWithDataSource(byte[] caDataSource);

	public boolean clearAllAID();

	public boolean clearAllCAPublicKey();

	public boolean clearCAPublicKeyByRid(byte[] rid);

	public boolean deleteAID(byte[] aid);

	public boolean deleteCAPublicKey(byte[] rid, int index);

	public List<AIDConfig> fetchAllAID();

	public List<CAPublicKey> fetchAllCAPublicKey();

	public byte[] fetchLastTradeInfo(EmvDataType mode);

	public List<PbocTransLog> fetchPbocLog();

	public void fetchPbocLog(EmvControllerListener emvControllerListener);

	public AIDConfig fetchSpecifiedAID(byte[] aid);

	public CAPublicKey fetchSpecifiedCAPublicKey(byte[] rid, int index);

	public EmvCardInfo getAccountInfo(Set<Integer> tags);

	public byte[] getEmvData(int tag);

	public EmvTransController getEmvTransController(EmvControllerListener emvControllerListener);

	public EmvTagRef getSystemSupportTagRef(int tag);

	public void initEmvModule(android.content.Context context);

	public void initEmvModule(android.content.Context context, InputStream inputStream);

	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, MSDAlgorithm alg);

	public boolean setEmvData(int tag, byte[] value);

	public void setOnlinePinConfig(OnlinePinConfig onlinePinConfig);

	public boolean setTrmnlParams(TerminalConfig trmnlConfig);

	public void setWorkingMode(EmvWorkingMode workingMode);
}
