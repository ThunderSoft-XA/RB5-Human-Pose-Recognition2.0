package com.thundercomm.eBox.Utils;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    private static String TAG = "FileUtil==>";
    private static String FILEPATH = "testVideo";
    private static String FILENAME = "test1.mp4";
    private static String FILENAME2 = "test2.mp4";
    private Context mContext;

    private static FileUtil instatce;

    public static FileUtil getInstance(Context context) {
        if (instatce == null) {
            if (context != null) {
                instatce = new FileUtil(context);
            }
        }
        return instatce;
    }

    private FileUtil(Context context) {
        mContext = context;
    }

    public File getTestFile() {
        String filePathStr = mContext.getFilesDir().getPath() + "/" + FILEPATH;
        File filePath = new File(filePathStr);
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        File videoFile = new File(filePath + "/" + FILENAME);
        LogUtil.i(TAG, "FilePath -->" + videoFile + " find file :" + videoFile.exists());
        if (videoFile.exists()) {
            return videoFile;
        } else return null;
    }

    public File getTestFile2() {
        String filePathStr = mContext.getFilesDir().getPath() + "/" + FILEPATH;
        File filePath = new File(filePathStr);
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        File videoFile = new File(filePath + "/" + FILENAME2);
        LogUtil.i(TAG, "FilePath -->" + videoFile + " find file :" + videoFile.exists());
        if (videoFile.exists()) {
            return videoFile;
        } else return null;
    }

    public void writeTestVideo() {
        try {
            InputStream inputStream = mContext.getResources().getAssets().open("test1.mp4");
            if (inputStream != null) {
                String filePathStr = mContext.getFilesDir().getPath() + "/" + FILEPATH;
                File filePath = new File(filePathStr);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                File videoFile = new File(filePath + "/" + FILENAME);
                FileOutputStream fos = new FileOutputStream(videoFile);
                byte[] buffer = new byte[1024 * 10];
                int count = 0;
                while ((count = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                inputStream.close();
                LogUtil.i(TAG, "writeTestVideo -->> write file success.a ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e(TAG, "writeTestVideo -->> write file filed. ");
        }
    }

    public void writeTestVideo2() {
        try {
            InputStream inputStream = mContext.getResources().getAssets().open("test1.mp4");
            if (inputStream != null) {
                String filePathStr = mContext.getFilesDir().getPath() + "/" + FILEPATH;
                File filePath = new File(filePathStr);
                if (!filePath.exists()) {
                    filePath.mkdir();
                }
                File videoFile = new File(filePath + "/" + FILENAME2);
                FileOutputStream fos = new FileOutputStream(videoFile);
                byte[] buffer = new byte[1024 * 10];
                int count = 0;
                while ((count = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                inputStream.close();
                LogUtil.i(TAG, "writeTestVideo -->> write file success.a ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e(TAG, "writeTestVideo -->> write file filed. ");
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param filePath ????????????????????????????????????data/data/com.test/files
     * @param fileName ?????????????????????????????????abc.txt
     * @param string   ???????????????????????????
     */
    public void write(String filePath, String fileName, String string) throws IOException {
        File file = new File(filePath);

        // ?????????????????????????????????
        if (!file.exists()) {
            if (!file.mkdirs()) {   // ?????????????????????????????????
            }
        } else {
            File fileWrite = new File(filePath + File.separator + fileName);

            // ??????????????????????????????
            if (!fileWrite.exists()) {
                if (!fileWrite.createNewFile()) {   // ??????????????????????????????
                    return;
                }
            }
            // ?????????????????????????????????
            FileOutputStream fileOutputStream = new FileOutputStream(fileWrite);

            // ????????????
            fileOutputStream.write(string.getBytes());

            // ?????????????????????
            fileOutputStream.flush();

            // ???????????????
            fileOutputStream.close();
        }
    }
}
