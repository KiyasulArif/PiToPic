package com.kiyasul.android.fiftyx;

import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by kiyasul on 02/01/18.
 */
public class I2cRpi3ToPic implements Runnable{

    private static final String TAG = "SingletonClass";
    private static final int POLLING_DELAY_DEFAULT = 2000;          // Default Polling Freq.
    private static volatile I2cRpi3ToPic i2cInstance = null;

    private I2cDevice mI2cToPicDevice;
    private Handler mHandler;       // Polling Delay
    private boolean mEnabled;

    // I2C Device Bus Name
    private static final String I2C_DEVICE_NAME = "I2C1";
    // I2C Slave Address
    private static final int I2C_ADDRESS = 0x08;

    /**
     * PWM Address Registers in PIC
     */
    private static final int PWM3_ADDRESS = 0x00;
    private static final int PWM4_ADDRESS = 0x01;
    private static final int PWM5_ADDRESS = 0x02;
    private static final int PWM6_ADDRESS = 0x03;

    /**
     * DAC Address Registers in PIC
     */

    private static final int DAC1_ADDRESS = 0x04;

    /**
     * ADC Address Registers in PIC
     */
    private static final int ADA5_ADDRESS = 0x05;
    private static final int ADC3_ADDRESS = 0x07;
    private static final int ADC4_ADDRESS = 0x09;
    private static final int ADC5_ADDRESS = 0x0b;

    // ## Internal Temp Tested Successfully ## //
    // private static final int ADC_TEMP_DIE = 0x0d;

    /**
     * Firebase reference
     */
    DatabaseReference Firebase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mADA5 = null;
    DatabaseReference mADC3 = null;
    DatabaseReference mADC4 = null;
    DatabaseReference mADC5 = null;

    /**
     *  Converted Values from the ADC Channels (Byte to Int)
     */
    private int adcA5 ;
    private int adcC3 ;
    private int adcC4 ;
    private int adcC5 ;

    /**
     *  Analog Channel to which TMP36 sensor is connected
     */
    private int TMP36_CHANNEL;
    private float TMP36_VALUE;

    // Private Constructor
    private I2cRpi3ToPic() {

        //Prevent form the reflection api.
        if (i2cInstance != null){
            throw new RuntimeException("Use getI2cInstance() method to get the single instance of this class.");
        }

        mHandler = new Handler();
    }

    public static I2cRpi3ToPic getI2cInstance() {

        //If there is no instance available... create new one
        if (i2cInstance == null) {
            synchronized (I2cRpi3ToPic.class) {
                if (i2cInstance == null)
                    i2cInstance = new I2cRpi3ToPic();
            }
        }

        return i2cInstance;
    }

    /**
     * Setup Firebase Reference for this class
     * Only ADC references from Firebase
     */
    public void setupFirebaseADCReferences(String ADA5IN, String ADC3IN, String ADC4IN, String ADC5IN, int TMP36IN){

         mADA5 = Firebase.child(ADA5IN.trim());
         mADC3 = Firebase.child(ADC3IN.trim());
         mADC4 = Firebase.child(ADC4IN.trim());
         mADC5 = Firebase.child(ADC5IN.trim());

         TMP36_CHANNEL = (TMP36IN >= 1 && TMP36IN <= 4) ? TMP36IN : 1;

    }

