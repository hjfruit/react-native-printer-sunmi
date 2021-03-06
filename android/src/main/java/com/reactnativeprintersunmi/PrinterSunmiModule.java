package com.reactnativeprintersunmi;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.peripheral.printer.WoyouConsts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ReactModule(name = PrinterSunmiModule.NAME)
public class PrinterSunmiModule extends ReactContextBaseJavaModule {
  private SunmiPrinterService sunmiPrinterService;
  private final ReactApplicationContext reactContext;
  public static final String NAME = "PrinterSunmi";

  public PrinterSunmiModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  public void setGlobalPrinterStyle(final ReadableMap printerStyle) throws RemoteException {
    Boolean isInvert = printerStyle.hasKey("INVERT") ? printerStyle.getBoolean("INVERT") : null;
    Boolean isAntiWhite = printerStyle.hasKey("ANTI_WHITE") ? printerStyle.getBoolean("ANTI_WHITE") : null;
    boolean isBold = printerStyle.hasKey("BOLD") && printerStyle.getBoolean("BOLD");
    boolean isDoubleHeight = printerStyle.hasKey("DOUBLE_HEIGHT") && printerStyle.getBoolean("DOUBLE_HEIGHT");
    boolean isDoubleWidth = printerStyle.hasKey("DOUBLE_WIDTH") && printerStyle.getBoolean("DOUBLE_WIDTH");
    boolean ilalic = printerStyle.hasKey("ILALIC") && printerStyle.getBoolean("ILALIC");
    boolean isUnderline = printerStyle.hasKey("UNDERLINE") && printerStyle.getBoolean("UNDERLINE");
    Integer absPosition = printerStyle.hasKey("ABSOLUATE_POSITION") ? printerStyle.getInt("ABSOLUATE_POSITION") : null;
    Integer leftSpacing = printerStyle.hasKey("LEFT_SPACING") ? printerStyle.getInt("LEFT_SPACING") : null;
    Integer lineSpacing = printerStyle.hasKey("LINE_SPACING") ? printerStyle.getInt("LINE_SPACING") : null;
    Integer relativePosition = printerStyle.hasKey("LINE_SPACING") ? printerStyle.getInt("LINE_SPACING") : null;
    Integer rightSpacing = printerStyle.hasKey("TEXT_RIGHT_SPACING") ? printerStyle.getInt("TEXT_RIGHT_SPACING") : null;
    if (isInvert != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_INVERT, isInvert ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    }
    if (isAntiWhite != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_ANTI_WHITE, isAntiWhite ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    }
    sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_BOLD, isBold ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_DOUBLE_HEIGHT, isDoubleHeight ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_DOUBLE_WIDTH, isDoubleWidth ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_ILALIC, ilalic ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    sunmiPrinterService.setPrinterStyle(WoyouConsts.ENABLE_UNDERLINE, isUnderline ? WoyouConsts.ENABLE : WoyouConsts.DISABLE);
    if (absPosition != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.SET_ABSOLUATE_POSITION, absPosition);
    }
    if (leftSpacing != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.SET_LEFT_SPACING, leftSpacing);
    }
    if (lineSpacing != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.SET_LINE_SPACING, lineSpacing);
    }
    if (relativePosition != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.SET_RELATIVE_POSITION, relativePosition);
    }
    if (rightSpacing != null) {
      sunmiPrinterService.setPrinterStyle(WoyouConsts.SET_TEXT_RIGHT_SPACING, rightSpacing);
    }
  }

  @TargetApi(Build.VERSION_CODES.N)
  private int[] toArray(Object[] arr) {
    return Arrays.stream(arr).mapToInt(e -> Integer.parseInt(e.toString())).toArray();
  }

  private void sendEvent(ReactContext ctx, String eventName) {
    ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, null);
  }
  @ReactMethod
  public void openPrinter(final ReadableMap options, final int mode, final Promise promise) throws RemoteException {
    int currentMode = sunmiPrinterService.getPrinterMode();
    if ( mode != currentMode) {
      promise.reject("MODE_ERROR", "????????????????????????????????????????????????" + PrinterMode.getDescByCode(currentMode));
    } else {
      boolean buffer = true;
      if (mode == 2) {
        buffer = false;
      }
      this.printWithOptions(options,mode, buffer, promise);
    }
  }
  public void printWithOptions(final ReadableMap options, int mode, final boolean buffer, final Promise promise) throws RemoteException {
    ReadableArray content = options.hasKey("content") ? options.getArray("content") : null;
    // ????????????????????????????????????
    if (mode == 2) {
      sunmiPrinterService.labelLocate();
    }
    // ??????????????????
    if (buffer) {
      // ????????????
      sunmiPrinterService.enterPrinterBuffer(true);
    }
    for (int i = 0; i < content.size(); i++) {
      ReadableMap rowConfig = content.getMap(i);
      int wrap = rowConfig.hasKey("wrap") ? rowConfig.getInt("wrap") : 0; // ??????????????????????????????
      String data = rowConfig.hasKey("data") ? rowConfig.getString("data"): "";
      if (!"".equals(data)) {
        // ????????????
        sunmiPrinterService.setAlignment(1, null);
        int modulesize = rowConfig.hasKey("modulesize") ? rowConfig.getInt("modulesize") : 8;
        int errorlevel = rowConfig.hasKey("errorlevel") ? rowConfig.getInt("errorlevel") : 0;
        // ???????????????
        sunmiPrinterService.printQRCode(data, modulesize, errorlevel, null);
        // ??????
        if (wrap != 0) {
          sunmiPrinterService.lineWrap(wrap, null);
        }
        continue;
      }
      ReadableArray textRow = rowConfig.getArray("row");
      Integer fontSize = rowConfig.hasKey("fontSize") ? rowConfig.getInt("fontSize") : null;
      boolean bold = rowConfig.hasKey("bold") && rowConfig.getBoolean("bold");
      // ????????????????????????
      if (fontSize != null) {
        sunmiPrinterService.setFontSize(fontSize, null);
      }
      sunmiPrinterService.sendRAWData(bold ? new byte[]{0x1B, 0x45, 0x01} : new byte[]{0x1B, 0x45, 0x0}, null);
      // ??????????????????(??????)
      int size = textRow.size();
      if (size == 1) {
        ReadableMap item = textRow.getMap(0);
        String text = item.hasKey("text") ? item.getString("text") : "";
        int align = item.hasKey("align") ? item.getInt("align") : 0;
        Integer innerFontSize = item.hasKey("fontSize") ? item.getInt("fontSize") : null;
        boolean innerBold = item.hasKey("bold") && item.getBoolean("bold");
        sunmiPrinterService.setAlignment(align, null);
        if (innerFontSize != null) {
          sunmiPrinterService.setFontSize(innerFontSize, null);
        }
        sunmiPrinterService.sendRAWData(innerBold ? new byte[]{0x1B, 0x45, 0x01} : new byte[]{0x1B, 0x45, 0x0}, null);
        sunmiPrinterService.printOriginalText(text + "\n", null);
      } else {
        // ???????????????
        ArrayList<String> textArr = new ArrayList<>();
        ArrayList<Integer> widthArr = new ArrayList<>();
        ArrayList<Integer> alignArr = new ArrayList<>();
        for (int j = 0; j < size; j++) {
          ReadableMap atom = textRow.getMap(j); // ?????????????????????
          String text = atom.hasKey("text") ? atom.getString("text") : "";
          int align = atom.hasKey("align") ? atom.getInt("align") : 0;
          int width = atom.hasKey("width") ? atom.getInt("width") : 1;
          textArr.add(text);
          widthArr.add(width);
          alignArr.add(align);
        }
        sunmiPrinterService.printColumnsString(textArr.toArray(new String[textArr.size()]), toArray(widthArr.toArray()), toArray(alignArr.toArray()), null);
      }
      // ??????
      if (wrap != 0) {
        sunmiPrinterService.lineWrap(wrap, null);
      }
    }
    if (mode == 2) {
      sunmiPrinterService.labelOutput();
    }
    if (buffer) {
      sunmiPrinterService.commitPrinterBufferWithCallback(new InnerResultCallback() {
        @Override
        public void onRunResult(boolean b) throws RemoteException {

        }

        @Override
        public void onReturnString(String s) throws RemoteException {

        }

        @Override
        public void onRaiseException(int i, String s) throws RemoteException {

        }

        @Override
        public void onPrintResult(int code, String s) throws RemoteException {
          if (code == 0) {
            promise.resolve(s);
          } else {
            promise.reject("PRINT_ERROR", s);
          }
        }
      });
    } else {
      promise.resolve("????????????");
    }
  }

  @ReactMethod
  public void getPrinterState(Promise promise) {
    WritableMap result = Arguments.createMap();
    try{
      int code = sunmiPrinterService.updatePrinterState();
//      Log.i("ReactNative", code+"");
      switch (code) {
        case 1:
          result.putString("state", PrinterState.FINE.getCode());
          result.putString("desc", PrinterState.FINE.getMsg());
          break;
        case 3:
          result.putString("state", PrinterState.CONNECT_EXCEPTION.getCode());
          result.putString("desc", PrinterState.CONNECT_EXCEPTION.getMsg());
          break;
        case 4:
          result.putString("state", PrinterState.OUT_OF_PAPER.getCode());
          result.putString("desc", PrinterState.OUT_OF_PAPER.getMsg());
          break;
        case 5:
          result.putString("state", PrinterState.OVER_HEAT.getCode());
          result.putString("desc", PrinterState.OVER_HEAT.getMsg());
          break;
        case 6:
          result.putString("state", PrinterState.LID_OPENED.getCode());
          result.putString("desc", PrinterState.LID_OPENED.getMsg());
          break;
        case 505:
          result.putString("state", PrinterState.NO_PRINTER.getCode());
          result.putString("desc", PrinterState.NO_PRINTER.getMsg());
          break;
        default:
          result.putString("state", String.valueOf(code));
          result.putString("desc", "????????????");
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    promise.resolve(result);
  }

  @ReactMethod
  public void connect(final Promise promise)  {
    InnerPrinterCallback printerCallback = new InnerPrinterCallback() {
      @Override
      protected void onConnected(SunmiPrinterService service) {
        sunmiPrinterService = service;
        promise.resolve(true);
      }

      @Override
      protected void onDisconnected() {
        // ??????disconnect????????????????????????????????????
        sendEvent(reactContext, "onDisconnect");
      }
    };
    // ????????????
    try {
     boolean success = InnerPrinterManager.getInstance().bindService(reactContext, printerCallback);
     if (!success) {
       promise.reject("CONNECTING_FAILED", "can not find service or no permission to bind");
     }
    } catch (Exception e){
      // no server or can not bind
      promise.reject("CONNECTING_FAILED", e.getMessage());
    }
  }
  // ?????????????????????
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    final String devicesName = Build.SERIAL.toUpperCase(Locale.ENGLISH);
    boolean supported = false;
    if (devicesName.startsWith("V2")) {
      supported = true;
    }
    constants.put("DEVICES_NAME", devicesName); // ???????????????
    constants.put("SUPPORTED", supported); // ????????????????????????????????????????????????????????????
    return constants;
  }
}
