package com.cynoware.firmwareupdate;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;

/**
 * USB设备模型
 */

public class DeviceModel {

    private UsbDevice usbDevice;
    private UsbDeviceConnection usbDevConn;

    private UsbInterface usbInterface;
    private UsbEndpoint usbEPOut;
    private UsbEndpoint usbEPIn;
    private UsbRequest usbRequest;

    public DeviceModel() {
    }

    public DeviceModel(UsbDevice usbDevice, UsbDeviceConnection usbDevConn, UsbInterface usbInterface,
                       UsbEndpoint usbEPOut, UsbEndpoint usbEPIn, UsbRequest usbRequest) {
        this.usbDevice = usbDevice;
        this.usbDevConn = usbDevConn;
        this.usbInterface = usbInterface;
        this.usbEPOut = usbEPOut;
        this.usbEPIn = usbEPIn;
        this.usbRequest = usbRequest;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public UsbDeviceConnection getUsbDevConn() {
        return usbDevConn;
    }

    public void setUsbDevConn(UsbDeviceConnection usbDevConn) {
        this.usbDevConn = usbDevConn;
    }

    public UsbInterface getUsbInterface() {
        return usbInterface;
    }

    public void setUsbInterface(UsbInterface usbInterface) {
        this.usbInterface = usbInterface;
    }

    public UsbEndpoint getUsbEPOut() {
        return usbEPOut;
    }

    public void setUsbEPOut(UsbEndpoint usbEPOut) {
        this.usbEPOut = usbEPOut;
    }

    public UsbEndpoint getUsbEPIn() {
        return usbEPIn;
    }

    public void setUsbEPIn(UsbEndpoint usbEPIn) {
        this.usbEPIn = usbEPIn;
    }

    public UsbRequest getUsbRequest() {
        return usbRequest;
    }

    public void setUsbRequest(UsbRequest usbRequest) {
        this.usbRequest = usbRequest;
    }

}
