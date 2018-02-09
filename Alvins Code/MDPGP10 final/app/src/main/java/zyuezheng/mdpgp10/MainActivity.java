package zyuezheng.mdpgp10;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    private static final String TAG = "MainActivity : ";
    private static final String DEBUGMDF = "DebugMDF : ";

    // Msg type sent from the Bluetooth Service Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the Bluetooth Service Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BLUETOOTH = 3;

    private String mConnectedDevice= "";
    private BluetoothAdapter btAdapter = null;
    private Bluetooth chatService = null;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private Handler customerHandler = new Handler();
    private Arena arena;

    private String gridString = "GRID 15 20 3 2 2 2 0 0 0 0 0 0 0"; // rows, cols, head center, body center, ..
    private int[] intArray = new int[300];

    // AMD
    private ListView tConversationView;
    private ListView fConversationView;
    private Button tSendButton;
    private ArrayAdapter<String> tConversationAA;
    private ArrayAdapter<String> fConversationAA;

    String decodeString;
    // f1, f2 configuration
    SharedPreferences preferences;
    Handler mMyHandler = new Handler();
    TextView robotStatus, exploreTime, fastestTime;
    TextView  x_coordinate;
    TextView y_coordinate, direction;
    EditText TextAMD;
    ToggleButton autoManual, explore, fastest;
    ToggleButton tiltSensing;
    Button update;
    Button f1, f2;
    ImageButton up, left, right;
    RelativeLayout arenaDisplay;

    boolean autoUpdate = true;
    boolean tilt = false;
    int[][] obstacleArray = new int[15][20];
    ArrayList obstacleSensor = new ArrayList();
    private long startTimeExplore = 0L;
    private long startTimeFastest = 0L;
    long timeBuffExplore = 0L;
    long timeBuffFastest = 0L;
    long timeInMillisecondsExplore = 0L;
    long timeInMillisecondsFastest = 0L;
    long updateTimeExplore = 0L;
    long updateTimeFastest = 0L;
    StringBuffer outStringBuffer;
    JSONObject jsonObj;

    // fastest path
    String dir = "";
    int run = 0;

    // robot default position
    int xStatus = 2;
    int yStatus = 2;
    int dStatus = 90;
    private Button startBtn;
    private SensorManager SM;
    private boolean isShowMDF=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth is unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tilt sensor
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Preferences for reconfiguration
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        robotStatus = (TextView)findViewById(R.id.text_robotStatus);

        exploreTime = (TextView)findViewById(R.id.timer_explore);
        fastestTime = (TextView)findViewById(R.id.timer_fastest);

        // Robot position and orientation
        x_coordinate = (TextView)findViewById(R.id.coord_x);
        y_coordinate = (TextView)findViewById(R.id.coord_y);
        direction = (TextView)findViewById(R.id.direction);

        TextAMD = (EditText)findViewById(R.id.send_text);
        autoManual = (ToggleButton)findViewById(R.id.btn_automanual);
        update = (Button)findViewById(R.id.btn_update);
        explore = (ToggleButton)findViewById(R.id.btn_explore);
        fastest = (ToggleButton)findViewById(R.id.btn_fastest);
        tiltSensing = (ToggleButton)findViewById(R.id.tilt_btn);

        f1 = (Button)findViewById(R.id.btn_f1);
        f2 = (Button)findViewById(R.id.btn_f2);

        up = (ImageButton)findViewById(R.id.btn_up);
        left = (ImageButton)findViewById(R.id.btn_left);
        right = (ImageButton)findViewById(R.id.btn_right);

        startBtn = (Button) findViewById(R.id.btn_start);


        // Initializing environment
        init();

        // Setup onClick listeners
        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "F1 clicked");
                sendMessage(preferences.getString("F1String", ""));
            }
        });

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "F2 clicked");
                sendMessage(preferences.getString("F2String", ""));
            }
        });

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Up arrow clicked");
                goStraight();
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Left arrow clicked");
                turnLeft();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Right arrow clicked");
                turnRight();
            }
        });

        // Update button can only be used when the auto button is set to off
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(obstacleArray != null){
                        autoUpdate = true;
                        arena.setObstacles(obstacleArray);
                    }
                    if(decodeString != null){
                        autoUpdate = true;
                        System.out.println(decodeString);
                        updateGridArray(toIntArray(decodeString));
                    }
                    autoUpdate = false;
                    Toast.makeText(MainActivity.this, "Map Updated", Toast.LENGTH_SHORT).show();

                } catch(Exception e){
                    Toast.makeText(MainActivity.this, "Already Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(autoUpdate){
            autoManual.setChecked(true);
        }

        // lister for all buttons
        autoManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){   // Auto
                    update.setEnabled(false);
                    update.setBackgroundColor(Color.parseColor("#efa1bfc7"));
                    autoUpdate = true;
                    Toast.makeText(MainActivity.this, "Auto ON", Toast.LENGTH_SHORT).show();
                }
                else{           // Manual
                    autoUpdate = false;
                    update.setEnabled(true);
                    update.setBackgroundResource(R.drawable.enabled_btn);
                    Toast.makeText(MainActivity.this, "Manual ON", Toast.LENGTH_SHORT).show();
                }
            }
        });

        explore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    sendMessage("$go"); //ExploreCommand
                    startTimeExplore = SystemClock.uptimeMillis();
                    customerHandler.post(updateTimerThreadExplore);
                }
                else{
                    timeBuffExplore += timeInMillisecondsExplore;
                    customerHandler.removeCallbacks(updateTimerThreadExplore);
                }
            }
        });

        fastest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    sendMessage("$fp");
                    startTimeFastest = SystemClock.uptimeMillis();
                    customerHandler.post(updateTimerThreadFastest);
                }
                else{
                    timeBuffFastest += timeInMillisecondsFastest;
                    customerHandler.removeCallbacks(updateTimerThreadFastest);
                }
            }
        });

        tiltSensing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    onResume();
                    tilt = true;
                }
                else{
                    onPause();
                    tilt = false;
                }
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if((chatService == null) || (chatService.getState() != Bluetooth.STATE_CONNECTED)){
                    Toast.makeText(MainActivity.this, "Bluetooth Not Connected", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendMessage("pstart");
                }
            }
        });

    }

    // initialise the whole android environment
    private void init(){
        // default value for the robot position
        gridString = "GRID 15 20 3 2 2 2 0 0 0 0 0 0 0";
        x_coordinate.setText("2", TextView.BufferType.EDITABLE);
        y_coordinate.setText("2", TextView.BufferType.EDITABLE);
        direction.setText("90");
        intArray = toIntArray(gridString);
        Log.e(TAG, "int array: " + intArray);
        arena = new Arena(this);
        arena.setClickable(false);
        arena.setGridArray(intArray);
        arena.setObstacles(obstacleArray);
        for(int x = 0; x < 15; x++){
            for(int y = 0; y < 20; y++){
                obstacleArray[x][y] = 0;
            }
        }

        arenaDisplay = (RelativeLayout) findViewById(R.id.arenaView);
        arenaDisplay.addView(arena);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.e(TAG, "-- onStart --");
        if(!btAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
            Toast.makeText(getApplicationContext(), "Disabled bluetooth", Toast.LENGTH_SHORT).show();
        }
        else{
            if(chatService == null){
                setupChat();
            }
            Toast.makeText(getApplicationContext(),"Enabled bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupChat(){
        Log.d(TAG, "setupChat()");
        // initialise the array for chat
        tConversationAA = new ArrayAdapter<String>(this, R.layout.text);
        fConversationAA = new ArrayAdapter<String>(this, R.layout.text);

        tConversationView = (ListView) findViewById(R.id.listView_to);
        tConversationView.setAdapter(tConversationAA);
        fConversationView = (ListView) findViewById(R.id.listView_from);
        fConversationView.setAdapter(fConversationAA);

        tSendButton = (Button) findViewById(R.id.send_btn);
        tSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = TextAMD.getText().toString();
                sendMessage(msg);
                TextAMD.setText("");
            }
        });

        // initiate the bluetooth service for connections
        chatService = new Bluetooth(this, mHandler);

        // initiate the buffer for outgoing msg
        outStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onResume(){
        Log.d(TAG, "-- onResume --");
        super.onResume();
        // Resume the BT when it first fail onStart()
        if (chatService != null) {
            if (chatService.getState() == Bluetooth.STATE_IDLE) {
                // Start the Bluetooth chat services
                chatService.start();
            }
        }
        // Resume Tilt Sensor
       SM.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        Log.d(TAG, "-- onPause --");
        super.onPause();
        // sensor on pause
        SM.unregisterListener(this);
    }

    @Override
    public void onStop(){
        Log.d(TAG, "-- onStop -- ");
        super.onStop();
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "-- onDestroy --");
        super.onDestroy();
        if(chatService != null){
            chatService.stop();
        }
    }

    private void allowDiscoverable(){
        Log.d(TAG, "allowDiscoverable");
        if(btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent requireDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            requireDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(requireDiscoverable);
        }
    }

    private void sendMessage(String string) {
        Log.d(TAG, "sendMessage(): " + string);
        // Check that we're actually connected before trying anything
        if(chatService.getState() != Bluetooth.STATE_CONNECTED){
            Toast.makeText(this, "Bluetooth Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if(string.length() > 0){
            string += "\n";
            byte[] msgSend = string.getBytes();
            chatService.write(msgSend);
            // reset buffer to zero
            outStringBuffer.setLength(0);
        }
    }

    private Runnable updateTimerThreadExplore = new Runnable() {
        @Override
        public void run() {
            timeInMillisecondsExplore = SystemClock.uptimeMillis() - startTimeExplore;
            updateTimeExplore = timeBuffExplore + timeInMillisecondsExplore;

            int sec = (int) (updateTimeExplore/1000);
            int min = sec/60;
            sec %= 60;
            int millisecond = (int) (updateTimeExplore % 1000);
            int milli = millisecond / 10;
            if(min < 10){
                exploreTime.setText("0" + min + ":" + sec + ":" + milli);
            }
            else{
                exploreTime.setText(min + ":" + sec + ":" + milli);
            }
            customerHandler.post(this);
        }
    };

    private Runnable updateTimerThreadFastest = new Runnable() {
        @Override
        public void run() {
            timeInMillisecondsFastest = SystemClock.uptimeMillis() - startTimeFastest;
            updateTimeFastest = timeBuffFastest + timeInMillisecondsFastest;

            int sec = (int) (updateTimeFastest/1000);
            int min = sec/60;
            sec %= 60;
            int millisecond = (int) (updateTimeFastest % 1000);
            int milli = millisecond / 10;
            if(min < 10){
                fastestTime.setText("0" + min + ":" + sec + ":" + milli);
            }
            else{
                fastestTime.setText(min + ":" + sec + ":" + milli);
            }
            customerHandler.post(this);
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            robotStatus.setText("Stopped");
        }
    };

    public void goStraight(){
        sendMessage("#w");

        try {
            decodeString = decodeRobotString_algo("{go:[F]}");
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}

        // 1 sec later, set robot status back to stopped
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public void turnLeft(){
        sendMessage("#a");
        try {
            decodeString = decodeRobotString_algo("{go:[L]}");
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public void turnRight(){
        sendMessage("#d");
        try {
            decodeString = decodeRobotString_algo("{go:[R]}");
            if(decodeString != null)
                updateGridArray(toIntArray(decodeString));
        } catch (JSONException e){}
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public void alignRobot(View view){
        sendMessage("#l");
        robotStatus.setText("Calibrating");
        mMyHandler.postDelayed(mRunnable, 1000);
    }

    public int[] toIntArray(String s){
        Log.d("toIntArray()", s);
        String[] stringArray = s.split(" ");
        int len = stringArray.length-1;
        int[] intArray = new int[len];

        for(int i = 1; i < len; i++){
            intArray[i-1] = Integer.parseInt(stringArray[i]);
        }
        return intArray;
    }


    private final void setStatus(CharSequence subTitle) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //final ActionBar actionBar = this.getActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    private String obstacleMdfStr2="";
    private String obstacleMdfStr1="";
    // the handler that gets info back from the Bluethooth Chat Service
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case Bluetooth.STATE_CONNECTED:
                            setStatus("Connected to " + mConnectedDevice);
                            tConversationAA.clear();
                            break;
                        case Bluetooth.STATE_CONNECTING:
                            setStatus("Connecting");
                            break;
                        case Bluetooth.STATE_LISTEN:
                        case Bluetooth.STATE_IDLE:
                            setStatus("Disconnected");
                            break;
                    }
                    break;

                case MESSAGE_READ:
                    byte[] read = (byte[]) msg.obj;
                    String readMsg = new String(read);  // byte[]; offset; byteCount
                    Log.e("DebugGrid", "readMsg: " + readMsg);

                    char letter = readMsg.charAt(0);
                    if (letter == 'w'){
                        Log.e(TAG,"letter == w");
                        try{
                            fConversationAA.add(mConnectedDevice + " : " + readMsg);
                            decodeString = decodeStringDirection(readMsg);
                            Log.e(TAG,"Bluetooth msg: " + readMsg);
                            if(decodeString != null)
                                updateGridArray(toIntArray(decodeString));
                        } catch(JSONException e){   // json generating error
                            e.printStackTrace();
                        }
                    }
                    else if (letter == 'a'){
                        Log.e(TAG,"letter == a");
                        try{
                            fConversationAA.add(mConnectedDevice + " : " + readMsg);
                            decodeString = decodeStringDirection(readMsg);
                            Log.e(TAG,"Bluetooth msg: " + readMsg);
                            if(decodeString != null)
                                updateGridArray(toIntArray(decodeString));
                        } catch(JSONException e){   // json generating error
                            e.printStackTrace();
                        }
                    }
                    else if (letter == 'd'){
                        Log.e(TAG,"letter == d");
                        try{
                            fConversationAA.add(mConnectedDevice + " : " + readMsg);
                            decodeString = decodeStringDirection(readMsg);
                            Log.e(TAG,"Bluetooth msg: " + readMsg);
                            if(decodeString != null)
                                updateGridArray(toIntArray(decodeString));
                        } catch(JSONException e){   // json generating error
                            e.printStackTrace();
                        }
                    }
                    else if(readMsg.contains("go")){
                        try{
                            fConversationAA.add(mConnectedDevice + " : " + readMsg);
                            decodeString = decodeRobotString_algo(readMsg);
                            Log.e(TAG,"Bluetooth msg: " + readMsg);
                            if(decodeString != null)
                                updateGridArray(toIntArray(decodeString));
                        } catch(JSONException e){   // json generating error
                            e.printStackTrace();
                        }
                    }
                    else if(readMsg.contains("grid")){
                        Log.e(TAG,"grid readMsg:" + readMsg);
                        try{
                            // the readMessage should already in a hex format
                            fConversationAA.add(mConnectedDevice + " : " + readMsg);
                            obstacleArray = decodeObstacleMsg(readMsg.substring(0,75+4));
                            obstacleMdfStr1="1: " + convertHexToMDF1(readMsg);
                            obstacleMdfStr2 = "2: " + convertHexToMDF2(readMsg);

                            Log.e(DEBUGMDF,"obstacleMdfStr1: "  + obstacleMdfStr1);
                            Log.e(DEBUGMDF,"obstacleMdfStr2: "  + obstacleMdfStr2);

                            updateObstacleArray(obstacleArray);
                            Log.e(TAG,"Bluetooth msg: "  + readMsg);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        fConversationAA.add(mConnectedDevice + " : " + readMsg);
                        Log.e(TAG,"Bluetooth msg: " + readMsg);
                        decodeString = readMsg;
                    }
                    chatService.setMsg(true);
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    tConversationAA.add("Group 10:  " + writeMessage);
                    Toast.makeText(MainActivity.this, "SEND", Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_DEVICE_NAME:
                    mConnectedDevice = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected with " + mConnectedDevice, Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public String decodeRobotString_algo(String s) throws JSONException{
        jsonObj = new JSONObject(s);
        //# arduino
        //$ pc
        //% all
        String decode = jsonObj.getString("go");
        Log.e(TAG,"decode: "+decode);
        Log.e(TAG, "jsonObj: " + decode);
        decode = decode.replace("[","");
        Log.e(TAG, "jsonObj: " + decode);
        decode = decode.replace("]","");
        Log.e(TAG, "jsonObj: " + decode);
        int robotX = xStatus; //2
        int robotY = yStatus; //2
        int robotD = dStatus; //90
        Log.e(TAG, "-------------------------");
        Log.e(TAG, "robotX: " + robotX);
        Log.e(TAG, "robotY: " + robotY);
        Log.e(TAG, "robotD: " + robotD);
        // move forward
        if(decode.equals("\"F\"")){
            switch(dStatus){
                case 0:
                    robotY = yStatus - 1;
                    break;
                case 90:
                    robotX = xStatus + 1;
                    break;
                case 180:
                    robotY = yStatus + 1;
                    break;
                case 270:
                    robotX = xStatus - 1;
                    break;
            }
        }
        // turn left
        if(decode.equals("\"L\"")){
            switch(dStatus){
                case 0:
                    robotD = 270;
                    break;
                case 90:
                    robotD = 0;
                    break;
                case 180:
                    robotD = 90;
                    break;
                case 270:
                    robotD = 180;
                    break;
            }
        }
        // turn right
        if(decode.equals("\"R\"")){
            switch(dStatus){
                case 0:
                    robotD = 90;
                    break;
                case 90:
                    robotD = 180;
                    break;
                case 180:
                    robotD = 270;
                    break;
                case 270:
                    robotD = 0;
                    break;
            }
        }
        // check robot out of bound
        if(robotX < 2 || robotX > 19 || robotY < 2 || robotY > 14)
            return null;
        return decodeRobotString_(robotX, robotY, robotD);
    }


    public String decodeStringDirection(String s) throws JSONException{

        String decode = s.substring(0,1);
        Log.e(TAG,"decode: "+decode);
        Log.e(TAG, "jsonObj: " + decode);
        int robotX = xStatus; //2
        int robotY = yStatus; //2
        int robotD = dStatus; //90
        Log.e(TAG, "-------------------------");
        Log.e(TAG, "robotX: " + robotX);
        Log.e(TAG, "robotY: " + robotY);
        Log.e(TAG, "robotD: " + robotD);
        // move forward
        if(decode.equals("w")){
            Log.e(TAG, "decodeStringDirection w");
            switch(dStatus){
                case 0:
                    robotY = yStatus - 1;
                    break;
                case 90:
                    robotX = xStatus + 1;
                    break;
                case 180:
                    robotY = yStatus + 1;
                    break;
                case 270:
                    robotX = xStatus - 1;
                    break;
            }
        }
        // turn left
        if(decode.equals("a")){
            Log.e(TAG, "decodeStringDirection a");
            switch(dStatus){
                case 0:
                    robotD = 270;
                    break;
                case 90:
                    robotD = 0;
                    break;
                case 180:
                    robotD = 90;
                    break;
                case 270:
                    robotD = 180;
                    break;
            }
        }
        // turn right
        if(decode.equals("d")){
            Log.e(TAG, "decodeStringDirection d");
            switch(dStatus){
                case 0:
                    robotD = 90;
                    break;
                case 90:
                    robotD = 180;
                    break;
                case 180:
                    robotD = 270;
                    break;
                case 270:
                    robotD = 0;
                    break;
            }
        }
        // check robot is out of bound
        if(robotX < 2 || robotX > 19 || robotY < 2 || robotY > 14)
            return null;
        return decodeRobotString_(robotX, robotY, robotD);
    }

    // AMD json decoding
    public String decodeRobotString(String s)throws JSONException{
        jsonObj = new JSONObject(s);
        String decode = jsonObj.getString("go");
        decode = decode.replace("[", "");
        decode = decode.replace("]", "");
        String array[] = decode.split(",");
        Integer robotX = Integer.parseInt(array[0]);
        Integer robotY = Integer.parseInt(array[1]);
        Integer robotD = Integer.parseInt(array[2]);

        return decodeRobotString_(robotX, robotY, robotD);
    }



    public String decodeRobotString_(int newX, int newY, int newDirection){
        String decode = "";
        String hx = "";
        String hy = "";
        String bx = String.valueOf(newX);
        String by = String.valueOf(newY);

        x_coordinate.setText(newX+"");
        y_coordinate.setText(newY+"");

        if (newDirection == 0){
            hx = String.valueOf(newX);
            hy = String.valueOf(newY-1);

            direction.setText("0");

            if(dStatus == 90){
                robotStatus.setText("Turn Left");
            }
            if(dStatus == 270){
                robotStatus.setText("Turn Right");
            }
            if(newY < yStatus){
                robotStatus.setText("Moving Foward");
            }
        }
        else if (newDirection == 90){
            hx = String.valueOf(newX+1);
            hy = String.valueOf(newY);

            direction.setText("90");
            if(dStatus == 0){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 180){
                robotStatus.setText("Turn Left");
            }
            if(newX > xStatus){
                robotStatus.setText("Moving Foward");
            }
        }
        else if (newDirection == 180){
            hx = String.valueOf(newX);
            hy = String.valueOf(newY+1);

            direction.setText("180");
            if(dStatus == 90){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 270){
                robotStatus.setText("Turn Left");
            }
            if(newY > yStatus){
                robotStatus.setText("Moving Foward");
            }
            if(newY < yStatus){
                robotStatus.setText("Moving Backward");
            }
        }
        else if (newDirection == 270){
            hx = String.valueOf(newX-1);
            hy = String.valueOf(newY);
            direction.setText("270");
            if(dStatus == 180){
                robotStatus.setText("Turn Right");
            }
            if(dStatus == 0){
                robotStatus.setText("Turn Left");
            }
            if(newX < xStatus){
                robotStatus.setText("Moving Foward");
            }
            if(newX > xStatus){
                robotStatus.setText("Moving Backward");
            }

        }
        // delay the status updating by 1 sec
        mMyHandler.postDelayed(mRunnable, 1000);
        decode = "GRID 15 20 " + hx + " " + hy + " " + bx + " " + by + " 0 0 0 0 0 0 0 0";

        xStatus = newX;
        yStatus = newY;
        dStatus = newDirection;
        Log.d(TAG, "Grid decode: " + decode);
        return decode;
    }


    // string with obstacle values
    public int[][] decodeObstacleMsg(String s)throws JSONException{
        Log.d(TAG, "decodeObstacleMsg: " + s);
        s = s.replace("grid", "");
        //s = s.replace("\n","");
        Log.d(TAG, "string: " + s);
        Log.d(TAG, "length: " + s.length());
        // convert the hex value into binary one
        int[][] obsArr = convertToIntNew(s.substring(0,75));
        return obsArr;
    }

    private static String convertBinaryToHex(String binaryStr) {
        String hexStr = "";
        for (int i=0;i<binaryStr.length();i+=4) {
            int decimal = Integer.parseInt(binaryStr.substring(i, i+4),2);
            hexStr += Integer.toString(decimal,16);
        }
        return hexStr;

    }

    private static String convertHexToMDF2(String s) {
        String MdfString2 = "";
        String tmpString = "";
        String[] binaryTempArray;
        String binaryString = "";

        s = s.replace("grid", "");
        Log.e(DEBUGMDF,"convertHexToMDF2 s: " + s);
        Log.e(DEBUGMDF,"convertHexToMDF2 s.length(): " + s.length());

        for (int i = 1; i <= 75; i++) {
            int hexToInt = Integer.parseInt(s.substring(i - 1, i), 16);
            String intToBinary = Integer.toBinaryString(hexToInt);
            // make sure all are 4 bits
            while (intToBinary.length() < 4) {
                intToBinary = "0" + intToBinary;
            }
            binaryString += intToBinary;
        }
        binaryTempArray = binaryString.split("");
        String[] binaryArray = Arrays.copyOfRange(binaryTempArray, 1, binaryTempArray.length);
        int row = 20;
        int col = 15;
        for (int i=row-1; i>=0;i--) {
            for(int j=0;j<col;j++) {
                tmpString += binaryArray[i*15+j];
            }
        }
        Log.e(DEBUGMDF,"convertHexToMDF2 tmpString: " + tmpString);
        MdfString2 = convertBinaryToHex(tmpString);

        return MdfString2;
    }

    private static String convertHexToMDF1(String s) {
        String MdfString1 = "";
        String tmpString = "11";
        String[] binaryTempArray;
        String binaryString = "";

        s = s.replace("grid", "");
        Log.e(DEBUGMDF,"convertHexToMDF1 s: " + s);
        Log.e(DEBUGMDF,"convertHexToMDF1 s.length(): " + s.length());

        for (int i = 76; i <= 150; i++) {
            int hexToInt = Integer.parseInt(s.substring(i - 1, i), 16);
            String intToBinary = Integer.toBinaryString(hexToInt);
            // make sure all are 4 bits
            while (intToBinary.length() < 4) {
                intToBinary = "0" + intToBinary;
            }
            binaryString += intToBinary;
        }
        binaryTempArray = binaryString.split("");
        String[] binaryArray = Arrays.copyOfRange(binaryTempArray, 1, binaryTempArray.length);
        int row = 20;
        int col = 15;
        for (int i=row-1; i>=0;i--) {
            for(int j=0;j<col;j++) {
                tmpString += binaryArray[i*15+j];
            }
        }
        tmpString = tmpString + "11";
        Log.e(DEBUGMDF,"convertHexToMDF1 tmpString: " + tmpString);

        MdfString1 = convertBinaryToHex(tmpString);

        return MdfString1;
    }


    private int[][] convertToIntNew(String s){
        Log.e("DebugGrid", "String: " + s);
        int[][] obstacleCells = new int[15][20];
        String[] binaryTempArray;
        String string = "";
        for (int i = 1; i <= 75; i++){
            int hexToInt = Integer.parseInt(s.substring(i-1,i), 16);
            String intToBinary = Integer.toBinaryString(hexToInt);
            // make sure all are 4 bits
            while (intToBinary.length() < 4){
                intToBinary = "0" + intToBinary;
            }
            string += intToBinary;
        }
        binaryTempArray = string.split("");
        String[] binaryArray = Arrays.copyOfRange(binaryTempArray, 1, binaryTempArray.length);
        //String[][] newBinArray= new String[20][15];

        for(int i=0, q=20-1; i<20; i++, q--){
            for (int j=0, p=0; j<15; j++, p++) {
                obstacleCells[p][q] = Integer.parseInt(binaryArray[i*15+j]);
            }
        }
        return obstacleCells;
    }

    public void updateObstacleArray(int[][] list){
        Log.d(TAG, "updateObstacleArray()");
//        for(int i = 0; i < 15; i++) {
//            for (int j = 0; j < 20; j++)
//                System.out.print(list[i][j] + " ");
//            System.out.println();
//        }
        if(autoUpdate == true){
            arena.setObstacles(list);
        }
    }

    public void updateGridArray(int[] array){
        if(autoUpdate == true){
            Log.d("updateGridArray","true");
            arena.setGridArray(array);
        }
    }

    public void onActivityResult(int request, int result, Intent data){
        Log.d("bluetooth", "onActivityResult " + result);
        Log.e("bluetooth","onActivityResult request: "+request);

        switch (request){
            case REQUEST_CONNECT_DEVICE:
                if (result == Activity.RESULT_OK){
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
                if(result == Activity.RESULT_OK){
                    //after selecting devices in BluetoothDevicesActivity, connect device.
                    setupChat();
                    connectDevice(data);

                }
                else{
                    Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void connectDevice(Intent data){
        Log.e(TAG, "connectDevice method");
        // get the connected device's MAC address
        String addr = data.getExtras().getString(BluetoothDevicesActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(addr);
        // connect to the device
        chatService.connect(device, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        new MenuInflater(getApplication()).inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = null;
        switch(item.getItemId()){
            case R.id.connect_devices:
                if (!btAdapter.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                    Toast.makeText(getApplicationContext(), "Bluetooth Enable", Toast.LENGTH_SHORT).show();
                }
                else {
                    intent = new Intent(this, BluetoothDevicesActivity.class);
                    startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
                }
                return true;
            case R.id.reconfiguration:
                Intent reconfigure = new Intent(this, ReconfigurationActivity.class);
                startActivityForResult(reconfigure, 0);
                break;
            case R.id.discoverable:
                allowDiscoverable();
                break;
        }
        return false;
    }

    public void reset(View v){
        for(int i = 0; i < 15; i ++){
            for(int j = 0; j < 20; j++)
                obstacleArray[i][j] = 0;
        }

        arena.setObstacles(obstacleArray);
        xStatus = 2;
        yStatus = 2;
        dStatus = 90;

        tConversationAA.clear();
        fConversationAA.clear();

        init();
        setRobot();

        startTimeFastest = 0L;
        startTimeExplore = 0L;
        timeInMillisecondsFastest = 0L;
        timeInMillisecondsExplore = 0L;
        timeBuffExplore = 0L;
        timeBuffFastest = 0L;
        exploreTime.setText("00:00:00");
        fastestTime.setText("00:00:00");
    }


    public void setRobot(){
        Log.e(TAG,"------setRobot------");
        String newPos = "{go:[";
        newPos += x_coordinate.getText().toString() + ",";
        newPos += y_coordinate.getText().toString() + ",";
        newPos += direction.getText().toString() + "]}";
        Log.e(TAG,"new Position: " + newPos);
        sendMessage(newPos);

        try {
            decodeString = decodeRobotString(newPos);
            updateGridArray(toIntArray(decodeString));
            Toast.makeText(getApplicationContext(), "Robot Set", Toast.LENGTH_SHORT).show();
            Log.d("setPosition: ", newPos);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to set robot", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void setRobotNew(){
        float[] waypointsCoord = arena.getWaypoints();
        String newPos = "{go:[";
        newPos += ((int)(waypointsCoord[0])+1) + ",";
        newPos += ((int)(waypointsCoord[1])+1) + ",";
        newPos += direction.getText().toString() + "]}";

        sendMessage(newPos);

        try {
            decodeString = decodeRobotString(newPos);
            updateGridArray(toIntArray(decodeString));
            Toast.makeText(getApplicationContext(), "Robot Set", Toast.LENGTH_SHORT).show();
            Log.d("setPosition: ", newPos);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to set robot", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public void setWaypoint(View view) {
        float[] waypointsCoord = arena.getWaypoints();
        Log.e(TAG,"------setWaypoints------");
        String pcY = "";
        String pcX = "";
        pcY += convertWaypointXToY((int)(waypointsCoord[0])+1);
        if (pcY.length() == 1){
            pcY = "0" + pcY;
        }
        pcX += convertWaypointYtoX((int)(waypointsCoord[1])+1);
        if (pcX.length() == 1){
            pcX = "0" + pcX;
        }
        String waypoints = "$" + pcX + pcY;
        Log.e(TAG,"Waypoints Sent: " + waypoints);
        sendMessage(waypoints);
        arena.setAllowDrawGreenBox(false);
        arena.wayPointIsSet(true,(int)(waypointsCoord[0]),(int)(waypointsCoord[1]));

}

    public void setStartPoint(View view){
        setRobotNew();
        arena.setAllowDrawGreenBox(false);
    }

    public int convertWaypointXToY(int x){
        int pcY;
        pcY = 20 - x + 1;
        return pcY;
    }
    public int convertWaypointYtoX(int y){
        int pcX;
        pcX = y;
        return pcX;
    }

    public void showMDF(View view) {
        TextView setCoordinatesTV = (TextView) findViewById(R.id.setCoordinatesTextView);
        Button setWaypointBtn = (Button) findViewById(R.id.btn_setWaypoint);
        Button setXYBtn = (Button) findViewById(R.id.btn_setXY);
        TextView MDFStringsTV = (TextView) findViewById(R.id.mdfStringsTV);
        TextView mdf1 = (TextView) findViewById(R.id.mdfString1);
        TextView mdf2 = (TextView) findViewById(R.id.mdfString2);
        TextView robotConfigTV = (TextView) findViewById(R.id.robotConfigurationTV);
        LinearLayout robotConfigLayout = (LinearLayout) findViewById(R.id.robotConfigurationLayout);
        mdf1.setText(obstacleMdfStr1);
        mdf2.setText(obstacleMdfStr2);

        if (isShowMDF){
            isShowMDF = false;
            setCoordinatesTV.setVisibility(GONE);
            setWaypointBtn.setVisibility(GONE);
            setXYBtn.setVisibility(GONE);
            robotConfigLayout.setVisibility(GONE);
            robotConfigTV.setVisibility(GONE);
            MDFStringsTV.setVisibility(View.VISIBLE);
            mdf1.setVisibility(View.VISIBLE);
            mdf2.setVisibility(View.VISIBLE);
        }
        else{
            isShowMDF=true;
            setCoordinatesTV.setVisibility(View.VISIBLE);
            setWaypointBtn.setVisibility(View.VISIBLE);
            setXYBtn.setVisibility(View.VISIBLE);
            robotConfigLayout.setVisibility(View.VISIBLE);
            robotConfigTV.setVisibility(View.VISIBLE);
            MDFStringsTV.setVisibility(GONE);
            mdf1.setVisibility(GONE);
            mdf2.setVisibility(GONE);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event){
        if(tilt == false){
            onPause();
        }
        if(event.values[0] > 4){
            turnLeft();
        }
        if(event.values[0] < -5){
            turnRight();
        }
        if(event.values[1] < 0){
            goStraight();
        }
        if(event.values[1] > 8){
            // reverse the direction
            turnLeft();
            turnLeft();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }


}

