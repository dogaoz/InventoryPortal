/**
* Ipos 打印服务
* IPosPrinterService.aidl
* AIDL Version：1.0.0
*/

package com.iposprinter.iposprinterservice;
import  com.iposprinter.iposprinterservice.IPosPrinterCallback;
import  android.graphics.Bitmap;

interface IPosPrinterService {
    /**
    * 打印机状态查询
    * @return 打印机当前状态
    * <ul>
    * <li>0:PRINTER_NORMAL 此时可以启动新的打印
    * <li>1:PRINTER_PAPERLESS  此时停止打印，如果当前打印未完成，加纸后需重打
    * <li>2:PRINTER_THP_HIGH_TEMPERATURE 此时暂停打印，如果当前打印未完成，冷却后将继续打印，无需重打
    * <li>3:PRINTER_MOTOR_HIGH_TEMPERATURE 此时不执行打印，冷却后需要初始化打印机，重新发起打印任务
    * <li>4:PRINTER_IS_BUSY    此时打印机正在打印
    * <li>5:PRINTE_ERROR_UNKNOWN  打印机异常
    */
    int getPrinterStatus();

    /**
    * 打印机初始化
    * 打印机上电并初始化默认设置
    * 使用时请查询打印机状态，PRINTER_IS_BUSY时请等待
    * @param callback 执行结果回调
    * @return
    */
    void printerInit(in IPosPrinterCallback callback);

    /**
    * 设置打印机的打印浓度，对之后打印有影响，除非初始化
    * @param depth:     浓度等级,范围1-10,超出范围此功能不执行 默认等级6
    * @param callback 执行结果回调
    * @return
    */
    void setPrinterPrintDepth(int depth,in IPosPrinterCallback callback);

    /**
    * 设置打印字体类型，对之后打印有影响，除非初始化
    * （目前只支持一种字体ST，后续会提供更多字体的支持）
    * @param typeface:     字体名称 ST(宋体)
    * @param callback  执行结果回调
    * @return
    */
    void setPrinterPrintFontType(String typeface,in IPosPrinterCallback callback);

    /**
    * Set the font size, which affects subsequent printing unless initialized
    * Note: The font size is a printing method that exceeds the standard international directives.
    * Adjusting the font size will affect the character width, and the number of characters per line will change accordingly.
    * As a result, the layout of monospaced fonts may be confusing and you need to adjust it yourself
    * @param fontsize: the font size. The currently supported sizes are 16, 24, 32, and 48. If you enter an illegal size, the default value is 24.
    * @param callback execution result callback
    * @return
    */
    void setPrinterPrintFontSize(int fontsize,in IPosPrinterCallback callback);

    /**
    * 设置对齐方式，对之后打印有影响，除非初始化
    * @param alignment:	对齐方式 0--居左 , 1--居中, 2--居右,默认居中
    * @param callback  执行结果回调
    * @return
    */
    void setPrinterPrintAlignment(int alignment, in IPosPrinterCallback callback);

    /**
    * 打印机走纸  (强制换行，结束之前的打印内容后走纸lines行，此时马达空转走纸，无数据传送给打印机)
    * @param lines：    打印机走纸行数(每行是一个像素点)
    * @param callback  执行结果回调
    * @return
    */
    void printerFeedLines(int lines,in IPosPrinterCallback callback);

    /**
    * 打印空白行  (强制换行，结束之前的打印内容后打印空白行，此时传送给打印机的数据全是0x00)
    * @param lines：    打印空白行数 限制最多100行
    * @param height：   空白行的高度(单位：像素点)
    * @param callback  执行结果回调
    * @return
    */
    void printBlankLines(int lines,int height,in IPosPrinterCallback callback);

    /**
    * 打印文字
    * 文字宽度满一行自动换行排版
    * @param text:	要打印的文字字符串
    * @param callback  执行结果回调
    * @return
    */
    void printText(String text, in IPosPrinterCallback callback);

    /**
    * 打印指定字体类型和大小文本，字体设置只对本次有效
    * 文字宽度满一行自动换行排版
    * @param text:	要打印的文字字符串
    * @param typeface:  字体名称 ST（目前只支持一种）
    * @param fontsize:	字体大小，目前支持的size有16、24、32、48，输入非法size执行默认值24
    * @param callback  执行结果回调
    * @return
    */
    void printSpecifiedTypeText(String text, String typeface,int fontsize,in IPosPrinterCallback callback);

