package org.rogerunwin.devices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

import org.rogerunwin.devices.base.Actuator;

import com.fazecast.jSerialComm.SerialPort;

public class LX16A_Servo extends Actuator {
    public static final byte SERVO_MODE = 0;
    public static final byte MOTOR_MODE = 1;
    public static final byte UNKNOWN_MODE = 2;

    private static final byte SERVO_COMMAND_START = 0x55;

    private static final byte SERVO_MOVE_TIME_WRITE = 0x01;

    private static final byte SERVO_MOVE_TIME_READ = 0x02;
    private static final byte SERVO_MOVE_TIME_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_MOVE_TIME_WAIT_WRITE = 7;

    private static final byte SERVO_MOVE_TIME_WAIT_READ = 8;
    private static final byte SERVO_MOVE_TIME_WAIT_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_MOVE_START = 11;

    private static final byte SERVO_MOVE_STOP = 12;

    // private static final byte SERVO_ID_WRITE = 13;
    // private static final byte SERVO_ID_WRITE_READ_BYTES = 4; // bytes

    // private static final byte SERVO_ID_READ = 14;
    // private static final byte SERVO_ID_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_ANGLE_OFFSET_ADJUST = 17;

    private static final byte SERVO_ANGLE_OFFSET_WRITE = 18;

    private static final byte SERVO_ANGLE_OFFSET_READ = 19;
    private static final byte SERVO_ANGLE_OFFSET_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_ANGLE_LIMIT_WRITE = 20;

    private static final byte SERVO_ANGLE_LIMIT_READ = 21;
    private static final byte SERVO_ANGLE_LIMIT_READ_READ_BYTES = 10; // bytes

    private static final byte SERVO_VIN_LIMIT_WRITE = 22;

    private static final byte SERVO_VIN_LIMIT_READ = 23;
    private static final byte SERVO_VIN_LIMIT_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_TEMP_MAX_LIMIT_WRITE = 24;

    private static final byte SERVO_TEMP_MAX_LIMIT_READ = 25;
    private static final byte SERVO_TEMP_MAX_LIMIT_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_TEMP_READ = 26;
    private static final byte SERVO_TEMP_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_VIN_READ = 27;
    private static final byte SERVO_VIN_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_POS_READ = 28;
    private static final byte SERVO_POS_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_OR_MOTOR_MODE_WRITE = 29;

    private static final byte SERVO_OR_MOTOR_MODE_READ = 30;
    private static final byte SERVO_OR_MOTOR_MODE_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_LOAD_OR_UNLOAD_WRITE = 31; // 4 bytes

    private static final byte SERVO_LOAD_OR_UNLOAD_READ = 32;
    private static final byte SERVO_LOAD_OR_UNLOAD_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_LED_CTRL_WRITE = 33; // 4 bytes

    private static final byte SERVO_LED_CTRL_READ = 34;
    private static final byte SERVO_LED_CTRL_READ_READ_BYTES = 3; // bytes

    private static final byte SERVO_LED_ERROR_WRITE = 35; // 4 bytes

    private static final byte SERVO_LED_ERROR_READ = 36;
    private static final byte SERVO_LED_ERROR_READ_READ_BYTES = 3; // bytes

    public static final byte FAULT_NO_ALARM = 0;
    public static final byte FAULT_OVER_TEMPERATURE = 1;
    public static final byte FAULT_OVER_VOLTAGE = 2;
    public static final byte FAULT_OVER_TEMPERATURE_AND_OVER_VOLTAGE = 3;
    public static final byte FAULT_LOCKED_ROTOR = 4;
    public static final byte FAULT_OVER_TEMPERATURE_AND_STALLED = 5;
    public static final byte FAULT_OVER_VOLTAGE_AND_STALLED = 6;
    public static final byte FAULT_OVER_TEMPERATURE_OVER_VOLTAGE_AND_STALLED = 7;

    private static final byte CHECKSUM_HOLDER = 0;

    private final SerialPort port;
    private final OutputStream portout;
    private final InputStream portin;

    private final HashMap<Byte, Short> minAngleLimit = new HashMap<Byte, Short>();
    private final HashMap<Byte, Short> maxAngleLimit = new HashMap<Byte, Short>();

    private final HashMap<Byte, Short> minVInLimit = new HashMap<Byte, Short>();
    private final HashMap<Byte, Short> maxVInLimit = new HashMap<Byte, Short>();

