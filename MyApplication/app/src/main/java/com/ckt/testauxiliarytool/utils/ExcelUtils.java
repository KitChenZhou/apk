package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.autophone.CallRecord;
import com.ckt.testauxiliarytool.batterymonitor.bean.BatteryInfo;
import com.ckt.testauxiliarytool.sensortest.bean.CommendResult;
import com.ckt.testauxiliarytool.sensortest.bean.HSensor;
import com.ckt.testauxiliarytool.sensortest.bean.LSensorTestRecord;
import com.ckt.testauxiliarytool.sensortest.bean.MSensor;
import com.ckt.testauxiliarytool.sensortest.bean.PSensorTestRecord;
import com.ckt.testauxiliarytool.webviewtest.ExcelBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 表格工具类
 */
public final class ExcelUtils {

    public static final String TAG = ExcelUtils.class.getSimpleName();
    //add by gongpeng.wang for webtest in 2017-12-05
    private static String[] title = {"编号", "开始时间", "结束时间", "是否成功", "加载时长（秒）", "是否超时", "失败原因"};
    public static WritableFont arial14font = null;
    private static List<ExcelBean> list;
    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;
    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";

    //end by gongpeng.wang for webtest in 2017-12-05
    private ExcelUtils() {
        throw new UnsupportedOperationException("you can't instantiate ExcelUtils");
    }

    //add by chen shuaian for AutoPhone
    public ExcelUtils(String excelPath) {
        File excelFile = new File(excelPath);
        createExcel(excelFile);
    }

