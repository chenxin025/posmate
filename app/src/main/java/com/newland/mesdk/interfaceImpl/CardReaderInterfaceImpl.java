package com.newland.mesdk.interfaceImpl;

import java.util.concurrent.TimeUnit;

import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.cardreader.OpenCardReaderResult;
import com.newland.mtype.module.common.rfcard.RFCardType;
import com.newland.mesdk.moduleinterface.CardReaderInterface;
import com.newland.mesdk.util.ModuleBase;

/**
 * Created by YJF 读卡器接口实现
 */
public class CardReaderInterfaceImpl extends ModuleBase implements CardReaderInterface {

	private CardReader cardReader;

	public CardReaderInterfaceImpl() {
		cardReader = (CardReader) factory.getModule(ModuleType.COMMON_CARDREADER);
	}
	
	//取消读卡操作
	@Override
	public void cancelCardRead() {
		cardReader.cancelCardRead();
	}

	//关闭读卡器
	@Override
	public void closeCardReader() {
		cardReader.closeCardReader();
	}

	//获取上次读卡的类型
	@Override
	public ModuleType[] getLastReaderTypes() {
		ModuleType[] lastReaderTypes = cardReader.getLastReaderTypes();
		return lastReaderTypes;
	}

	//获取支持的读卡类型
	@Override
	public ModuleType[] getSupportCardReaderModule() {
		ModuleType[] moduleTypes = cardReader.getSupportCardReaderModule();
		return moduleTypes;
	}

	//打开读卡器
	@Override
	public OpenCardReaderResult openCardReader(String screenText, ModuleType[] openReaders, RFCardType[] expectedRfCardTypes,
			boolean isAllowfallback, boolean isMSDChecking, long timeout, TimeUnit timeunit) {
		OpenCardReaderResult readResult = cardReader.openCardReader(screenText, new ModuleType[] { ModuleType.COMMON_SWIPER,
				ModuleType.COMMON_ICCARDREADER, ModuleType.COMMON_RFCARDREADER }, null, true, true, 30, TimeUnit.SECONDS);
		return readResult;
	}

	//打开读卡器
	@Override
	public void openCardReader(String screenText, ModuleType[] openReaders, RFCardType[] expectedRfCardTypes, boolean isAllowfallback,
			boolean isMSDChecking, long timeout, TimeUnit timeunit, DeviceEventListener<OpenCardReaderEvent> listener) {
		cardReader.openCardReader(screenText, openReaders, expectedRfCardTypes, isAllowfallback, isMSDChecking, timeout, timeunit, listener);
	}
}
