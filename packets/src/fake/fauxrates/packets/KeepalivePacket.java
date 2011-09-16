package fake.fauxrates.packets;

import java.util.Calendar;
import java.util.Date;

public class KeepalivePacket extends Packet {

	private Date time = Calendar.getInstance().getTime();

	public Date getTime () {
		return time;
	}

	public void setTime (Date time) {
		this.time = time;
	}
}
