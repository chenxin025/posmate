package com.newland.mesdk.moduleinterface;

import java.util.concurrent.TimeUnit;

import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.cardreader.OpenCardReaderResult;
import com.newland.mtype.module.common.rfcard.RFCardType;

/**
 * Created by HJP on 2015/8/11.
 */
public interface CardReaderInterface {
	public void cancelCardRead();

	public void closeCardReader();

	public ModuleType[] getLastReaderTypes();

	public ModuleType[] getSupportCardReaderModule();

	public OpenCardReaderResult openCardReader(String screenText, ModuleType[] openReaders, RFCardType[] expectedRfCardTypes,
			boolean isAllowfallback, boolean isMSDChecking, long timeout, TimeUnit timeunit);

	public void openCardReader(String screenText, ModuleType[] openReaders, RFCardType[] expectedRfCardTypes, boolean isAllowfallback,
			boolean isMSDChecking, long timeout, TimeUnit timeunit, DeviceEventListener<OpenCardReaderEvent> listener);
}