    /**
     * 在appName/BatteryMonitor目录下生成表格文件
     *
     * @param titles    表格的第一行内容，即各列标题
     * @param excelName 生成的表格文件的名字
     * @param data      需要写入表格的数据
     * @throws WriteException 写入表格异常
     * @throws IOException    IO异常
     */
    public static void write(String[] titles, String excelName, List<BatteryInfo> data) throws IOException, WriteException {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext()) + File.separator + MyConstants.ROOT_DIR + File.separator + MyConstants.BATTERY_DIR, excelName + ".xls");
        if (GetRamRomSdUtil.externalMemoryAvailable() && FileUtils.createOrExistsFile(file)) {
            // 创建工作表，一个工作表可添加多个Sheet
            WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);
            // 默认只添加一个Sheet，一个Excel表格可以有多个Sheet
            WritableSheet sheet = writableWorkbook.createSheet(excelName, 0);
            // 设置表格的列宽，第1列20，第6、7、8列15
            sheet.setColumnView(0, 20);
            sheet.setColumnView(5, 15);
            sheet.setColumnView(6, 15);
            sheet.setColumnView(7, 15);
            // 添加表头，第一行
            for (int i = 0; i < titles.length; i++) {
                Label label = new Label(i, 0, titles[i], getHeadCellFormat());
                sheet.addCell(label);
            }
            int size;
            if (data != null && (size = data.size()) > 0) {
                for (int i = 0; i < size; i++) {
                    BatteryInfo batteryInfo = data.get(i);
                    // 生成一系列单元格，从第二行开始，即i+1,共8列数据
                    addCellToSheet(sheet, i + 1, 0, DateTimeUtils.millis2String(batteryInfo.getCurrentMillis()));
                    addCellToSheet(sheet, i + 1, 1, batteryInfo.getLevel() + "%");
                    addCellToSheet(sheet, i + 1, 2, batteryInfo.getTemperature() + "℃");
                    addCellToSheet(sheet, i + 1, 3, batteryInfo.getVoltage());
                    addCellToSheet(sheet, i + 1, 4, batteryInfo.getChargeCurrent());
                    addCellToSheet(sheet, i + 1, 5, batteryInfo.getPlugged());
                    addCellToSheet(sheet, i + 1, 6, batteryInfo.getStatus());
                    addCellToSheet(sheet, i + 1, 7, batteryInfo.getHealth());
                }
            }
            writableWorkbook.write();
            writableWorkbook.close();
        }
    }

    /**
     * 生成普通单元格的格式
     *
     * @return 普通单元格的格式
     * @throws WriteException 写入异常
     */
    private static WritableCellFormat getCommonFormat() throws WriteException {
        // 单元格的格式
        WritableCellFormat format = new WritableCellFormat();
        format.setAlignment(jxl.format.Alignment.CENTRE); // 左右居中
        format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // 上下居中
        return format;
    }

    /**
     * 添加单元格到Sheet中
     *
     * @param sheet   可写入的Sheet表格
     * @param row     行
     * @param column  列
     * @param content 要写入的内容
     * @throws WriteException 写入异常
     */
    private static void addCellToSheet(WritableSheet sheet, int row, int column, String content) throws WriteException {
        Label label = new Label(column, row, content, getCommonFormat());
        sheet.addCell(label);
    }

    /**
     * 表格表头，即第一行的单元格格式
     *
     * @return 表头单元格的格式
     * @throws WriteException 写入异常
     */
    private static CellFormat getHeadCellFormat() throws WriteException {
        // 红色Times 12号字体加粗
        WritableFont writableFont = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD);
        writableFont.setColour(Colour.RED);
        // 单元格的格式
        WritableCellFormat format = new WritableCellFormat(writableFont);
        format.setAlignment(jxl.format.Alignment.CENTRE); // 左右居中
        format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // 上下居中
        return format;
    }

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     * 并将错误的标红
     */
    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.BLUE2);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);
            arial12font = new WritableFont(WritableFont.ARIAL, 10);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setAlignment(Alignment.CENTRE);//设置居中对齐
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN); //设置边框
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Excel
     * add by gongpeng.wang for webtest in 2017-12-05
     *
     * @param fileName
     * @param colName
     */
    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(DateTimeUtils.shortYearFormat(new Date()), 0);
            //创建标题栏
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
                sheet.setColumnView(col, 25);//设置列宽
            }
            sheet.setRowView(0, 340); //设置行高
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * add by gongpeng.wang for webtest in 2017-12-05
     *
     * @param objList  目标list
     * @param fileName 文件名
     * @param c        上下文
     * @param <T>      泛型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean writeObjListToExcel(List<T> objList, String fileName, Context c) {
        boolean result = false;
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName), workbook);
                WritableSheet sheet = writebook.getSheet(0);
                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = (ArrayList<String>) objList.get(j);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(3).equals("失败")) {
                            WritableCell cell = new Label(i, j + 1, list.get(i));
                            //创建一个单元格样式
                            WritableCellFormat writableCellFormat = new WritableCellFormat(arial12font);
                            //设置单元格的样式
                            writableCellFormat.setBorder(jxl.format.Border.ALL, BorderLineStyle.THICK, Colour.RED); //设置边框,并将边框颜色设置为红色
                            //设置样式的背景色为红色
                            writableCellFormat.setAlignment(Alignment.CENTRE);//设置居中对齐
                            cell.setCellFormat(writableCellFormat);
                            sheet.addCell(cell);
                        } else
                            sheet.addCell(new Label(i, j + 1, list.get(i), arial12format));
                        if (list.get(i).length() <= 5) {
                            sheet.setColumnView(i, list.get(i).length() + 8); //设置列宽
                        } else {
                            sheet.setColumnView(i, list.get(i).length() + 5); //设置列宽
                        }
                    }
                    sheet.setRowView(j + 1, 350); //设置行高
                }
                writebook.write();
                Toast.makeText(c, "导出到手机Excel成功", Toast.LENGTH_SHORT).show();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    /**
     * add by gongpeng.wang for webtest in 2017-12-05
     * 将数据集合 转化成ArrayList<ArrayList<String>>
     *
     * @return
     */
    private static ArrayList<ArrayList<String>> getRecordData(List<ExcelBean> list) throws WriteException {
        ArrayList<ArrayList<String>> recordList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ExcelBean reportBean = list.get(i);
            ArrayList<String> beanList = new ArrayList<>();
            beanList.add(reportBean.getId());
            beanList.add(reportBean.getBegin());
            beanList.add(reportBean.getEnd());
            beanList.add(reportBean.getIsSucc());
            beanList.add(reportBean.getTime());
            beanList.add(reportBean.getIsdelay());
            beanList.add(reportBean.getDescrip());
            recordList.add(beanList);
        }
        return recordList;
    }

    public static boolean exportExcel(List<ExcelBean> list, Context context) throws WriteException {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Excel");
        if (!file.exists()) {
            file.mkdirs();
        }
        String _fileName = DateTimeUtils.detailLSHFormat(new Date());
        ExcelUtils.initExcel(file.toString() + "/" + _fileName + ".xls", title);
        String fileName = file.toString() + "/" + _fileName + ".xls";
        return ExcelUtils.writeObjListToExcel(getRecordData(list), fileName, context);
    }


    //add by yonglong.cai for sensortest
    private static String[] mSensorTitles = {"ID", "参考角度(°)", "偏差角度(°)", "测试结果"};
    private static String[] hSensorTitles = {"ID", "保护套状态", "响应时间(ms)", "测试结果"};
    private static String[] gSensorTitles = {"次数", "时间(ms)"};
    private static String[] GyroscopeSensorTitles = {"角度", "响应时间(ms)"};
    private static String[] LSensorTitles = {"ID", "环境", "光照强度/lx", "亮度变化", "响应时间/s"};
    private static String[] PSensorTitles = {"ID", "灭屏时间/ms", "亮屏时间/ms"};

    private static String EXPORT_FAIL = "导出失败";
    private static String EXPORT_SUCCESS = "导出成功，路径:";
    //add by yonglong.cai for sensortest

    /**
     * 创建指南针Excel表格
     * add by yonglong.cai for sensortest
     *
     * @param filename 文件名
     * @param results  结果
     * @return
     */
    public static String createExcelForMSensor(String filename, List<MSensor> results) {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext())
                + File.separator + MyConstants.ROOT_DIR
                + File.separator + MyConstants.SENSOR_DIR,
                filename + ".xls");
        if (FileUtils.createOrExistsFile(file)) {
            Log.d("Excel", "创建成功" + file.getAbsolutePath());
            try {
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(file);
                // 生成名为"第一张工作表"的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet("mSensor", 0);

                //第一行
                for (int i = 0; i < mSensorTitles.length; i++) {
                    //(列,行,值,(样式))
                    Label label = new Label(i, 0, mSensorTitles[i], getHeadStyle());
                    sheet.addCell(label);
                }
                sheet.setColumnView(1, 15);
                sheet.setColumnView(2, 20);

                //其他部分
                for (int i = 0; i < results.size(); i++) {
                    Object[] values = {
                            results.size() - i,
                            results.get(i).getAngle(),
                            results.get(i).getDeviation(),
                            results.get(i).getDeviation() <= 5 ? "pass" : "fail",
                    };

                    for (int j = 0; j < values.length; j++) {
                        CellFormat format = getBodyStyle();
                        if (values[values.length - 1].equals("fail")) {
                            format = getFailStyle();
                        }
                        Label label = new Label(j, i + 1, values[j].toString(), format);
                        sheet.addCell(label);
                    }

                }

                //写入数据并关闭
                book.write();
                book.close();
            } catch (Exception e) {
                e.printStackTrace();
                return EXPORT_FAIL;
            }
            return EXPORT_SUCCESS + file.getAbsolutePath();
        }
        return EXPORT_FAIL;
    }

    /**
     * 创建霍尔传感器Excel表格
     * add by yonglong.cai for sensortest
     *
     * @param filename         文件名
     * @param results          结果
     * @param maxCloseInterval 最大盒盖-皮套应用出现间隔时间
     * @param maxOpenInterval  最大开盖-亮屏间隔时间
     * @return
     */
    public static String createExcelForHSensor(String filename, List<HSensor> results, int maxCloseInterval, int maxOpenInterval) {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext())
                + File.separator + MyConstants.ROOT_DIR
                + File.separator + MyConstants.SENSOR_DIR,
                filename + ".xls");
        if (FileUtils.createOrExistsFile(file)) {
            Log.d("Excel", "创建成功" + file.getAbsolutePath());
            try {
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(file);
                // 生成名为"第一张工作表"的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet(filename, 0);

                //第一行
                for (int i = 0; i < hSensorTitles.length; i++) {
                    //(列,行,值,(样式))
                    Label label = new Label(i, 0, hSensorTitles[i], getHeadStyle());
                    sheet.addCell(label);
                }
                sheet.setColumnView(1, 15);
                sheet.setColumnView(2, 20);

                //其他部分
                for (int i = 0; i < results.size(); i++) {

                    boolean status;
                    if (results.get(i).getStatus().equals("合盖/灭屏")
                            || results.get(i).getStatus().equals("合盖->皮套应用出现")) {
                        status = results.get(i).getInterval() <= maxCloseInterval;
                    } else {
                        status = results.get(i).getInterval() <= maxOpenInterval;
                    }
                    Object[] values = {
                            results.size() - i,
                            results.get(i).getStatus(),
                            results.get(i).getInterval(),
                            status ? "pass" : "fail",
                    };

                    for (int j = 0; j < values.length; j++) {
                        CellFormat format = getBodyStyle();
                        if (!results.get(i).getStatus().equals("开盖->亮屏")) {
                            format = getFailStyle();
                        }
                        //(列,行,值,(样式))
                        Label label = new Label(j, i + 1, values[j].toString(), format);
                        sheet.addCell(label);
                    }
                }

                //写入数据并关闭
                book.write();
                book.close();
            } catch (Exception e) {
                e.printStackTrace();
                return EXPORT_FAIL;
            }
            return EXPORT_SUCCESS + file.getAbsolutePath();
        }
        return EXPORT_FAIL;
    }

    /**
     * 创建加速度传感器  Excel表格
     * add by yonglong.cai for sensortest
     *
     * @param filename 文件名
     * @param results  结果
     * @return
     */
    public static String createExcelForGSensor(String filename, List<CommendResult> results) {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext())
                + File.separator + MyConstants.ROOT_DIR
                + File.separator + MyConstants.SENSOR_DIR, filename + "-" + DateTimeUtils.detailLSHFormat(new Date()) + ".xls");
        if (FileUtils.createOrExistsFile(file)) {
            Log.d("Excel", "创建成功" + file.getAbsolutePath());
            try {
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(file);
                // 生成名为"第一张工作表"的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet(filename, 0);

                //第一行
                for (int i = 0; i < gSensorTitles.length; i++) {
                    //(列,行,值,(样式))
                    Label label = new Label(i, 0, gSensorTitles[i], getHeadStyle());
                    sheet.addCell(label);
                }

                //其他部分
                for (int i = 0; i < results.size(); i++) {
                    CommendResult commendResult = results.get(i);
                    Object[] values = {commendResult.getTimes(), commendResult.getTime()};

                    for (int j = 0; j < values.length; j++) {
                        //(列,行,值,(样式))
                        Label label = new Label(j, i + 1, values[j].toString(), getBodyStyle());
                        sheet.addCell(label);
                    }
                }

                //写入数据并关闭
                book.write();
                book.close();
            } catch (Exception e) {
                e.printStackTrace();
                return EXPORT_FAIL;
            }
            return EXPORT_SUCCESS + file.getAbsolutePath();
        }
        return EXPORT_FAIL;
    }

    /**
     * 创建Gyroscope Excel表格
     * add by yonglong.cai for sensortest
     *
     * @param filename 文件名
     * @param results  结果
     * @return
     */

    public static String createExcelForGyroscope(String filename, List<CommendResult> results) {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext())
                + File.separator + MyConstants.ROOT_DIR
                + File.separator + MyConstants.SENSOR_DIR, filename + "_" + DateTimeUtils.detailLSHFormat(new Date()) + ".xls");
        if (FileUtils.createOrExistsFile(file)) {
            Log.d("Excel", "创建成功" + file.getAbsolutePath());
            try {
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(file);
                // 生成名为"第一张工作表"的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet(filename, 0);

                //第一行
                for (int i = 0; i < GyroscopeSensorTitles.length; i++) {
                    //(列,行,值,(样式))
                    Label label = new Label(i, 0, GyroscopeSensorTitles[i], getHeadStyle());
                    sheet.addCell(label);
                }

                //其他部分
                for (int i = 0; i < results.size(); i++) {
                    CommendResult commendResult = results.get(i);
                    Object[] values = {commendResult.getTimes(), commendResult.getTime()};

                    for (int j = 0; j < values.length; j++) {
                        //(列,行,值,(样式))
                        Label label = new Label(j, i + 1, values[j].toString(), getBodyStyle());
                        sheet.addCell(label);
                    }
                }

                //写入数据并关闭
                book.write();
                book.close();
            } catch (Exception e) {
                e.printStackTrace();
                return EXPORT_FAIL;
            }
            return EXPORT_SUCCESS + file.getAbsolutePath();
        }
        return EXPORT_FAIL;
    }

    /**
     * PSensor的Excel使用方法
     * add by yonglong.cai for sensortest
     *
     * @param filename 文件名
     * @param results  结果
     * @return
     */

    public static String createExcelForPSensor(String filename, List<PSensorTestRecord> results) {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext())
                + File.separator + MyConstants.ROOT_DIR
                + File.separator + MyConstants.SENSOR_DIR, filename + "_" + DateTimeUtils.detailLSHFormat(new Date()) + ".xls");
        if (FileUtils.createOrExistsFile(file)) {
            Log.d("Excel", "创建成功" + file.getAbsolutePath());
            try {
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(file);
                // 生成名为"第一张工作表"的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet(filename, 0);

                //第一行
                for (int i = 0; i < PSensorTitles.length; i++) {
                    //(列,行,值,(样式))
                    Label label = new Label(i, 0, PSensorTitles[i], getHeadStyle());
                    sheet.addCell(label);
                }

                //其他部分
                for (int i = 0; i < results.size(); i++) {
                    PSensorTestRecord pSensorTestRecord = results.get(i);
                    Object[] values = {i + 1, pSensorTestRecord.getScreenOffTime(),
                            pSensorTestRecord.getScreenOnTime()};

                    for (int j = 0; j < values.length; j++) {
                        //(列,行,值,(样式))
                        Label label = new Label(j, i + 1, values[j].toString(), getBodyStyle());
                        sheet.addCell(label);
                    }

                }

                //写入数据并关闭
                book.write();
                book.close();
            } catch (Exception e) {
                e.printStackTrace();
                return EXPORT_FAIL;
            }
            return EXPORT_SUCCESS + file.getAbsolutePath();
        }
        return EXPORT_FAIL;
    }

    /**
     * LSensor的Excel使用方法
     * add by yonglong.cai for sensortest
     *
     * @param filename 文件名
     * @param results  结果
     * @return
     */

    public static String createExcelForLSensor(String filename, List<LSensorTestRecord> results) {
        File file = new File(MyConstants.getStorageRootDir(MyApplication.getContext())
                + File.separator + MyConstants.ROOT_DIR
                + File.separator + MyConstants.SENSOR_DIR, filename + "_" + DateTimeUtils.detailLSHFormat(new Date()) + ".xls");
        if (FileUtils.createOrExistsFile(file)) {
            Log.d("Excel", "创建成功" + file.getAbsolutePath());
            try {
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(file);
                // 生成名为"第一张工作表"的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet(filename, 0);

                //第一行
                for (int i = 0; i < LSensorTitles.length; i++) {
                    //(列,行,值,(样式))
                    Label label = new Label(i, 0, LSensorTitles[i], getHeadStyle());
                    sheet.addCell(label);
                }

                //其他部分
                for (int i = 0; i < results.size(); i++) {
                    LSensorTestRecord lSensorTestRecord = results.get(i);
                    Object[] values = {i + 1, lSensorTestRecord.getTestName(),
                            lSensorTestRecord.getLux(), lSensorTestRecord.getRange(),
                            lSensorTestRecord.getTime()};

                    for (int j = 0; j < values.length; j++) {
                        //(列,行,值,(样式))
                        Label label = new Label(j, i + 1, values[j].toString(), getBodyStyle());
                        sheet.addCell(label);
                    }

                }

                //写入数据并关闭
                book.write();
                book.close();
            } catch (Exception e) {
                e.printStackTrace();
                return EXPORT_FAIL;
            }
            return EXPORT_SUCCESS + file.getAbsolutePath();
        }
        return EXPORT_FAIL;
    }


    /**
     * 获取头部样式
     * add by yonglong.cai for sensortest
     *
     * @return
     * @throws WriteException
     */
    private static WritableCellFormat getHeadStyle() throws WriteException {
        WritableCellFormat cell = new WritableCellFormat();
        cell.setAlignment(Alignment.CENTRE);
        cell.setBackground(Colour.YELLOW);
        cell.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
        return cell;
    }

    /**
     * 获取样式
     * add by yonglong.cai for sensortest
     *
     * @return
     * @throws WriteException
     */
    private static WritableCellFormat getBodyStyle() throws WriteException {
        WritableCellFormat cell = new WritableCellFormat();
        cell.setAlignment(Alignment.CENTRE);
        cell.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
        return cell;
    }

    /**
     * 获取fail记录的样式
     * add by yonglong.cai for sensortest
     *
     * @return
     * @throws WriteException
     */
    private static WritableCellFormat getFailStyle() throws WriteException {
        WritableCellFormat cell = new WritableCellFormat();
        cell.setAlignment(Alignment.CENTRE);
        cell.setBackground(Colour.GREY_25_PERCENT);
        cell.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
        return cell;
    }

    /**
     * 创建Excel表格
     * add by chen shuaian for AutoPhone
     *
     * @param file
     */
    static void createExcel(File file) {
        WritableSheet ws;
        try {
            if (!file.exists()) {
                WritableWorkbook wwb = Workbook.createWorkbook(file);
                ws = wwb.createSheet("sheet1", 0);

                // 在指定单元格插入数据
                Label lbl1 = new Label(0, 0, "ID", getHeader());
                Label lbl2 = new Label(1, 0, "开始时间", getHeader());
                Label lbl3 = new Label(2, 0, "结束时间", getHeader());
                Label lbl4 = new Label(3, 0, "持续时间", getHeader());
                Label lbl5 = new Label(4, 0, "网络状态", getHeader());
                Label lbl6 = new Label(5, 0, "是否连接", getHeader());

                ws.addCell(lbl1);
                ws.addCell(lbl2);
                ws.addCell(lbl3);
                ws.addCell(lbl4);
                ws.addCell(lbl5);
                ws.addCell(lbl6);

                // 从内存中写入文件中
                wwb.write();
                wwb.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将数据存入到Excel表中
     * add by chen shuaian for AutoPhone
     *
     * @param excelFile
     * @param list
     */
    static void writeToExcel(File excelFile, ArrayList<CallRecord> list) {

        try {
            Workbook oldWwb = Workbook.getWorkbook(excelFile);
            WritableWorkbook wwb = Workbook.createWorkbook(excelFile, oldWwb);
            WritableSheet ws = wwb.getSheet(0);

            Label lab1, lab2, lab3, lab4, lab5, lab6;
            WritableCellFormat wcf;
            CallRecord record;
            for (int i = 0; i < list.size(); i++) {
                record = list.get(i);
                if (record.isConnect().equals("pass")) {
                    wcf = getAlignment();
                } else {
                    wcf = getBackground();
                }
                lab1 = new Label(0, i + 1, record.getId(), wcf);
                lab2 = new Label(1, i + 1, record.getStartDate(), wcf);
                lab3 = new Label(2, i + 1, record.getEndDate(), wcf);
                lab4 = new Label(3, i + 1, record.getDuration(), wcf);
                lab5 = new Label(4, i + 1, record.getNetworkType(), wcf);
                lab6 = new Label(5, i + 1, record.isConnect(), wcf);

                ws.addCell(lab1);
                ws.addCell(lab2);
                ws.addCell(lab3);
                ws.addCell(lab4);
                ws.addCell(lab5);
                ws.addCell(lab6);
            }

            // 从内存中写入文件中,只能刷一次.
            wwb.write();
            wwb.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 表格头部样式
     * add by chen shuaian for AutoPhone
     *
     * @return
     */
    private static WritableCellFormat getHeader() {
        WritableFont font = new WritableFont(WritableFont.TIMES, 10,
                WritableFont.BOLD);// 定义字体
        try {
            font.setColour(Colour.BLUE);// 蓝色字体
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
            format.setBorder(Border.ALL, BorderLineStyle.THIN,
                    Colour.BLACK);// 黑色边框
            format.setBackground(Colour.YELLOW);// 黄色背景
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }

    /**
     * 数据显示为fail时的表格背景
     * add by chen shuaian for AutoPhone
     *
     * @return format
     */
    private static WritableCellFormat getBackground() {
        WritableCellFormat format = new WritableCellFormat();
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
            format.setBackground(Colour.RED);// 红色背景
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }

    /**
     * 数据显示为pass时表格背景
     * add by chen shuaian for AutoPhone
     *
     * @return format
     */
    private static WritableCellFormat getAlignment() {
        WritableCellFormat format = new WritableCellFormat();
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }
}
