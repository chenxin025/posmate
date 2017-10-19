package com.newland.mesdk.moduleinterface;

import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.swiper.MSDAlgorithm;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwiperReadModel;


/**
 * Created by HJP on 2015/8/12.
 */
public interface SwiperInterface {
	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, byte[] acctMask, MSDAlgorithm alg);// ��ȡ���ܵĴŵ���Ϣ

	public SwipResult readEncryptResult(SwiperReadModel[] readModel, WorkingKey wk, MSDAlgorithm alg);

	public SwipResult readPlainResult(SwiperReadModel[] readModel);// 通过安全认证后，使用明文方式返回刷卡结果

}