    /**
    * 打印指定字体类型和大小文本，字体设置只对本次有效
    * 文字宽度满一行自动换行排版
    * @param text:	要打印的文字字符串
    * @param typeface:  字体名称 ST（目前只支持一种）
    * @param fontsize:	字体大小，目前支持的size有16、24、32、48，输入非法size执行默认值24
    * @param alignment:    对齐方式  (0居左, 1居中, 2居右)
    * @param callback  执行结果回调
    * @return
    */
    void PrintSpecFormatText(String text, String typeface, int fontsize, int alignment, IPosPrinterCallback callback);

	/**
	* 打印表格的一行，可以指定列宽、对齐方式
	* @param colsTextArr   各列文本字符串数组
	* @param colsWidthArr  各列宽度数组  总宽度不能大于((384 / fontsize) << 1)-（列数+1）
	*                      (以英文字符计算, 每个中文字符占两个英文字符, 每个宽度大于0),
	* @param colsAlign	        各列对齐方式(0居左, 1居中, 2居右)
	* @param isContinuousPrint   是否继续续打印表格 1：继续续打印  0：不继续打印
	* 备注: 三个参数的数组长度应该一致, 如果colsTextArr[i]的宽度大于colsWidthArr[i], 则文本换行
	* @param callback  执行结果回调
	* @return
	*/
	void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign,int isContinuousPrint, in IPosPrinterCallback callback);

    /**
    * 打印图片
    * @param alignment:	对齐方式 0--居左 , 1--居中, 2--居右, 默认居中
    * @param bitmapSize ： 位图大小，传入大小范围1~16,超出范围默认选择10 单位：24bit
    * @param mBitmap: 	图片bitmap对象(最大宽度384像素，超过无法打印并且回调callback异常函数)
    * @param callback  执行结果回调
    * @return
    */
    void printBitmap(int alignment, int bitmapSize, in Bitmap mBitmap, in IPosPrinterCallback callback);

	/**
	* Print 1D barcode
    * @param data: barcode data
    * @param symbology: barcode type
    * 0-UPC-A,
    * 1-UPC-E,
    * 2-JAN13 (EAN13),
    * 3-JAN8 (EAN8),
    * 4-CODE39,
    * 5-ITF,
    * 6-CODABAR,
    * 7-CODE93,
    * 8-CODE128
    * @param height: bar code height, value is 1 to 16, default is 6 if it is out of range, each unit represents 24 pixels height
    * @param width: bar code width, value is 1 to 16, default is 12 if it is out of range, each unit represents 24 pixels
    * @param textposition: text position 0--do not print text, 1--text above the barcode, 2--text below the barcode, 3--print both above and below the barcode
    * @param callback execution result callback
    * @return
	*/
	void printBarCode(String data, int symbology, int height, int width, int textposition,  in IPosPrinterCallback callback);

    /**
    * Print 2D barcode
    * @param data: QR code data
    * @param modulesize: QR code block size (unit: point, value 1 to 16), default value is 10 if it exceeds the setting range
    * @param mErrorCorrectionLevel: 2D error correction level (0: L 1: M 2: Q 3: H)
    * @param callback execution result callback
    * @return
    */
	void printQRCode(String data, int modulesize, int mErrorCorrectionLevel, in IPosPrinterCallback callback);

	/**
	*打印原始的byte数据
	* @param rawPrintData    Byte 数据数据块
	* @param callback  结果回调
	*/
	void printRawData(in byte[] rawPrintData, in IPosPrinterCallback callback);

	/**
	* 使用ESC/POS指令打印
	* @param data	 指令
	* @param callback  结果回调
	*/
	void sendUserCMDData(in byte[] data, in IPosPrinterCallback callback);

	/**
	* 执行打印
	* 当执行完成各打印功能方法后，需要执行此方法，打印机才能执行打印；
	* 此方法执行之前需要判断打印机状态，当打印机处于PRINTER_NORMAL此方法有效，否则不执行。
	* @param feedlines: 打印并走纸freedlines点行
	* @param callback  结果回调
	*/
	void printerPerformPrint(int feedlines, in IPosPrinterCallback callback);
}
