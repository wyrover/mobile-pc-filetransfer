package com.zsl.file_transfer;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Menu;

public class ComputerActivity extends Activity {

	PcInfoMgr pcInfoMgr = new PcInfoMgr();
	ProgressDialog progressDlg;
	int count;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_computer);
		
		pcInfoMgr.run();
		
/*		progressDlg = new ProgressDialog(ComputerActivity.this);
		progressDlg.setTitle("��������");
		progressDlg.setMessage("�����������ԣ����Ժ�...");
		progressDlg.setCancelable(false);
		progressDlg.show();
		
        new Thread()   
        {  
            public void run()  
            {  
                try  
                {  
                    while (count <= 100)  
                    {   
                        // ���߳������ƽ��ȡ�  
                        progressDlg.setProgress(count++);  
                        Thread.sleep(100);   
                    }  
                    progressDlg.cancel();  
                }  
                catch (InterruptedException e)  
                {  
                	progressDlg.cancel();  
                }  
            }  
    }.start();    
*/
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.computer, menu);
		return true;
	}

}
