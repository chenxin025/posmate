package com.cynoware.posmate.sdk;

public class cmds{
    public static final int MAX_CMD_SIZE                        = 32;
    public static final int MAX_EVENT_SIZE                      = 4;
    public static final byte CMD_REPORT_ID                      = 0x02;

    public static final int UART_CMD_WRITE_HEAD                 = 0x5A;
    public static final int UART_CMD_READ_HEAD                  = 0xA5;
    public static final int UART_CMD_EVENT_HEAD                 = 0x55;
    public static final int UART_CMD_MAX_SIZE                   = (MAX_CMD_SIZE + 4);
    public static final int UART_CMD_READ_SIZE                  = 4;

    public static final int kNoEvent                = 0x00000000;   /* 没有事件返回，保留 */
    public static final int kUnknownEvent           = 0x00000001;   /* 未知类型事件，需要调用查询命令 */
    public static final int kUsbConnected           = 0x00000002;   /* USB接入事件 */
    public static final int kGPIOInterruptTriggered = 0x00000004;   /* GPIO中断事件 */
    //public static final int kUartRxAailable       = 0x00000008;   /* 串口可读事件 */
    public static final int kKeyboardInputing       = 0x00000010;   /* 键盘输入中状态 */
    public static final int kDebugInfoAvailable     = 0x00000020;   /* 有debug信息 */
    public static final int kUartRx0Aailable        = 0x00000040;   /* 串口0可读状态 */
    public static final int kUartRx1Aailable        = 0x00000080;   /* 串口1可读状态 */
    public static final int kUartRx2Aailable        = 0x00000100;   /* 串口2可读状态 */
    public static final int kUartRx3Aailable        = 0x00000200;   /* 串口3可读状态 */
    public static final int kUartRx4Aailable        = 0x00000400;   /* 串口4可读状态 */
    public static final int kUartRx5Aailable        = 0x00000800;   /* 串口5可读状态 */
    public static final int kUartRx6Aailable        = 0x00001000;   /* 串口6可读状态 */
    public static final int kUartRx7Aailable        = 0x00002000;   /* 串口7可读状态 */
    public static final int kConnectionClosed       = 0x80000000;   /* 连接结束事件 */


    public static final int CMD_WRITE_MEM32                     = 0x01;
    public static final int CMD_READ_MEM32                      = 0x02;
    public static final int CMD_WRITE_MEM                       = 0x03;
    public static final int CMD_READ_MEM                        = 0x04;
    public static final int CMD_KEYBOARD                        = 0x05;
    public static final int     SUBCMD_KEYBOARD_INPUT_KEY       = 0x00;
    public static final int     SUBCMD_KEYBOARD_INPUT_STRING    = 0x01;
    public static final int CMD_GPIO                            = 0x06;
    public static final int     SUBCMD_GPIO_SET_MODE            = 0x00;/* set direction and value 设置GPIO模式*/
    public static final int     SUBCMD_GPIO_GET_MODE            = 0x01;/* get direction and value 获取GPIO模式*/
    public static final int     SUBCMD_GPIO_OUTPUT              = 0x02;/* set value GPIO输出*/
    public static final int     SUBCMD_GPIO_INPUT               = 0x03;/* get value GPIO输入*/
    public static final int     SUBCMD_GPIO_SET_INTERRUPT       = 0x04;/* set gpio interrupt mode */
    public static final int CMD_UART                            = 0x07;
    public static final int     SUBCMD_UART_SET_CONFIG          = 0x00;//配置串口
    public static final int     SUBCMD_UART_GET_CONFIG          = 0x01;
    public static final int     SUBCMD_UART_WRITE               = 0x02;//写串口
    public static final int     SUBCMD_UART_READ                = 0x03;//读串口
    public static final int     SUBCMD_UART_CLEAR_BUF           = 0x04;//清除缓存
    public static final int     SUBCMD_UART_SET_AS_BT_PORT      = 0x05;//设置蓝牙Port
    public static final int     SUBCMD_UART_GET_RX_PORT         = 0x06;
    public static final int CMD_EVENT                           = 0x08;
    public static final int     SUBCMD_EVENT_GET                = 0x00;
    public static final int     SUBCMD_EVENT_SET                = 0x01;
    public static final int CMD_GET_INTERFACE_NUMBER            = 0x09;
    public static final int CMD_LOOPBACK_TEST                   = 0x0A;
    public static final int CMD_DEBUG                           = 0x0B;
    public static final int     SUBCMD_DEBUG_WRITE              = 0x00;
    public static final int     SUBCMD_DEBUG_READ               = 0x01;
    public static final int CMD_BT                              = 0x0C;
    public static final int     SUBCMD_BT_SET_MAC               = 0x00;//设置蓝牙地址
    public static final int     SUBCMD_BT_GET_MAC               = 0x01;//读蓝牙地址
}
