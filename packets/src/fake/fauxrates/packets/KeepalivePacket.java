package fake.fauxrates.packets;

import java.util.Calendar;
import java.util.Date;

public class KeepalivePacket extends Packet {

	Date time = Calendar.getInstance().getTime();
}
