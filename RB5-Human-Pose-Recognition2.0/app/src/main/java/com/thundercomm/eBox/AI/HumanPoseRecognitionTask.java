package com.thundercomm.eBox.AI;

import android.app.Application;
import android.graphics.Bitmap;

import android.media.Image;

import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;
import com.qualcomm.qti.snpe.Tensor;
import com.thundercomm.eBox.Config.GlobalConfig;
import com.thundercomm.eBox.Constants.Constants;
import com.thundercomm.eBox.Data.PointData;
import com.thundercomm.eBox.Jni;
import com.thundercomm.eBox.Model.RtspItemCollection;
import com.thundercomm.eBox.Utils.LogUtil;
import com.thundercomm.eBox.VIew.HumanPoseRecognitionFragment;
import com.thundercomm.eBox.VIew.PlayFragment;
import com.thundercomm.gateway.data.DeviceData;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;
import java.util.Set;
import java.util.Vector;

import lombok.SneakyThrows;

/**
 * Age Gender Detector
 *
 * @Describe
 */
public class HumanPoseRecognitionTask {

    private static String TAG = "HumanPoseRecognitionTask";

    private HashMap<Integer, NeuralNetwork> mapHumanPoseRecognition = new HashMap<Integer, NeuralNetwork>();

    private HashMap<Integer, DataInputFrame> inputFrameMap = new HashMap<Integer, DataInputFrame>();
    private Vector<HumanPoseRecognitionTaskThread> mHumanPoseRecognitionTaskThreads = new Vector<HumanPoseRecognitionTaskThread>();

    private boolean istarting = false;
    private boolean isInit = false;
    private Application mContext;
    private ArrayList<PlayFragment> playFragments;

    private int frameWidth;
    private int frameHeight;

    private static volatile HumanPoseRecognitionTask _instance;

    private HumanPoseRecognitionTask() {
    }

    public static HumanPoseRecognitionTask getHumanPoseRecognitionTask() {
        if (_instance == null) {
            synchronized (HumanPoseRecognitionTask.class) {
                if (_instance == null) {
                    _instance = new HumanPoseRecognitionTask();
                }
            }
        }
        return _instance;
    }

    public void init( Application context, Vector<Integer> idlist, ArrayList<PlayFragment> playFragments, int width, int height) {
        LogUtil.d(TAG, "init AI");
        frameWidth = width;
        frameHeight = height;
        interrupThread();
        for (int i = 0; i < idlist.size(); i++) {
            if (getHumanPoseRecognitionAlgorithmType(idlist.elementAt(i))) {
                DataInputFrame data = new DataInputFrame(idlist.elementAt(i));
                inputFrameMap.put(idlist.elementAt(i), data);
            }
        }
        mContext = context;
        istarting = true;
        isInit = true;
        this.playFragments = playFragments;
        for (int i = 0; i < idlist.size(); i++) {
            if (getHumanPoseRecognitionAlgorithmType(idlist.elementAt(i))) {
                HumanPoseRecognitionTaskThread humanPoseRecognitionTaskThread = new HumanPoseRecognitionTaskThread(idlist.elementAt(i));
                humanPoseRecognitionTaskThread.start();
                mHumanPoseRecognitionTaskThreads.add(humanPoseRecognitionTaskThread);
            }
        }
    }

    private boolean getHumanPoseRecognitionAlgorithmType(int id) {
        DeviceData deviceData = RtspItemCollection.getInstance().getDeviceList().get(id);
        boolean enable = Boolean.parseBoolean(RtspItemCollection.getInstance().getAttributesValue(deviceData, Constants.ENABLE_HUMANPOSERECOGNITION_STR));
        return enable;
    }

    public void addImgById(int id, final Image img) {
        if (!inputFrameMap.containsKey(id)) {
            return;
        }

        DataInputFrame data = inputFrameMap.get(id);
        data.addImgById(img);
    }

    public void addBitmapById(int id, final Bitmap bmp, int w, int h) {
        if (!inputFrameMap.containsKey(id)) {
            return;
        }

        DataInputFrame data = inputFrameMap.get(id);
        data.org_w = w;
        data.org_h = h;
        data.addBitMapById(bmp);
    }

    public void addMatById(int id, final Mat img, int w, int h) {
        if (!inputFrameMap.containsKey(id)) {
            return;
        }

        DataInputFrame data = inputFrameMap.get(id);
        data.org_w = w;
        data.org_h = h;
        data.addMatById(img);
    }


    class HumanPoseRecognitionTaskThread extends Thread {
        private HumanPoseRecognitionFragment humanPoseRecognitionTask = null;
        private NeuralNetwork network = null;
        Map<String, FloatTensor> outputs = null;
        Map<String, FloatTensor> inputs = null;
        int alg_camid = -1;

