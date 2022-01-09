package com.harrysoft.androidbluetoothserial.demoapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import ca.hss.heatmaplib.HeatMap;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class InsoleActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button leftBtn, rightBtn;
    private String L_insole_mac = "20:17:12:04:01:52";
    private String R_insole_mac = "20:17:12:04:19:55";
    private String start_bytes = "0 254 128";
    private String left_temp_bytes, right_temp_bytes;
    private boolean is_L_insole_started = false;
    private boolean is_R_insole_started = false;
    private boolean is_L_insole_connected = false;
    private boolean is_R_insole_connected = false;
    private int stateL, stateR;
    private int right_data_len, left_data_len = 0;
    private int right_sensor_data_count, right_data_index, right_package_count = 0;
    private int left_sensor_data_count, left_data_index, left_package_count = 0;
    private int max_data_len = 114;
    private int ns_list[] = {58,76,77,78,79,80,81,82,83,84,94,95,96,97,98,99,100,101,102,103,104,105,112,113,114};
    private Double r_data_double_arr[] = new Double[89];
    private Double l_data_double_arr[] = new Double[89];
    private List<Integer> non_sensor_indeces = new ArrayList<Integer>(ns_list.length+1);
    private SimpleBluetoothDeviceInterface left_insole_device_interface;
    private SimpleBluetoothDeviceInterface right_insole_device_interface;
    private BluetoothManager bluetoothManager = BluetoothManager.getInstance();
    private HeatMap heatMapLeft, heatMapRight;
    private Timer right_timer = new Timer();
    private Timer left_timer = new Timer();
    private String account, passwd, identity;
    final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    Map<String,Object> leftDataDict, rightDataDict;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
    private SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    float x_R[] = {0.25f, 0.25f,0.25f,0.25f,0.25f,0.25f,0.25f,0.25f,0.25f,0.25f,0.2f,0.2f,0.2f,0.2f,0.2f,0.2f,0.2f,0.2f,0.2f,
            0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,
            0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,
            0.7f,0.7f,0.7f,0.7f,0.7f,0.7f,0.7f,0.7f,0.7f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,
            0.85f,0.85f,0.8f,0.8f,0.8f,0.8f,0.8f,0.8f,0.8f,
            0.95f,0.95f,0.95f,0.95f,0.95f,0.95f};
    float x_L[] = {0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.8f,0.8f,0.8f,0.8f,0.8f,0.8f,0.8f,0.8f,0.8f,
            0.6f, 0.6f,0.6f,0.6f,0.6f,0.6f,0.6f,0.6f,0.6f,0.6f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,0.65f,
            0.45f,0.45f,0.45f,0.45f,0.45f,0.45f,0.45f,0.45f,0.45f,0.45f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,0.5f,
            0.3f,0.3f,0.3f,0.3f,0.3f,0.3f,0.3f,0.3f,0.3f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,0.35f,
            0.15f,0.15f,0.2f,0.2f,0.2f,0.2f,0.2f,0.2f,0.2f,
            0.05f,0.05f,0.05f,0.05f,0.05f,0.05f};
    float y[] = {0.95f,0.9f,0.85f,0.8f,0.75f,0.7f,0.65f,0.6f,0.55f,0.50f,0.45f,0.40f,0.35f,0.30f,0.25f,0.20f,0.15f,0.10f, 0.05f,
            0.95f,0.9f,0.85f,0.8f,0.75f,0.7f,0.65f,0.6f,0.55f,0.50f,0.45f,0.40f,0.35f,0.30f,0.25f,0.20f,0.15f,0.10f, 0.05f,
            0.95f,0.9f,0.85f,0.8f,0.75f,0.7f,0.65f,0.6f,0.55f,0.50f,0.45f,0.40f,0.35f,0.30f,0.25f,0.20f,0.15f,0.10f, 0.05f,
            0.9f,0.85f,0.8f,0.75f,0.7f,0.65f,0.6f,0.55f,0.50f,0.45f,0.40f,0.35f,0.30f,0.25f,0.20f,0.15f,0.10f,
            0.55f,0.50f,0.45f,0.40f,0.35f,0.30f,0.25f,0.20f,0.15f,
            0.45f,0.40f,0.35f,0.30f,0.25f,0.20f };

    //heroku
    HerokuService herokuService;

    private Button btn1, btn2;
    private BluetoothDevice BLEDevice = null;
    private BluetoothGatt connectedGatt = null;
    private final Handler BLEHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_insole);

        account = getIntent().getStringExtra("account");
        passwd = getIntent().getStringExtra("passwd");
        identity = getIntent().getStringExtra("identity");

        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(getApplication(), "Bluetooth not available.", Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            finish();
        }
        findObject();
        initial();
        btnClickEvent();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://93e3-140-116-247-243.ngrok.io/")
                .build();
        herokuService = retrofit.create(HerokuService.class);

        Double arr1[] = new Double[3];
        ArrayList<String> arr = new ArrayList<>();
        arr1[0] = 10.0;
        arr1[1] = 20.0;
        arr1[2] = 30.0;
        arr.add(Arrays.toString(arr1));
        arr1[0] = 40.0;
        arr1[1] = 50.0;
        arr1[2] = 60.0;
        arr.add(Arrays.toString(arr1));
        arr1[0] = 70.0;
        arr1[1] = 80.0;
        arr1[2] = 90.0;
        arr.add(Arrays.toString(arr1));

        Call<ResponseBody> call = herokuService.test(arr);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v("upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v("upload error", t.getMessage());
            }
        });

        android.bluetooth.BluetoothManager BLEBluetoothManager = (android.bluetooth.BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BLEDevice = BLEBluetoothManager.getAdapter().getRemoteDevice("1C:BA:8C:1D:31:80");
        connect();

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);

        btn1.setOnClickListener(v -> send("60\n"));
        btn2.setOnClickListener(v -> send("s\n"));
    }

    @Override
    protected  void onStop() {
        Toast.makeText(this,"onStop",Toast.LENGTH_SHORT).show();
        if(left_insole_device_interface != null)
            left_insole_device_interface.stopInsole();
        if(right_insole_device_interface != null)
            right_insole_device_interface.stopInsole();
        if(bluetoothManager != null)
            bluetoothManager.close();
        is_L_insole_connected = false;
        is_R_insole_connected = false;

        Intent intent = new Intent();
        intent.setClass(InsoleActivity.this, MenuActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("passwd", passwd);
        intent.putExtra("identity", identity);
        startActivity(intent);

        send("s\n");
        if (connectedGatt != null)
            connectedGatt.close();
        connectedGatt = null;
        Log.d("appendLog", "BlEDeviceActivity is closed");

        super.onStop();
    }

    private void send(String message) {
        if (BLEDevice != null && connectedGatt != null) {
            String SERVICE_UUID = "0000dfb0-0000-1000-8000-00805f9b34fb";
            String CHAR_FOR_INDICATE_UUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
            BluetoothGattService service = connectedGatt.getService(UUID.fromString(SERVICE_UUID));
            if (service == null){
                Log.d("appendLog", "ERROR: Service not found, disconnecting");
                if (connectedGatt != null)
                    connectedGatt.disconnect();
                return;
            }
            BluetoothGattCharacteristic characteristicForWrite = service.getCharacteristic(UUID.fromString(CHAR_FOR_INDICATE_UUID));
            if (characteristicForWrite != null) {
                characteristicForWrite.setValue(message.getBytes(StandardCharsets.UTF_8));
                connectedGatt.writeCharacteristic(characteristicForWrite);
            }
        } else {
            Log.d("appendLog", "ERROR: BluetoothDevice is null, cannot send");
        }
    }

    private void connect() {
        if (BLEDevice != null) {
            Log.d("appendLog", "Connecting");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                synchronized(this) {
                    BLEDevice.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
                }
            else
                synchronized(this) {
                    BLEDevice.connectGatt(this, false, gattCallback);
                }
        } else {
            Log.d("appendLog", "ERROR: BluetoothDevice is null, cannot connect");
        }
    }

    private void bleRestartLifecycle() {
        long timeoutSec = 5L;
        Log.d("appendLog", "Will try reconnect in " + timeoutSec + " seconds");
        BLEHandler.postDelayed(this::connect, timeoutSec * 1000);
    }

    //region BLE events, when connected
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // recommended on UI thread https://punchthrough.com/android-ble-guide/
                    BLEHandler.post(gatt::discoverServices);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    connectedGatt = null;
                    gatt.close();
                    bleRestartLifecycle();
                }
            } else {
                // random error 133 - close() and try reconnect
                Log.d("appendLog", "ERROR: onConnectionStateChange status=" + status + ", disconnecting");
                connectedGatt = null;
                gatt.close();
                bleRestartLifecycle();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 129 /*GATT_INTERNAL_ERROR*/) {
                // it should be a rare case, this article recommends to disconnect:
                // https://medium.com/@martijn.van.welie/making-android-ble-work-part-2-47a3cdaade07
                Log.d("appendLog", "ERROR: status=129 (GATT_INTERNAL_ERROR), disconnecting");
                gatt.disconnect();
                return;
            }
            connectedGatt = gatt;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //this block should be synchronized to prevent the function overloading
            synchronized(this) {
                String log = "onCharacteristicWrite " +
                        (status == BluetoothGatt.GATT_SUCCESS ? "OK" :
                                status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED ? "not allowed" :
                                        status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH ? "invalid length" : "error $status");
                Log.d("appendLog", log);
            }
        }
    };
    //endregion

    public void findObject() {
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);
        heatMapRight = (HeatMap) findViewById(R.id.heatmapRight);
        heatMapLeft = (HeatMap) findViewById(R.id.heatmapLeft);
    }

    private void initial() {
        //Set the range that you want the heat maps gradient to cover
        heatMapRight.setMinimum(0);
        heatMapRight.setMaximum(64);
        heatMapLeft.setMinimum(0);
        heatMapLeft.setMaximum(64);
        //make the colour gradient from yellow to red
        Map<Float, Integer> colorStops = new ArrayMap<>();
        colorStops.put(0.3f, 0xFFDACF03);
        colorStops.put(0.4f, 0xFFDA7203);
        colorStops.put(1.0f, 0xFFDA031C);
        heatMapRight.setColorStops(colorStops);
        heatMapRight.setRadius(180);
        heatMapLeft.setColorStops(colorStops);
        heatMapLeft.setRadius(180);

        for(int i = 0; i < ns_list.length; i++)
            non_sensor_indeces.add(ns_list[i]);

        leftDataDict = new HashMap<>();
        rightDataDict = new HashMap<>();

        stateL = 0;
        stateR = 0;
    }

    public void btnClickEvent() {
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stateL == 0) {
                    connectDevice(L_insole_mac);
                    leftBtn.setText("開始偵測");
                }
                else if(stateL == 1) {
                    left_insole_device_interface.startInsole();
                    is_L_insole_started = true;
                    //startLeftBtn.setText("Stop Left");
                    leftBtn.setText("結束偵測");
                    Toast.makeText(InsoleActivity.this, "Left Insole Started.", Toast.LENGTH_SHORT).show();
                }
                else if(stateL == 2) {
                    left_insole_device_interface.stopInsole();
                    is_L_insole_started = false;
                    //startLeftBtn.setText("Start Left");
                    leftBtn.setText("斷開藍芽");
                    left_timer.cancel();
                    Toast.makeText(InsoleActivity.this, "Left Insole Not Connected!", Toast.LENGTH_SHORT).show();
                }
                else if(stateL == 3) {
                    bluetoothManager.closeDevice(left_insole_device_interface);
                    is_L_insole_connected = false;
                    Toast.makeText(InsoleActivity.this, "Left Insole Disconnected.", Toast.LENGTH_SHORT).show();
                    leftBtn.setText("連線藍芽");
                }
                stateL++;
                stateL %= 4;
            }
            private void connectDevice(String mac) {
                bluetoothManager.openSerialDevice(mac)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onConnected, this::onError);
            }
            private void onConnected(BluetoothSerialDevice connectedDevice) {
                // You are now connected to this device!
                // Here you may want to retain an instance to your device:
                left_insole_device_interface = connectedDevice.toSimpleDeviceInterface();
                // Listen to bluetooth events
                left_insole_device_interface.setListeners(message -> onMessageReceived(message), this::onMessageSent, this::onError);
                left_insole_device_interface.stopInsole();
                is_L_insole_connected = true;
                Toast.makeText(getApplication(), "Connected to Left Insole.", Toast.LENGTH_SHORT).show();
            }
            private void onMessageReceived(String message) {
                //store incoming bytes temporarily
                if(!is_L_insole_started){
                    left_temp_bytes += message + " ";
                }
                //check whether the start_bytes exits in the temporary buffer
                if(!is_L_insole_started && left_temp_bytes.contains(start_bytes)){
                    is_L_insole_started = true;
                    left_temp_bytes = "";
                }
                //if the start_bytes are found in the temporary buffer, start storing the incoming messages in the actual buffer
                if(is_L_insole_started) {
                    left_data_len++;
                    if(left_data_len > 16) {
                        left_sensor_data_count++;
                        if (!non_sensor_indeces.contains(left_sensor_data_count)) {
                            l_data_double_arr[left_data_index] = Double.parseDouble(message);
                            // System.out.println("NON SENSOR INDEX:" + left_data_index + " " + message);
                            left_data_index++;
                        }
                    }
                    Date date = new Date();
                    leftDataDict.put(String.valueOf(formatter.format(date)), Arrays.toString(l_data_double_arr));
                    if (leftDataDict.size() == 50) {
                        sendToFirebase(leftDataDict, "Left_Insole");
                        leftDataDict.clear();
                    }
                    //if the data length reach the max_data_length, release the buffer and invert the start flag
                    if(left_data_len >= max_data_len + 16){
                        heatMapLeft.clearData();
                        for(int i = 0; i < x_L.length; i++) {
                            HeatMap.DataPoint point =  new HeatMap.DataPoint(x_L[i], y[i], l_data_double_arr[i]);
                            heatMapLeft.addData(point);
                            heatMapLeft.forceRefresh();
                        }
                        left_package_count++;
                        left_data_index = 0;
                        left_sensor_data_count = 0;
                        left_data_len = 0;
                        is_L_insole_started = false;
                    }
                }
            }
            private void onMessageSent(String message) {
                // We sent a message! Handle it here.
                // Toast.makeText(getApplication(), "Sent a message! Message was: " + message, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            }
            private void onError(Throwable error) {
                // Handle the error
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stateR == 0) {
                    connectDevice(R_insole_mac);
                    rightBtn.setText("開始偵測");
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP);
                }
                else if(stateR == 1) {
                    toneGen1.stopTone();
                    right_insole_device_interface.startInsole();
                    is_R_insole_started = true;
                    //startRightBtn.setText("Stop Right");
                    rightBtn.setText("結束偵測");
                    Toast.makeText(InsoleActivity.this, "Right Insole Started.", Toast.LENGTH_SHORT).show();
                }
                else if(stateR == 2) {
                    right_insole_device_interface.stopInsole();
                    is_R_insole_started = false;
                    //startRightBtn.setText("Start Right");
                    rightBtn.setText("斷開藍芽");
                    right_timer.cancel();
                    Toast.makeText(InsoleActivity.this, "Right Insole Not Connected!", Toast.LENGTH_SHORT).show();
                }
                else if(stateR == 3) {
                    bluetoothManager.closeDevice(right_insole_device_interface);
                    is_R_insole_connected = false;
                    Toast.makeText(InsoleActivity.this, "Right Insole Disconnected.", Toast.LENGTH_SHORT).show();
                    rightBtn.setText("連線藍芽");
                }
                stateR++;
                stateR %= 4;
            }
            private void connectDevice (String mac) {
                bluetoothManager.openSerialDevice(mac)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onConnected, this::onError);
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            private void onConnected (BluetoothSerialDevice connectedDevice) {
                // You are now connected to this device!
                // Here you may want to retain an instance to your device:
                right_insole_device_interface = connectedDevice.toSimpleDeviceInterface();
                // Listen to bluetooth events
                right_insole_device_interface.setListeners(message -> onMessageReceived(message), this::onMessageSent, this::onError);
                right_insole_device_interface.stopInsole();
                is_R_insole_connected = true;
                Toast.makeText(getApplication(), "Connected to Right Insole.", Toast.LENGTH_SHORT).show();
            }
            private void onMessageSent (String message) {
                // We sent a message! Handle it here.
            }
            private void onMessageReceived (String message) {
                //store incoming bytes temporarily
                if(!is_R_insole_started) {
                    right_temp_bytes += message + " ";
                }
                //check whether the start_bytes exits in the temporary buffer
                if(!is_R_insole_started && right_temp_bytes.contains(start_bytes)) {
                    is_R_insole_started = true;
                    right_temp_bytes = "";
                }
                //if the start_bytes are found in the temporary buffer, start storing the incoming messages in the actual buffer
                if(is_R_insole_started) {
                    right_data_len++;
                    if(right_data_len>16) {
                        right_sensor_data_count++;
                        if (!non_sensor_indeces.contains(right_sensor_data_count)) {
                            r_data_double_arr[right_data_index] = Double.parseDouble(message);
                            // System.out.println("NON SENSOR INDEX:" + right_data_index + " " + message);
                            right_data_index++;
                        }
                    }
                    Date date = new Date();
                    rightDataDict.put(String.valueOf(formatter.format(date)), Arrays.toString(r_data_double_arr));
                    if (rightDataDict.size() == 2) {
                        //sendToFirebase(rightDataDict, "Right_Insole");

                        rightDataDict.clear();
                        onStop();
                    }
                    // if the data length reach the max_data_length, release the buffer and invert the start flag
                    if(right_data_len >= max_data_len + 16) {
                        heatMapRight.clearData();
                        for(int i = 0; i < x_R.length; i++) {
                            HeatMap.DataPoint point =  new HeatMap.DataPoint(x_R[i], y[i], r_data_double_arr[i]);
                            heatMapRight.addData(point);
                            heatMapRight.forceRefresh();
                        }
                        right_data_index = 0;
                        right_sensor_data_count = 0;
                        right_data_len = 0;
                        is_R_insole_started = false;
                    }
                }
            }
            private void onError(Throwable error) {
                // Handle the error
            }
        });
    }

    public void sendToFirebase(Map<String,Object>  data, String of_insole){
        Date date = new Date();
        db.collection(identity).document(account).collection(of_insole).document(String.valueOf(formatter2.format(date))).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // data.clear();
                        Toast.makeText(InsoleActivity.this, "Succesfully saved to Firebase", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InsoleActivity.this, "Failed to saved to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}