    private final HashMap<Byte, Byte> angleOffset = new HashMap<Byte, Byte>();

    private final HashMap<Byte, Byte> maxTemp = new HashMap<Byte, Byte>();
    private final HashMap<Byte, Byte> currentTemp = new HashMap<Byte, Byte>();
    private final HashMap<Byte, Double> currentVoltage = new HashMap<Byte, Double>();

    private final HashMap<Byte, Short> targetPosition = new HashMap<Byte, Short>();
    private final HashMap<Byte, Short> actualPosition = new HashMap<Byte, Short>();

    private final HashMap<Byte, Byte> servoMode = new HashMap<Byte, Byte>();

    private final HashMap<Byte, Byte> motorEnabled = new HashMap<Byte, Byte>();

    private final HashMap<Byte, Boolean> ledState = new HashMap<Byte, Boolean>();

    public byte[] setChecksum(byte[] bytes) {
	byte checksum = 0;
	for (byte b : Arrays.copyOfRange(bytes, 2, bytes.length - 1)) {
	    checksum += b;
	}
	bytes[bytes.length - 1] = (byte) ~checksum;
	if (!testChecksum(bytes)) {
	    System.out.println("WARNING BAD CHECKSUM!");
	}
	return bytes;
    }

    public boolean testChecksum(byte[] bytes) {
	byte checksum = 0;
	for (byte b : Arrays.copyOfRange(bytes, 2, bytes.length - 1)) {
	    checksum += b;
	}
	return ~checksum == bytes[bytes.length - 1];
    }