        HumanPoseRecognitionTaskThread(int id) {
            alg_camid = id;

            Map<String, int[]> inputDimsMap = new HashMap<>();;
            inputDimsMap.put("image", new int[] {1, 368, 368, 3});
            if (!mapHumanPoseRecognition.containsKey(alg_camid)) {
                try {
                    final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder(mContext)
                            .setInputDimensions(inputDimsMap)
                            .setOutputLayers("net_output")
                            .setRuntimeOrder(NeuralNetwork.Runtime.GPU)
                            .setModel(new File( GlobalConfig.SAVE_PATH + "openpose_body25.dlc"));
                    network = builder.build();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mapHumanPoseRecognition.put(alg_camid, network);
            } else {
                network = mapHumanPoseRecognition.get(alg_camid);
            }
        }

        @SneakyThrows
        @Override
        public void run() {
            super.run();
            Jni.Affinity.bindToCpu(alg_camid % 4 + 4);
            humanPoseRecognitionTask = (HumanPoseRecognitionFragment) playFragments.get(alg_camid);
            DataInputFrame inputFrame = inputFrameMap.get(alg_camid);
            Mat rotateimage = new Mat(frameHeight, frameWidth, CvType.CV_8UC4);
            Mat resizeimage = new Mat(frameHeight, frameWidth, CvType.CV_8UC4);
            Mat frameBgrMat = new Mat(frameHeight, frameWidth, CvType.CV_8UC3);
            float[] matData = new float[frameHeight * frameWidth * 3];
            float[] matDataTemp = new float[frameHeight * frameWidth * 3];
            int height  = 0, width = 0;

            LogUtil.d("", "debug test start camid  " + alg_camid);
            Set<String> inputNames = network.getInputTensorsNames();
            final String inputLayer = inputNames.iterator().next();

            final FloatTensor tensor = network.createFloatTensor(
                    network.getInputTensorsShapes().get(inputLayer));
            while (istarting) {
                try {
                    inputFrame.updateFaceRectCache();
                    Mat mat = inputFrame.getMat();
                    if (!OPencvInit.isLoaderOpenCV() || mat == null) {
                        if (mat != null) mat.release();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    height = mat.height();
                    width = mat.width();
                    Core.flip(mat, rotateimage, 0);

                    PointData mPoint = new PointData();
                    Imgproc.resize(rotateimage, resizeimage, new Size(frameHeight, frameWidth));
                    Imgproc.cvtColor(resizeimage, frameBgrMat, Imgproc.COLOR_RGBA2BGR);

                    frameBgrMat = Dnn.blobFromImage(frameBgrMat, 1.0 / 255 , new Size(368, 368), new Scalar(0, 0, 0), false, false);

                    frameBgrMat = frameBgrMat.reshape(3, new int [] {368, 368});

                    frameBgrMat.get(0,0, matData);
                    // 3x368x368 => 368x368x3
                    for (int i = 0; i < 3 ; i++) {
                        for (int j = 0; j < 368 ; j++) {
                            for (int z = 0; z < 368; z++) {
                                matDataTemp[z * 368 * 3 + j * 3 + i] = matData[i * 368 * 368  + j * 368 + z];
                            }
                        }
                    }

                    for (int i = 0; i < 368 ; i++) {
                        for (int j = 0; j < 368 ; j++) {
                            for (int z = 0; z < 3; z++) {
                                matData[j * 368 * 3 + i * 3 + z] = matDataTemp[i * 368 * 3 + j * 3 + z];
                            }
                        }
                    }
                    if (mat != null) mat.release();
                    tensor.write(matData, 0, matData.length);
                    inputs = new HashMap<>();
                    inputs.put(inputLayer, tensor);
                    outputs = network.execute(inputs);
                    boolean drawSkeleton = true;
                    for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
                        FloatTensor outputTensor = output.getValue();
                        final float[] array = new float[outputTensor.getSize()];
                        outputTensor.read(array, 0, array.length);

                        float temp = 0.0f, prob = 0.0f;
                        int x = 0, y =0;
                        for (int i =0; i < 25; i++) {
                            for (int j =0; j < 46; j++) {
                                for (int z =0; z < 46; z++) {
                                    temp = array[j * 46 * 78 + z * 78 + i];
                                    if (temp > prob) {
                                        prob = temp;
                                        y = j;
                                        x = z;
                                    }
                                }
                            }

                            if (prob < 0.1f) {
                                drawSkeleton = false;
                                break;
                            }
                            mPoint.setValue(x * width / 46, y * height / 46, prob, i);
                            prob = 0.0f;
                        }
                    }
                    if (drawSkeleton) {
                        postObjectDetectResult(alg_camid, mPoint);
                    } else {
                        postNoResult(alg_camid);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "Exception!");
                }
            }
            releaseTensors(inputs, outputs);
        }


        private final void releaseTensors(Map<String, ? extends Tensor>... tensorMaps) {
            for (Map<String, ? extends Tensor> tensorMap: tensorMaps) {
                for (Tensor tensor: tensorMap.values()) {
                    tensor.release();
                }
            }
        }

        private void postNoResult(int id) {
            if (humanPoseRecognitionTask != null) {
                humanPoseRecognitionTask.OnClean();
            }
        }

        private void postObjectDetectResult(int id, PointData mPoint) {
            if (humanPoseRecognitionTask != null) {
                humanPoseRecognitionTask.Ondraw(mPoint);
            }
        }
    }

    //Mat转Bitmap
    public static Bitmap matToBitmap(Mat mat) {
        Bitmap resultBitmap = null;
        if (mat != null) {
            resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            if (resultBitmap != null)
                Utils.matToBitmap(mat, resultBitmap);
        }
        return resultBitmap;
    }

    public void closeService() {

        isInit = false;
        istarting = false;

        System.gc();
        System.gc();
    }

    private void interrupThread() {
        for (HumanPoseRecognitionTaskThread humanPoseRecognitionTaskThread : this.mHumanPoseRecognitionTaskThreads) {
            if (humanPoseRecognitionTaskThread != null && !humanPoseRecognitionTaskThread.isInterrupted()) {
                humanPoseRecognitionTaskThread.interrupt();
            }
        }
        mapHumanPoseRecognition.clear();
    }

    public boolean isIstarting() {
        return isInit;
    }
}
