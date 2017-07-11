
# 所有方法不进行混淆
#-keep public abstract interface *{
#public protected <methods>;
#}
#保留类及其所有成员不被混淆
#-keep public class com.cynoware.posmate.sdk { *;}

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-dontoptimize
-dontpreverify

-keep public class *

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.cynoware.posmate.sdk.Beeper {
    public <methods>;
}

-keep class com.cynoware.posmate.sdk.BleDevice{
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.BluetoothLeService {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.BT {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.buf {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.CashDrawer {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.cmds {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.Debug {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.Device {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.EscCommand {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.Event {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.GPIO {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.GpUtils {
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.HidDevice {
     public <methods>;
}
 -keep class com.cynoware.posmate.sdk.Keyboard {
     public <methods>;
 }
 -keep class com.cynoware.posmate.sdk.LED {
     public <methods>;
 }
 -keep class com.cynoware.posmate.sdk.LoopbackTest {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.Printer {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.Beeper {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.QrReader {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.UART {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.uart_cmd {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.UsbHandler {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.UsbHandlerManager {
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.util {
      public <methods>;
  }




