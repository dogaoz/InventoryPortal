package com.bitpops.barcode.Helpers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.bitpops.barcode.Model.Action;
import com.bitpops.barcode.Model.Product;
import com.bitpops.barcode.R;
import com.bitpops.barcode.Utils.HandlerUtils;
import com.bitpops.barcode.Utils.ThreadPoolManager;
import com.iposprinter.iposprinterservice.IPosPrinterCallback;
import com.iposprinter.iposprinterservice.IPosPrinterService;

import static com.bitpops.barcode.Utils.MemInfo.bitmapRecycle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class PosPrinter {

    private static final String TAG = "PosPrinter";
    /* Demo 版本号*/
    private static final String VERSION = "V1.1.0";

    /*定义打印机状态*/
    private final int PRINTER_NORMAL = 0;
    private final int PRINTER_PAPERLESS = 1;
    private final int PRINTER_THP_HIGH_TEMPERATURE = 2;
    private final int PRINTER_MOTOR_HIGH_TEMPERATURE = 3;
    private final int PRINTER_IS_BUSY = 4;
    private final int PRINTER_ERROR_UNKNOWN = 5;
    /*打印机当前状态*/
    private int printerStatus = 0;

    /*定义状态广播*/
    private final String PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION";
    private final String PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.PAPERLESS_ACTION";
    private final String PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION";
    private final String PRINTER_THP_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION";
    private final String PRINTER_THP_NORMALTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION";
    private final String PRINTER_MOTOR_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION";
    private final String PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION";
    private final String PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION = "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION";

    /*定义消息*/
    private final int MSG_TEST = 1;
    private final int MSG_IS_NORMAL = 2;
    private final int MSG_IS_BUSY = 3;
    private final int MSG_PAPER_LESS = 4;
    private final int MSG_PAPER_EXISTS = 5;
    private final int MSG_THP_HIGH_TEMP = 6;
    private final int MSG_THP_TEMP_NORMAL = 7;
    private final int MSG_MOTOR_HIGH_TEMP = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER = 9;
    private final int MSG_CURRENT_TASK_PRINT_COMPLETE = 10;

    /*循环打印类型*/
    private final int MULTI_THREAD_LOOP_PRINT = 1;
    private final int INPUT_CONTENT_LOOP_PRINT = 2;
    private final int DEMO_LOOP_PRINT = 3;
    private final int PRINT_DRIVER_ERROR_TEST = 4;
    private final int DEFAULT_LOOP_PRINT = 0;

    //循环打印标志位
    private int loopPrintFlag = DEFAULT_LOOP_PRINT;
    private byte loopContent = 0x00;
    private int printDriverTestCount = 0;


    private IPosPrinterService mIPosPrinterService;
    private IPosPrinterCallback callback = null;

    private Random random = new Random();
    private HandlerUtils.MyHandler handler;

    static Context ct;
    static PosPrinter pos_printer;
    static String last_print_function = null;
    static String last_print_text = null;
    static Action last_print_data = null;
    PosPrinter(Context _ct) {
        ct = _ct;
    }
    public static PosPrinter getInstance(Context _ct)
    {
        if (pos_printer == null)
        {
            pos_printer = new PosPrinter(_ct);
            pos_printer.preparePrinter();
        }
        else
        {
            ct = _ct;
        }

        return pos_printer;
    }
    public void printLastPrinted(Context ct)
    {

        Method method = null;
        try {
            if (last_print_data == null)
            {
                Toast.makeText(ct.getApplicationContext(),"There is nothing printed last.",Toast.LENGTH_LONG);
                throw new Exception();
            }
            Class[] cArg = new Class[1];
            cArg[0] = Action.class;
            method = PosPrinter.class.getDeclaredMethod(last_print_function,cArg);
            method.invoke(PosPrinter.this,last_print_data);
            Toast.makeText(ct.getApplicationContext(),"Printing now...",Toast.LENGTH_LONG);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    /**
     * 消息处理
     */
    private HandlerUtils.IHandlerIntent iHandlerIntent = new HandlerUtils.IHandlerIntent() {
        @Override
        public void handlerIntent(Message msg) {
            switch (msg.what) {
                case MSG_TEST:
                    break;
                case MSG_IS_NORMAL:
                    if (getPrinterStatus() == PRINTER_NORMAL) {
                        //loopPrint(loopPrintFlag);
                        Toast.makeText(ct, "Loop Print Flag", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_IS_BUSY:
                    Toast.makeText(ct, R.string.printer_is_working, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_LESS:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(ct, R.string.out_of_paper, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_EXISTS:
                    Toast.makeText(ct, R.string.exists_paper, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_THP_HIGH_TEMP:
                    Toast.makeText(ct, R.string.printer_high_temp_alarm, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_MOTOR_HIGH_TEMP:
                    loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(ct, R.string.motor_high_temp_alarm, Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000);  //马达高温报警，等待3分钟后复位打印机
                    break;
                case MSG_MOTOR_HIGH_TEMP_INIT_PRINTER:
                    printerInit();
                    break;
                case MSG_CURRENT_TASK_PRINT_COMPLETE:
                    Toast.makeText(ct, R.string.printer_current_task_print_complete, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver IPosPrinterStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Log.d(TAG, "IPosPrinterStatusListener onReceive action = null");
                return;
            }
            Log.d(TAG, "IPosPrinterStatusListener action = " + action);
            if (action.equals(PRINTER_NORMAL_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_IS_NORMAL, 0);
            } else if (action.equals(PRINTER_PAPERLESS_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_PAPER_LESS, 0);
            } else if (action.equals(PRINTER_BUSY_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_IS_BUSY, 0);
            } else if (action.equals(PRINTER_PAPEREXISTS_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_PAPER_EXISTS, 0);
            } else if (action.equals(PRINTER_THP_HIGHTEMP_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_THP_HIGH_TEMP, 0);
            } else if (action.equals(PRINTER_THP_NORMALTEMP_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_THP_TEMP_NORMAL, 0);
            } else if (action.equals(PRINTER_MOTOR_HIGHTEMP_ACTION))  //此时当前任务会继续打印，完成当前任务后，请等待2分钟以上时间，继续下一个打印任务
            {
                handler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP, 0);
            } else if (action.equals(PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION)) {
                handler.sendEmptyMessageDelayed(MSG_CURRENT_TASK_PRINT_COMPLETE, 0);
            } else {
                handler.sendEmptyMessageDelayed(MSG_TEST, 0);
            }
        }
    };

    /**
     * 绑定服务实例
     */
    private ServiceConnection connectService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPosPrinterService = IPosPrinterService.Stub.asInterface(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIPosPrinterService = null;
        }
    };

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//       // setContentView(R.layout.activity_ipos_printer_test_demo);
//        //设置屏幕一直亮着，不进入休眠状态
//        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        preparePrinter();
//    }

    public void preparePrinter() {


        handler = new HandlerUtils.MyHandler(iHandlerIntent);

        callback = new IPosPrinterCallback.Stub() {

            @Override
            public void onRunResult(final boolean isSuccess) throws RemoteException {
                Log.i(TAG, "result:" + isSuccess + "\n");
            }

            @Override
            public void onReturnString(final String value) throws RemoteException {
                Log.i(TAG, "result:" + value + "\n");
            }
        };

        //绑定服务
        Intent intent = new Intent();
        intent.setPackage("com.iposprinter.iposprinterservice");
        intent.setAction("com.iposprinter.iposprinterservice.IPosPrintService");
        //startService(intent);
        ct.bindService(intent, connectService, Context.BIND_AUTO_CREATE);

        //注册打印机状态接收器
        IntentFilter printerStatusFilter = new IntentFilter();
        printerStatusFilter.addAction(PRINTER_NORMAL_ACTION);
        printerStatusFilter.addAction(PRINTER_PAPERLESS_ACTION);
        printerStatusFilter.addAction(PRINTER_PAPEREXISTS_ACTION);
        printerStatusFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION);
        printerStatusFilter.addAction(PRINTER_BUSY_ACTION);

        ct.registerReceiver(IPosPrinterStatusListener, printerStatusFilter);
        // printerInit();
    }


    protected void onResume() {
        Log.d(TAG, "activity onResume");
        //super.onResume();
    }


    protected void onPause() {
        Log.d(TAG, "activity onPause");
        //super.onPause();
    }


    protected void onStop() {
        Log.e(TAG, "activity onStop");
        loopPrintFlag = DEFAULT_LOOP_PRINT;
        //super.onStop();
    }

    protected void Destroy() {
        Log.d(TAG, "activity onDestroy");
        //super.onDestroy();
        ct.unregisterReceiver(IPosPrinterStatusListener);
        ct.unbindService(connectService);
        handler.removeCallbacksAndMessages(null);
    }


    /**
     * 获取打印机状态
     */
    public int getPrinterStatus() {

        Log.i(TAG, "***** printerStatus" + printerStatus);
        try {
            printerStatus = mIPosPrinterService.getPrinterStatus();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "#### printerStatus" + printerStatus);
        return printerStatus;
    }

    /**
     * 打印机初始化
     */
    public void printerInit() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    mIPosPrinterService.printerInit(callback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public IPosPrinterService get_mIPosPrinterService()
    {
        return mIPosPrinterService;
    }
    public IPosPrinterCallback get_callback()
    {
        return callback;
    }
    public void printConfirmation(final Action action)
    {
        last_print_function = "printConfirmation";
        last_print_data = action;
        final SavedData sd = new SavedData(ct);
        if (getPrinterStatus() != PRINTER_IS_BUSY)
        {
            ThreadPoolManager.getInstance().executeTask(new Runnable() {
                @Override
                public void run() {
                    Bitmap mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.mipmap.test);
                    try {
                        mIPosPrinterService.printSpecifiedTypeText("Inventory Portal\n", "ST", 32, callback);
                        mIPosPrinterService.printBlankLines(1, 16, callback);

                        mIPosPrinterService.setPrinterPrintAlignment(1,callback);
                        mIPosPrinterService.printBlankLines(1, 16, callback);
                        mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                        mIPosPrinterService.printSpecifiedTypeText( sd.getCompanyName() + " \n", "ST", 32, callback);
                        mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                        mIPosPrinterService.printSpecifiedTypeText("Action: " + action.getActionType() + " \n", "ST", 32, callback);
                        mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                        mIPosPrinterService.printSpecifiedTypeText("From: "+ action.getLocationFrom() +" \n", "ST", 32, callback);
                        mIPosPrinterService.printSpecifiedTypeText("To: "+ action.getLocationTo() +" \n", "ST", 32, callback);
                        mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                        mIPosPrinterService.printSpecifiedTypeText("Authorized by: "+ sd.getDeviceId() +"\n", "ST", 32, callback);
                        mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                        mIPosPrinterService.printSpecifiedTypeText("Products to transfer: \n", "ST", 32, callback);
                        mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                        mIPosPrinterService.setPrinterPrintAlignment(0,callback);
                        ArrayList<Product> products = action.getProducts();
                        for (int i = 0; i < products.size(); i++)
                        {
                            mIPosPrinterService.printSpecifiedTypeText("Product Code: \n" + products.get(i).getProductProperties().get("InventoryID"), "ST", 24, callback);
                            mIPosPrinterService.printSpecifiedTypeText("Product Name: \n" + products.get(i).getProductProperties().get("Name"), "ST", 24, callback);
                            mIPosPrinterService.printSpecifiedTypeText("Location before transfer: \n" + products.get(i).getProductProperties().get("Warehouse Location"), "ST", 24, callback);
                            mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                            mIPosPrinterService.printBlankLines(1, 16, callback);
                        }

//                        mIPosPrinterService.printQRCode(text, 10, 1, callback);
//                        mIPosPrinterService.printBlankLines(1, 16, callback);
//                        mIPosPrinterService.printBarCode(data.get(0),8,8,16,0,callback);
//                        mIPosPrinterService.printBlankLines(1, 16, callback);
                        mIPosPrinterService.printSpecifiedTypeText("**********END***********\n\n", "ST", 32, callback);
                        bitmapRecycle(mBitmap);
                        mIPosPrinterService.printerPerformPrint(160,  callback);

                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            });
        }
        else
        {
            Log.e("PRINTER","PRINTER IS BUSY");
        }



//                                            Uri uri = Uri.parse("mailto:" + "")
//                                                    .buildUpon()
//                                                    .appendQueryParameter("subject", "PORTAL REPORT - Android")
//                                                    .appendQueryParameter("body", finalText_data)
//                                                    .build();
//
//                                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
//                                            startActivity(Intent.createChooser(emailIntent, "Choose an app to share:"));
    }
    public void printText()
    {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                Bitmap mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.mipmap.test);
                try {
                    mIPosPrinterService.printSpecifiedTypeText("   Inventory Portal Test\n", "ST", 30, callback);
                    mIPosPrinterService.printBlankLines(1, 16, callback);
                    mIPosPrinterService.printBitmap(1, 12, mBitmap, callback);
                    mIPosPrinterService.printBlankLines(1, 16, callback);
                    mIPosPrinterService.printSpecifiedTypeText("********************************", "ST", 24, callback);
                    mIPosPrinterService.printSpecifiedTypeText("ABCDEFGHIJKLMNOPQRSTUVWXYZ01234\n", "ST", 16, callback);
                    mIPosPrinterService.printSpecifiedTypeText("abcdefghijklmnopqrstuvwxyz56789\n", "ST", 24, callback);
                    mIPosPrinterService.setPrinterPrintAlignment(0,callback);
                    mIPosPrinterService.printQRCode("https://TEST\n", 10, 1, callback);
                    mIPosPrinterService.printBlankLines(1, 16, callback);
                    mIPosPrinterService.printBlankLines(1, 16, callback);
                    mIPosPrinterService.printSpecifiedTypeText("**********END***********\n\n", "ST", 32, callback);
                    bitmapRecycle(mBitmap);
                    mIPosPrinterService.printerPerformPrint(160,  callback);

                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }


    public void printQRCode()
    {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    mIPosPrinterService.setPrinterPrintAlignment(0, callback);
                    mIPosPrinterService.printQRCode("http://www.baidu.com\n", 2, 1, callback);
                    mIPosPrinterService.printBlankLines(1, 15, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(1, callback);
                    mIPosPrinterService.printQRCode("http://www.baidu.com\n", 3, 0, callback);
                    mIPosPrinterService.printBlankLines(1, 15, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(2, callback);
                    mIPosPrinterService.printQRCode("http://www.baidu.com\n", 4, 2, callback);
                    mIPosPrinterService.printBlankLines(1, 15, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(0, callback);
                    mIPosPrinterService.printQRCode("http://www.baidu.com\n", 5, 3, callback);
                    mIPosPrinterService.printBlankLines(1, 15, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(1, callback);
                    mIPosPrinterService.printQRCode("http://www.baidu.com\n", 6, 2, callback);
                    mIPosPrinterService.printBlankLines(1, 15, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(2, callback);
                    mIPosPrinterService.printQRCode("http://www.baidu.com\n", 7, 1, callback);
                    mIPosPrinterService.printBlankLines(1, 15, callback);

                    mIPosPrinterService.printerPerformPrint(160,  callback);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }





}