    public LX16A_Servo(SerialPort sharedPort) {
	port = sharedPort;
	port.setBaudRate(115200);
	port.setDTR();
	port.openPort();
	portout = port.getOutputStream();
	portin = port.getInputStream();
	port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1, 1);
    }

    private static short bytesToShort(byte lsb, byte msb) {
	return (short) ((msb << 8) + lsb);
    }

    private static int getMax(int a, int b) {
	return (a > b ? a : b);
    }

    public void close() {
	port.closePort();
    }

    private boolean sendCommand(byte servoId, byte command, short... paramaters) {
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	outputStream.write(SERVO_COMMAND_START);
	outputStream.write(SERVO_COMMAND_START);
	outputStream.write(servoId);
	outputStream.write((byte) (paramaters.length * 2 + 3));
	outputStream.write(command);
	for (short param : paramaters) {
	    outputStream.write((byte) (param & 0xFF));// LSB
	    outputStream.write((param & 0xFF00) >> 8);// MSB
	}
	outputStream.write(CHECKSUM_HOLDER);

	byte commandPacket[] = outputStream.toByteArray();

	try {
	    portout.write(setChecksum(commandPacket));
	    portout.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}

	return true;
    }

    private boolean sendCommand(byte servoId, byte command, byte... paramaters) {
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	outputStream.write(SERVO_COMMAND_START);
	outputStream.write(SERVO_COMMAND_START);
	outputStream.write(servoId);
	outputStream.write((byte) (paramaters.length + 3));
	outputStream.write(command);
	for (byte param : paramaters) {
	    outputStream.write(param);
	}
	outputStream.write(CHECKSUM_HOLDER);

	byte commandPacket[] = outputStream.toByteArray();

	try {
	    portout.write(setChecksum(commandPacket));
	    portout.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}

	return true;
    }

    private byte[] readBytes(int count) {
	byte[] result = new byte[count];
	byte[] readBuffer = new byte[count];
	int resultRead = 0;
	int numRead = 0;
	for (int tries = count; tries > 0; tries -= getMax(1, numRead)) {
	    numRead = port.readBytes(readBuffer, count);
	    if (numRead > 0) {
		System.arraycopy(readBuffer, 0, result, resultRead, numRead);
		resultRead += numRead;
	    }
	}
	return result;
    }

    @Override
    public Short getPosition(int servoId) {
	final Short pos; // TODO: do this on all returns to make them immutable
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_POS_READ);
	    byte[] data = readBytes(SERVO_POS_READ_READ_BYTES);
	    pos = bytesToShort(data[5], data[6]);
	    actualPosition.put((byte) servoId, pos);
	}
	return pos;
    }

    @Override
    public Double getVoltage(int servoId) {
	final Double voltage;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_VIN_READ);
	    byte[] data = readBytes(SERVO_VIN_READ_READ_BYTES);

	    voltage = bytesToShort(data[5], data[6]) / 1000.0;
	    currentVoltage.put((byte) servoId, voltage);
	}
	return voltage;
    }

    @Override
    public Short getMaxPosition(int servoId) {
	short min = -1;
	final Short max;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_ANGLE_LIMIT_READ);
	    byte[] bytes = readBytes(SERVO_ANGLE_LIMIT_READ_READ_BYTES);
	    max = bytesToShort(bytes[7], bytes[8]);
	    min = bytesToShort(bytes[5], bytes[6]);

	    minAngleLimit.put((byte) servoId, min);
	    maxAngleLimit.put((byte) servoId, max);
	}
	return max;
    }

    @Override
    public Short getMinPosition(int servoId) {
	final Short min;
	short max = -1;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_ANGLE_LIMIT_READ);
	    byte[] bytes = readBytes(SERVO_ANGLE_LIMIT_READ_READ_BYTES);
	    max = bytesToShort(bytes[7], bytes[8]);
	    min = bytesToShort(bytes[5], bytes[6]);

	    minAngleLimit.put((byte) servoId, min);
	    maxAngleLimit.put((byte) servoId, max);
	}
	return min;
    }

    @Override
    public Boolean setPosition(int servoId, short pos, short speed) {
	synchronized (portin) {
	    targetPosition.put((byte) servoId, pos);
	    return sendCommand((byte) servoId, SERVO_MOVE_TIME_WRITE, pos,
		    speed);
	}
    }

    @Override
    public void setMaxPosition(int servoId, short pos) {
	short min = getMinPosition(servoId);
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_ANGLE_LIMIT_WRITE, min, pos);
	}
    }

    @Override
    public void setMinPosition(int servoId, short pos) {
	short max = getMaxPosition(servoId);
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_ANGLE_LIMIT_WRITE, pos, max);
	}
    }

    @Override
    public void setLedState(int servoId, boolean state) {
	synchronized (portin) {
	    if (state) {
		sendCommand((byte) servoId, SERVO_LED_CTRL_WRITE, (byte) 0);
	    } else {
		sendCommand((byte) servoId, SERVO_LED_CTRL_WRITE, (byte) 1);
	    }
	}
    }

    @Override
    public Boolean getLedState(int servoId) {
	final Boolean lit;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_LED_CTRL_READ);
	    byte[] data = readBytes(SERVO_LED_CTRL_READ_READ_BYTES);

	    if (data[5] == 0) {
		ledState.put((byte) servoId, true);
		lit = true;
	    } else if (data[5] == 1) {
		ledState.put((byte) servoId, false);
		lit = false;
	    } else {
		lit = false;
	    }

	}
	return lit;
    }

    public void setMode(int servoId, byte mode) {
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_OR_MOTOR_MODE_WRITE, mode);
	    servoMode.put((byte) servoId, mode);
	}
    }

    public byte getMode(int servoId) {
	byte mode = -1;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_OR_MOTOR_MODE_READ);
	    byte[] data = readBytes(SERVO_OR_MOTOR_MODE_READ_READ_BYTES);
	    if (data[5] == 0) {
		mode = SERVO_MODE;
	    } else if (data[5] == 1) {
		mode = MOTOR_MODE;
	    } else {
		mode = UNKNOWN_MODE;
	    }
	    servoMode.put((byte) servoId, mode);
	}
	return mode;
    }

    @Override
    public void setMotorEnable(int servoId) {
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_LOAD_OR_UNLOAD_WRITE, (byte) 1);
	    motorEnabled.put((byte) servoId, (byte) 1);
	}
    }

    @Override
    public void setMotorDisable(int servoId) {
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_LOAD_OR_UNLOAD_WRITE, (byte) 0);
	    motorEnabled.put((byte) servoId, (byte) 0);
	}
    }

    @Override
    public Boolean getMotorEnabled(int servoId) {
	Boolean enabled = false;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_LOAD_OR_UNLOAD_READ);
	    byte[] data = readBytes(SERVO_LOAD_OR_UNLOAD_READ_READ_BYTES);

	    if (data[5] == 1) {
		enabled = true;
	    }
	}
	return enabled;
    }

    @Override
    public Byte getLedErrorState(int servoId) {
	final Byte errorState;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_LED_ERROR_READ);
	    byte[] data = readBytes(SERVO_LED_ERROR_READ_READ_BYTES);
	    errorState = data[5];
	}
	return errorState;
    }

    @Override
    public void setLedErrorState(int servoId, byte state) {
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_LED_ERROR_WRITE, state);
	}
    }

    @Override
    public void setAngleOffset(int servoId, byte offset) {
	synchronized (portin) {
	    // set it non-volatile
	    angleOffset.put((byte) servoId, offset);
	    sendCommand((byte) servoId, SERVO_ANGLE_OFFSET_WRITE, offset);
	    // make it take effect immediately
	    sendCommand((byte) servoId, SERVO_ANGLE_OFFSET_ADJUST, offset);
	}
    }

    @Override
    public Byte getAngleOffset(int servoId) {
	final Byte offset;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_ANGLE_OFFSET_READ);
	    byte[] data = readBytes(SERVO_ANGLE_OFFSET_READ_READ_BYTES);
	    offset = data[5];
	    angleOffset.put((byte) servoId, offset);
	}
	return offset;
    }

    @Override
    public Short getMinVoltage(int servoId) {
	final Short min;
	short max = -1;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_VIN_LIMIT_READ);
	    byte[] bytes = readBytes(SERVO_VIN_LIMIT_READ_READ_BYTES);
	    min = bytesToShort(bytes[7], bytes[8]);
	    max = bytesToShort(bytes[5], bytes[6]);

	    minVInLimit.put((byte) servoId, min);
	    maxVInLimit.put((byte) servoId, max);
	}
	return min;
    }

    @Override
    public void setMinVoltage(int servoId, short voltage) {
	short max = getMaxVoltage(servoId);
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_VIN_LIMIT_WRITE, voltage, max);
	}
    }

    @Override
    public Short getMaxVoltage(int servoId) {
	short min = -1;
	final Short max;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_VIN_LIMIT_READ);
	    byte[] bytes = readBytes(SERVO_VIN_LIMIT_READ_READ_BYTES);
	    min = bytesToShort(bytes[7], bytes[8]);
	    max = bytesToShort(bytes[5], bytes[6]);

	    minVInLimit.put((byte) servoId, min);
	    maxVInLimit.put((byte) servoId, max);
	}
	return max;
    }

    @Override
    public void setMaxVoltage(int servoId, short voltage) {
	short min = getMinVoltage(servoId);
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_VIN_LIMIT_WRITE, min, voltage);
	}
    }

    @Override
    public Byte getTemperature(int servoId) {
	final Byte temp;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_TEMP_READ);
	    byte[] data = readBytes(SERVO_TEMP_READ_READ_BYTES);
	    temp = data[5];
	    currentTemp.put((byte) servoId, temp);
	}
	return temp;
    }

    @Override
    public Byte getMaxTemperature(int servoId) {
	final Byte temp;
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_TEMP_MAX_LIMIT_READ);
	    byte[] data = readBytes(SERVO_TEMP_MAX_LIMIT_READ_READ_BYTES);
	    temp = data[5];
	    maxTemp.put((byte) servoId, temp);
	}
	return temp;
    }

    @Override
    public void setMaxTemperature(int servoId, short temp) {
	synchronized (portin) {
	    sendCommand((byte) servoId, SERVO_TEMP_MAX_LIMIT_WRITE, (byte) temp);
	}
    }

    @Override
    public Boolean hasVoltage() {
	return true;
    }

    @Override
    public Boolean hasTemperature() {
	return true;
    }

    @Override
    public Boolean hasMinPosition() {
	return true;
    }

    @Override
    public Boolean hasMaxPosition() {
	return true;
    }

    @Override
    public Boolean hasLed() {
	return true;
    }

    public static void main(String[] args) {
	SerialPort[] ports = SerialPort.getCommPorts();
	LX16A_Servo lx = new LX16A_Servo(ports[0]);
	lx.setPosition(3, (short) 100, (short) 1500);
	System.out.println(lx.getMinPosition(3));
	System.out.println(lx.getMinPosition(3));
	System.out.println(lx.getMinPosition(3));
	System.out.println(lx.getMinPosition(3));
	System.out.println("now fail");
	System.out.println(lx.getMaxPosition(4));
	System.out.println(lx.getMaxPosition(3));
	System.out.println(lx.getMaxPosition(3));
	System.out.println(lx.getMaxPosition(3));
	System.out.println(lx.getMaxPosition(3));
	lx.setPosition(3, (short) 700, (short) 1500);
    }
}
