package com.zsl.file_transfer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NetworkService extends Service {
    
    // ���͹㲥��Action
    public static String NETWORK_ACTION = "com.zsl.file_transfer.NETWORK_ACTION";
    
    private static int DOWN_FILE_COUNT = 3;
    
    private PcConnect connect;
    private Map<String, PcConnect> connectMap = new HashMap<String, PcConnect>();
    private Future<PcConnect> futureConnect; // ���ڽ����̵߳Ľ��
    private PcinfoCmdFactory cmdfactory;
    private ExecutorService pool;
    private ExecutorService downloadPool;
    
    private Handler handler = new Handler(new DirCmdHandlerCallback());
    
    // �̹߳����࣬�����߳�Ϊ��̨�߳�
    private class DeamonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            // TODO Auto-generated method stub
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            
            return thread;
        }
        
    }
    
    private Binder binder = new NetworkBinder();
    
    
    public NetworkService() {
    }

    public boolean isConnectComputer(String computerName){
        return connectMap.get(computerName) != null;
    }
    
    public void connect(String computerName, int port){
        futureConnect = pool.submit(cmdfactory.newConnectCmd(computerName, port));
        Log.i(this.getClass().getSimpleName(), "proc connect");
    }
    
    public void ls(String computerName, String dir){
        PcConnect con = connectMap.get(computerName);
        if (con == null){
            // ������󣬻�û������
        }
        else{
            pool.submit(cmdfactory.newLsCmd(con, dir));
        }
        
        Log.i(this.getClass().getSimpleName(), String.format("proc ls: %s/%s", computerName, dir));
    }
    
    public void download(String computerName, String fileFullPath, String savePath){
        PcConnect con = connectMap.get(computerName);
        if (con == null){
            // ������󣬻�û������
        }
        else{
            downloadPool.submit(cmdfactory.newDownloadCmd(con, fileFullPath, savePath));
        }
        
        Log.i(this.getClass().getSimpleName(), String.format("proc download: %s/%s", computerName, fileFullPath));
    }
    
    @Override
    public void onCreate(){
        // �����̳߳�
        pool = Executors.newSingleThreadExecutor(new DeamonThreadFactory());
        downloadPool = Executors.newFixedThreadPool(DOWN_FILE_COUNT);
        
        cmdfactory = new PcinfoCmdFactory(handler);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }
    
    // �����Լ���Binder
    public class NetworkBinder extends Binder{
        public NetworkService getService(){
            return NetworkService.this;
        }
    }
    
    
    // �̵߳�handler
    private class DirCmdHandlerCallback implements Handler.Callback{

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case PcinfoCmdFactory.CMD_CONNECTED:
                {
                    Bundle bundle = msg.getData();
                    boolean status = bundle.getBoolean("status");
                    
                    if (status){
                        try {
                            
                            connect = futureConnect.get();
                            String pcname = bundle.getString("pcname");
                            connectMap.put(pcname, connect);
                            
                            // ������ӳɹ�����ô���ǾͿ�ʼ�г�Ŀ¼
                            pool.submit(cmdfactory.newLsCmd(connect, ""));
                            
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    else{
                        String errmsg = bundle.getString("errmsg");
                        System.out.println(errmsg);
                        // ��������ʧ�ܹ㲥
                        Intent intent = new Intent(NETWORK_ACTION);
                        intent.putExtra("action", msg.what);
                        intent.putExtra("data", bundle);
                        
                        sendBroadcast(intent);
                    }
                    
                    break;
                }
                default:
                {
                    Intent intent = new Intent(NETWORK_ACTION);
                    intent.putExtra("action", msg.what);
                    intent.putExtra("data", msg.getData());
                    
                    sendBroadcast(intent);
                    break;
                }
            }
            
            return false;
        }
    }
}