    /**
     * I2C Connection to PIC is established
     */
    public void connectToPic() {

        PeripheralManagerService manager = new PeripheralManagerService();
        try {
            mI2cToPicDevice = manager.openI2cDevice(I2C_DEVICE_NAME,I2C_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Disconnects the Bus from PIC
     */
    public void DisconnectFromPic() {

        if(mI2cToPicDevice != null){
            try{
                mI2cToPicDevice.close();
                mI2cToPicDevice = null;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param dutycycle - dutycycle of the PWM3 pin in the PIC
     * @throws IOException
     */
    public void setPWM3DutyCycle(int dutycycle) throws IOException {

        Log.w(TAG,"Green inside");
        setPWM3DutyCycle(mI2cToPicDevice,PWM3_ADDRESS,dutycycle);
    }


    /**
     *
     * @param dutycycle - dutycycle of the PWM4 pin in the PIC
     * @throws IOException
     */
    public void setPWM4DutyCycle(int dutycycle) throws IOException {

        setPWM4DutyCycle(mI2cToPicDevice,PWM4_ADDRESS,dutycycle);
    }

    /**
     *
     * @param dutycycle - dutycycle of the PWM5 pin in the PIC
     * @throws IOException
     */
    public void setPWM5DutyCycle(int dutycycle) throws IOException {

        setPWM5DutyCycle(mI2cToPicDevice,PWM5_ADDRESS,dutycycle);
    }

    /**
     *
     * @param dutycycle - dutycycle of the PWM6 pin in the PIC
     * @throws IOException
     */
    public void setPWM6DutyCycle(int dutycycle) throws IOException {

        setPWM6DutyCycle(mI2cToPicDevice,PWM6_ADDRESS,dutycycle);
    }

    /**
     *
     * @param dac1OutputValue - 5-bit resolution DAC
     * @throws IOException
     */
    public void setDAC1OutputValue(int dac1OutputValue) throws IOException {

        setDAC1OutputValue(mI2cToPicDevice,DAC1_ADDRESS,dac1OutputValue);
        Log.w(TAG,"DAC Value going to change");
    }

    public int readA2DChannelA5()throws IOException {

        adcA5 =  readA2DChannelA5(mI2cToPicDevice,ADA5_ADDRESS );
        return adcA5;
    }

    public int readA2DChannelC3()throws IOException {

        adcC3 =  readA2DChannelC3(mI2cToPicDevice,ADC3_ADDRESS );
        return adcC3;
    }

    public int readA2DChannelC4() throws IOException {

        adcC4 = readA2DChannelC4(mI2cToPicDevice,ADC4_ADDRESS );
        return adcC4;
    }

    public int readA2DChannelC5() throws IOException {

        adcC5 = readA2DChannelC5(mI2cToPicDevice,ADC5_ADDRESS );
        return adcC5;
    }


    /**
     *
     * @param device - I2C device object reference
     * @param address - Write value to this register in PIC
     * @param dutycycle - PWM3 pin dutycycle
     * @throws IOException
     */
    private void setPWM3DutyCycle(I2cDevice device, int address, int dutycycle) throws IOException {

        Log.w(TAG,"Green byte sent...");
        dutycycle = dutyCycleBoundaryCheck(dutycycle);
        byte value = (byte) dutycycle;

        // Write the dutycycle value to slave
        device.writeRegByte(address, value);
    }

    /**
     *
     * @param device - I2C device object reference
     * @param address - Write value to this register in PIC
     * @param dutycycle - PWM4 pin dutycycle
     * @throws IOException
     */
    private void setPWM4DutyCycle(I2cDevice device, int address, int dutycycle) throws IOException {

        dutycycle = dutyCycleBoundaryCheck(dutycycle);
        byte value = (byte) dutycycle;

        // Write the dutycycle value to slave
        device.writeRegByte(address, value);
    }

    /**
     *
     * @param device - I2C device object reference
     * @param address - Write value to this register in PIC
     * @param dutycycle - PWM5 pin dutycycle
     * @throws IOException
     */
    private void setPWM5DutyCycle(I2cDevice device, int address, int dutycycle) throws IOException {

        dutycycle = dutyCycleBoundaryCheck(dutycycle);
        byte value = (byte) dutycycle;

        // Write the dutycycle value to slave
        device.writeRegByte(address, value);
    }

    /**
     *
     * @param device - I2C device object reference
     * @param address - Write value to this register in PIC
     * @param dutycycle - PWM6 pin dutycycle
     * @throws IOException
     */
    private void setPWM6DutyCycle(I2cDevice device, int address, int dutycycle) throws IOException {

        dutycycle = dutyCycleBoundaryCheck(dutycycle);
        byte value = (byte) dutycycle;

        // Write the dutycycle value to slave
        device.writeRegByte(address, value);
    }

    /**
     *
     * @param device - I2C device object reference
     * @param address - Write value to this register in PIC
     * @param dac1OutputValue - 5-bit resolution analog output
     * @throws IOException
     */
    private void setDAC1OutputValue(I2cDevice device, int address, int dac1OutputValue) throws IOException {

        dac1OutputValue = DAC1OutputBoundaryCheck(dac1OutputValue);
        byte value = (byte) dac1OutputValue;
        Log.w(TAG,"DAC Value byte written");
        // Write the Analog Output Voltage Equivalent Value
        device.writeRegByte(address, value);

    }

    private int readA2DChannelA5(I2cDevice device, int startAddress)throws IOException {
        // Read three consecutive register values
        byte[] data = new byte[2];
        device.readRegBuffer(startAddress, data, data.length);
        return byteToInt(data);
    }

    private int readA2DChannelC3(I2cDevice device, int startAddress)throws IOException {
        // Read three consecutive register values
        byte[] data = new byte[2];
        device.readRegBuffer(startAddress, data, data.length);
        return byteToInt(data);
    }

    private int readA2DChannelC4(I2cDevice device, int startAddress) throws IOException {
        // Read three consecutive register values
        byte[] data = new byte[2];
        device.readRegBuffer(startAddress, data, data.length);
        return byteToInt(data);

    }

    private int readA2DChannelC5(I2cDevice device, int startAddress) throws IOException {
        // Read three consecutive register values
        byte[] data = new byte[2];
        device.readRegBuffer(startAddress, data, data.length);
        return byteToInt(data);
    }


    /**
     *
     * @param dutycycle - Exception inputs from user
     * @return - modified dutycycle if boundary conditions met
     */
    private int dutyCycleBoundaryCheck(int dutycycle) {

        if(dutycycle > 100) {
            dutycycle = 100;
        }
        else if(dutycycle < 0) {
            dutycycle = 0;
        }

        return dutycycle;
    }

    /**
     *
     * @param dac1OutputValue - Exception inputs from user
     * @return - modified dac output if boundary conditions met
     */
    private int DAC1OutputBoundaryCheck(int dac1OutputValue) {

        if(dac1OutputValue > 31) {
            dac1OutputValue = 31;
        } else if(dac1OutputValue < 0) {
            dac1OutputValue = 0;
        }

        return dac1OutputValue;
    }

    /**
     *
     * @param byte_value - byte value from the I2C read api
     * @return - byte value converted to int
     */
    private int byteToInt(byte[] byte_value){

        return ((byte_value[1] & 0xff) << 8) | (byte_value[0] & 0xff);
    }

    /* Handler Controls */

    /**
     * Start Routine
     */
    public void threadUpdateADC() {
        if(mHandler != null) {
            mEnabled = true;
            mHandler.postDelayed(this, POLLING_DELAY_DEFAULT);
        }
    }

    /**
     * call on destroy() method - for best practices
     */
    public void stopADCThread(){
        if(mHandler != null){
            mEnabled = false;
        }
    }

    /**
     * Thread kicks off
     */
    @Override
    public void run() {

        if(!mEnabled)
            return;

        try {
            readA2DChannelA5();
            readA2DChannelC3();
            readA2DChannelC4();
            readA2DChannelC5();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Loop program
        loop();
    }

    private void loop(){

        calibrateAndUpdateFirebase();

        // handler to loop in the specified delay
        mHandler.postDelayed(this, POLLING_DELAY_DEFAULT);
    }

    /**
     * Case statements based on the selection of the TMP36 analog channel
     */
    private void calibrateAndUpdateFirebase() {

       switch (TMP36_CHANNEL){
           case 1:
               TMP36_VALUE = calibrateTMP36(adcA5);
               mADA5.setValue(TMP36_VALUE);
               mADC3.setValue(adcC3);
               mADC4.setValue(adcC4);
               mADC5.setValue(adcC5);
               logDebugADC();
               break;
           case 2:
               TMP36_VALUE = calibrateTMP36(adcC3);
               mADA5.setValue(adcA5);
               mADC3.setValue(TMP36_VALUE);
               mADC4.setValue(adcC4);
               mADC5.setValue(adcC5);
               logDebugADC();
               break;
           case 3:
               TMP36_VALUE = calibrateTMP36(adcC4);
               mADA5.setValue(adcA5);
               mADC3.setValue(adcC3);
               mADC4.setValue(TMP36_VALUE);
               mADC5.setValue(adcC5);
               logDebugADC();
               break;
           case 4:
               TMP36_VALUE = calibrateTMP36(adcC5);
               mADA5.setValue(adcA5);
               mADC3.setValue(adcC3);
               mADC4.setValue(adcC4);
               mADC5.setValue(TMP36_VALUE);
               logDebugADC();
               break;
           default:
               TMP36_VALUE = calibrateTMP36(adcA5);
               mADA5.setValue(TMP36_VALUE);
               mADC3.setValue(adcC3);
               mADC4.setValue(adcC4);
               mADC5.setValue(adcC5);
               logDebugADC();
       }
    }

    /**
     *
     * @param adc_value - uncalibrated TMP36 adc_value
     * @return - calibrated value in float (°C)
     *
     * Temperature (in °C) = ( ( ( (ADC_VALUE) * (FVR / 1024) ) - 500 ) / 10 )
     * SETUP FVR    = 2.048 V
     * MEASURED FVR = 1.98  V
     * ADC_VALUE - raw adc value from PIC
     */
    private float calibrateTMP36(int adc_value){
        return (float) (((adc_value * 1.933) - 500 ) / 10);
    }

    private void  logDebugADC(){
        Log.d(TAG,"ADC A5   = " + String.valueOf(adcA5));
        Log.d(TAG,"ADC C3   = " + String.valueOf(adcC3));
        Log.d(TAG,"ADC C3   = " + String.valueOf(adcC4));
        Log.d(TAG,"ADC C3   = " + String.valueOf(adcC5));
    }
    /**
     * Getters
     * @return - adc values
     */
    public int getAdcA5(){ return adcA5; }

    public int getAdc3() {
        return adcC3;
    }

    public int getAdc4() {
        return adcC4;
    }

    public int getAdc5() {
        return adcC5;
    }

}
