
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
    public <fields>;            #匹配所有成员
    public <methods>;           #匹配所有方法
}

-keep class com.cynoware.posmate.sdk.BleDevice{
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.BluetoothLeService {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.BT {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.buf {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.CashDrawer {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.cmds {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.config {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.Debug {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.Device {
     public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.EscCommand {
     public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.Event {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.GPIO {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.GpUtils {
    public <fields>;
    public <methods>;
}
-keep class com.cynoware.posmate.sdk.HidDevice {
     public <fields>;
     public <methods>;
}
 -keep class com.cynoware.posmate.sdk.Keyboard {
     public <fields>;
     public <methods>;
 }
 -keep class com.cynoware.posmate.sdk.LED {
     public <fields>;
     public <methods>;
 }
 -keep class com.cynoware.posmate.sdk.LoopbackTest {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.Printer {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.Beeper {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.QrReader {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.UART {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.uart_cmd {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.UsbHandler {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.UsbHandlerManager {
      public <fields>;
      public <methods>;
  }
  -keep class com.cynoware.posmate.sdk.Util {
      public <fields>;
      public <methods>;
  }

  -keep class com.cynoware.posmate.sdk.SDKLog {
       public <fields>;
       public <methods>;
  }

  -keep class com.cynoware.posmate.sdk.LCD {
         public <fields>;
         public <methods>;
    }




