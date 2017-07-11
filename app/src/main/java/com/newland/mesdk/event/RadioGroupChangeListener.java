package com.newland.mesdk.event;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cynoware.posmate.R;
import com.newland.mtype.module.common.iccard.ICCardSlot;
import com.newland.mtype.module.common.iccard.ICCardType;
import com.newland.mtype.module.common.pin.MacAlgorithm;
import com.newland.mtype.module.common.rfcard.RFKeyMode;
import com.newland.mesdk.util.Const;

/**
 * Created by YJF on 2015/8/17 0017.
 */
public class RadioGroupChangeListener implements RadioGroup.OnCheckedChangeListener {
	private Boolean changeGroup = false;
	private RadioButton radio_MAC_ECB, radio_MAC_X99, radio_MAC_X919, radio_MAC_9606, radio_KEYA_0X60, radio_KEYA_0X00, radio_KEYA_0X61,
			radio_KEYA_0X01, radio_IC1, radio_IC2, radio_IC3, radio_SAM1, radio_SAM2, radio_SAM3;
	private RadioGroup radioGroup_encrypt_type1, radioGroup_encrypt_type2, radioGroup_iccard_type1;
	private MacAlgorithm macAlgorithm = MacAlgorithm.MAC_ECB;
	private RFKeyMode rfKeyMode = RFKeyMode.KEYA_0X60;
	private ICCardSlot iCCardSlot = ICCardSlot.IC1;
	private ICCardType icCardType = ICCardType.CPUCARD;
	private RadioButton radio_CPUCARD, radio_AT24CXX, radio_AT88SC102;
	private int flag;

	public RadioGroupChangeListener(View view, int flag) {
		this.flag = flag;
		if (flag == Const.DialogView.IC_CARD_ICCardSlot_DIALOG) {
			radio_IC1 = (RadioButton) view.findViewById(R.id.radio_IC1);
			radio_IC2 = (RadioButton) view.findViewById(R.id.radio_IC2);
			radio_IC3 = (RadioButton) view.findViewById(R.id.radio_IC3);
			radio_SAM1 = (RadioButton) view.findViewById(R.id.radio_SAM1);
			radio_SAM2 = (RadioButton) view.findViewById(R.id.radio_SAM2);
			radio_SAM3 = (RadioButton) view.findViewById(R.id.radio_SAM3);
			radio_CPUCARD = (RadioButton) view.findViewById(R.id.radio_CPUCARD);
			radio_AT24CXX = (RadioButton) view.findViewById(R.id.radio_AT24CXX);
			radio_AT88SC102 = (RadioButton) view.findViewById(R.id.radio_AT88SC102);
		}
		radioGroup_encrypt_type1 = (RadioGroup) view.findViewById(R.id.radioGroup_encrypt_type1);
		radioGroup_encrypt_type2 = (RadioGroup) view.findViewById(R.id.radioGroup_encrypt_type2);
		radioGroup_encrypt_type1.setOnCheckedChangeListener(this);
		radioGroup_encrypt_type2.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group != null && checkedId > -1 && changeGroup == false) {
			if (flag == Const.DialogView.MAC_CACL_DIALOG) {
				if (group == radioGroup_encrypt_type1) {
					changeGroup = true;
					radioGroup_encrypt_type2.clearCheck();
					if (checkedId == radio_MAC_ECB.getId()) {
						macAlgorithm = MacAlgorithm.MAC_ECB;
					} else if (checkedId == radio_MAC_X99.getId()) {
						macAlgorithm = MacAlgorithm.MAC_X99;
					}
					changeGroup = false;
				} else if (group == radioGroup_encrypt_type2) {
					changeGroup = true;
					radioGroup_encrypt_type1.clearCheck();
					if (checkedId == radio_MAC_X919.getId()) {
						macAlgorithm = MacAlgorithm.MAC_X919;
					} else if (checkedId == radio_MAC_9606.getId()) {
						macAlgorithm = MacAlgorithm.MAC_9606;
					}
					changeGroup = false;
				}
			} else if (flag == Const.DialogView.NC_CARD_KEY_DIALOG) {
				if (group == radioGroup_encrypt_type1) {
					changeGroup = true;
					radioGroup_encrypt_type2.clearCheck();
					if (checkedId == radio_KEYA_0X60.getId()) {
						rfKeyMode = RFKeyMode.KEYA_0X60;
					} else if (checkedId == radio_KEYA_0X00.getId()) {
						rfKeyMode = RFKeyMode.KEYA_0X00;
					}
					changeGroup = false;
				} else if (group == radioGroup_encrypt_type2) {
					changeGroup = true;
					radioGroup_encrypt_type1.clearCheck();
					if (checkedId == radio_KEYA_0X61.getId()) {
						rfKeyMode = RFKeyMode.KEYB_0X61;
					} else if (checkedId == radio_KEYA_0X01.getId()) {
						rfKeyMode = RFKeyMode.KEYB_0X01;
					}
					changeGroup = false;
				}
			} else if (flag == Const.DialogView.IC_CARD_ICCardSlot_DIALOG) {
				if (group == radioGroup_encrypt_type1) {
					changeGroup = true;
					radioGroup_encrypt_type2.clearCheck();
					if (checkedId == radio_IC1.getId()) {
						iCCardSlot = ICCardSlot.IC1;
					} else if (checkedId == radio_IC2.getId()) {
						iCCardSlot = ICCardSlot.IC2;
					} else if (checkedId == radio_IC3.getId()) {
						iCCardSlot = ICCardSlot.IC3;
					}
					changeGroup = false;
				} else if (group == radioGroup_encrypt_type2) {
					changeGroup = true;
					radioGroup_encrypt_type1.clearCheck();
					if (checkedId == radio_SAM1.getId()) {
						iCCardSlot = ICCardSlot.SAM1;
					} else if (checkedId == radio_SAM2.getId()) {
						iCCardSlot = ICCardSlot.SAM2;
					} else if (checkedId == radio_SAM3.getId()) {
						iCCardSlot = ICCardSlot.SAM3;
					}
					changeGroup = false;
				}
			} else if (group == radioGroup_iccard_type1) {
				changeGroup = true;
				if (checkedId == radio_CPUCARD.getId()) {
					icCardType = ICCardType.CPUCARD;
				} else if (checkedId == radio_AT24CXX.getId()) {
					icCardType = ICCardType.AT24CXX;
				} else if (checkedId == radio_AT88SC102.getId()) {
					icCardType = ICCardType.AT88SC102;
				}
				changeGroup = false;
			}
		}

	}

	public MacAlgorithm getMacAlgorithm() {
		return macAlgorithm;
	}

	public void setMacAlgorithm(MacAlgorithm macAlgorithm) {
		this.macAlgorithm = macAlgorithm;
	}

	public RFKeyMode getRfKeyMode() {
		return rfKeyMode;
	}

	public void setRfKeyMode(RFKeyMode rfKeyMode) {
		this.rfKeyMode = rfKeyMode;
	}

	public ICCardSlot getiCCardSlot() {
		return iCCardSlot;
	}

	public void setiCCardSlot(ICCardSlot iCCardSlot) {
		this.iCCardSlot = iCCardSlot;
	}

	public ICCardType getIcCardType() {
		return icCardType;
	}

	public void setIcCardType(ICCardType icCardType) {
		this.icCardType = icCardType;
	}

}
