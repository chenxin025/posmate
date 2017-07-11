/**
 * ����
 */
package com.cynoware.posmate.sdk;

public class config {
	public static final int CONFIG_PRINTER_UART = 5;
    public static final int CONFIG_TRAY_QRREADER_UART = 1;
    public static final int CONFIG_DOCK_QRREADER_UART = 3;
    public static final int CONFIG_ONBOARD_LED_UART = 6;
    public static final int CONFIG_LED_UART = 1;
    public static final int CONFIG_CASHDRAWER_GPIO = (32 * 2 + 1);    //PC.1
    public static final int CONFIG_ENABLE_12V_GPIO = (32 * 1 + 13);   //PB.13
    public static final int CONFIG_ENABLE_24V_GPIO = (32 * 1 + 14);   //PB.14
    
    public static final int CONFIG_FRAME_2D_ENABLE = 39;   //PB7
    public static final int CONFIG_BEEPER_GPIO = 79;
  
}
