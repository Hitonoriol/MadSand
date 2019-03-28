package ru.bernarder.fallenrisefromdust;

public class NetApi {
	static int checkLogOn(String nick, String pass) {
		try {
			String resp = NetListener.getRespond("http://applang.tk/games/madsand/auth.php?n=" + nick + "&p=" + pass);
			return new Integer(resp).intValue();
		} catch (Exception e) {
		}
		return 0;
	}
}
