package com.bob.aidltest.aidl;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_Client";
    private static final int MESSAGE_QUERY_STUDENTLIST=1;
    private int student_size = 3;

    private IStudentManager mRemoteStudentManager;

    private ServiceConnection mConnection=new ServiceConnection() {
        //onServiceConnected与onServiceDisconnected都是在主线程中的，所以如果里面如果涉及到服务端的耗时操作那么需要在子线程中进行
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取到IStudentManager对象
            final IStudentManager studentManager =IStudentManager.Stub.asInterface(service);
            mRemoteStudentManager = studentManager;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteStudentManager = null;
            Log.d(TAG, "onServiceDisconnected.threadname:" + Thread.currentThread().getName());

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Student.name = "JACK";
        Log.d("MainActivity:Sname=", Student.name);
        Intent intent = new Intent(this, StudentManagerService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_QUERY_STUDENTLIST:
                    Toast.makeText(MainActivity.this, msg.obj.toString(),Toast.LENGTH_SHORT).show();
                default:
                    super.handleMessage(msg);
            }
        }
    };


    /**
     * 在客户端向服务端添加一名学生
     * @param view
     */
    public void addStudent(View view) {
        if (mRemoteStudentManager != null) {
            try{
                int student_id = student_size+ 1;
                Student newStudent;
                if (student_id % 2 == 0) {
                    newStudent= new Student(student_id, "新学生" + student_id, "man");
                } else {
                    newStudent= new Student(student_id, "新学生" + student_id, "woman");
                }
                mRemoteStudentManager.addStudent(newStudent);
                ++student_size;
                Log.d(TAG, "添加一位学生：" + newStudent.toString());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 在客户端向服务端发起查询学生的请求
     * @param view
     */
    public void get_student_list(View view) {
        Toast.makeText(this, "正在获取学生列表", Toast.LENGTH_SHORT).show();
        //由于服务端的查询操作是耗时操作，所以客户端需要开启子线程进行工作
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mRemoteStudentManager != null) {
                    try{
                        final List<Student> students = mRemoteStudentManager.getStudentList();
                        student_size = students.size();
                        Log.d(TAG, "从服务器成功获取到学生列表:" + students.toString());
                        mHandler.obtainMessage(MESSAGE_QUERY_STUDENTLIST, students).sendToTarget();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 跳转到SecondActivity
     * @param view
     */
    public void toSecondActivity(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

}
