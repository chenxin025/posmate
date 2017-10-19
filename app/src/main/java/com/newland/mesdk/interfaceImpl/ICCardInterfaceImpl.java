package com.newland.mesdk.interfaceImpl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.iccard.ICCardModule;
import com.newland.mtype.module.common.iccard.ICCardSlot;
import com.newland.mtype.module.common.iccard.ICCardSlotState;
import com.newland.mtype.module.common.iccard.ICCardType;
import com.newland.mesdk.moduleinterface.ICCardInterface;
import com.newland.mesdk.util.ModuleBase;

/**
 * Created by YJF . IC卡模块接口实现类
 */
public class ICCardInterfaceImpl extends ModuleBase implements ICCardInterface {
	private ICCardModule icCardModule;

	public ICCardInterfaceImpl() {
		icCardModule = (ICCardModule) factory.getModule(ModuleType.COMMON_ICCARDREADER);
	}

	//发起一个IC卡通信请求
	@Override
	public byte[] call(ICCardSlot slot, ICCardType cardType, byte[] req, long timeout, TimeUnit timeunit) {
		return icCardModule.call(slot, cardType, req, timeout, timeunit);
	}

	//获取当前IC卡状态
	@Override
	public Map<ICCardSlot, ICCardSlotState> checkSlotsState() {
		return icCardModule.checkSlotsState();
	}

	//卡槽下电
	@Override
	public void powerOff(ICCardSlot slot, ICCardType cardType) {
		icCardModule.powerOff(slot, cardType);
	}

	//卡槽上电
	@Override
	public byte[] powerOn(ICCardSlot slot, ICCardType cardType) {
		return icCardModule.powerOn(slot, cardType);
	}

	//设置当前的IC卡类型
	@Override
	public void setICCardType(ICCardSlot slot, ICCardType cardType) {
		icCardModule.setICCardType(slot, cardType);
	}
}
