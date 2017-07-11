package com.newland.mesdk.interfaceImpl;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.MSDAlgorithm;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.common.swiper.Swiper;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.newland.mesdk.moduleinterface.SwiperInterface;
import com.newland.mesdk.util.ModuleBase;

/**
 * Created by YJF 刷卡模块接口实现
 */
public class SwiperInterfaceImpl extends ModuleBase implements SwiperInterface {
	private Swiper swiper;
	SwipResult swipResult;

	public SwiperInterfaceImpl() {
		swiper = (Swiper) factory.getModule(ModuleType.COMMON_SWIPER);
	}

	//以掩码方式获取加密磁道信息
	@Override
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, byte[] acctMask, MSDAlgorithm alg) {
		swipResult = swiper.readEncryptResult(readModel, wk, acctMask, alg);
		return swipResult;
	}

	//获取加密的磁道信息
	@Override
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, MSDAlgorithm alg) {
		swipResult = swiper.readEncryptResult(readModel, wk, alg);
		return swipResult;
	}

	//获取明文磁道信息
	@Override
	public SwipResult readPlainResult(SwiperReadModel[] readModel) {
		swipResult = swiper.readPlainResult(readModel);
		if (null != swipResult && swipResult.getRsltType() == SwipResultType.SUCCESS) {
			return swipResult;
		}
		return null;
	}

}
