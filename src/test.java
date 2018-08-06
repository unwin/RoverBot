import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SerialPort[] ports = SerialPort.getCommPorts();
		for (final SerialPort port : ports) {
			port.setBaudRate(115200);
			port.setDTR();
			port.openPort();
			System.out.println(port.getDescriptivePortName());
			System.out.println(port.toString());
			System.out.println(port.getSystemPortName());
			System.out.println(port.isOpen());
			System.out.println(port.getBaudRate());
			System.out.println(port.getDSR());
			System.out.println(port.getCTS());

			port.addDataListener(new SerialPortDataListener() {
				@Override
				public int getListeningEvents() {
					return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
				}

				@Override
				public void serialEvent(SerialPortEvent event) {
					if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
						return;
					byte[] newData = new byte[port.bytesAvailable()];
					int numRead = port.readBytes(newData, newData.length);
					System.out.println("Read " + numRead + " bytes.");
				}
			});

			port.closePort();
			System.out.println(port.isOpen());
		}

	}

}
